package com.sheffield.leapmotion.frameselectors;

import com.leapmotion.leap.Frame;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.Serializer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class UserPlaybackFrameSelector extends FrameSelector {

	private FrameSelector backupFrameSelector;
	private ArrayList<Frame> frameStack;
	private long startSeedingTime = 0;

	private long lastSwitchRate = 0;

	private boolean seeded = false;

	private LineIterator lineIterator;

	private long seededTime = 0;
	private long firstFrameTimeStamp = 0;
	
	public UserPlaybackFrameSelector(FrameSelector frameSelector) {
		lastSwitchRate = Properties.FRAMES_PER_SECOND;
		Properties.FRAMES_PER_SECOND = 30;
		backupFrameSelector = frameSelector;
		try {
			lineIterator = FileUtils.lineIterator(new File(Properties.PLAYBACK_FILE));
			frameStack = new ArrayList<Frame>();
			int counter = 10;
			while (counter > 0 && lineIterator.hasNext()){
				frameStack.add(Serializer.sequenceFromJson(lineIterator.nextLine()));
				counter--;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(App.out);
			seeded = true;
		}

	}

	@Override
	public Frame newFrame() {
		if (seeded) {
			return backupFrameSelector.newFrame();
		}
		if (startSeedingTime == 0){
			startSeedingTime = System.currentTimeMillis();
		}

		seededTime = System.currentTimeMillis();

		if (frameStack.size() <= 0 || !lineIterator.hasNext()) {
			seeded = true;
			Properties.FRAMES_PER_SECOND = lastSwitchRate;
			App.out.println("- Finished seeding after " + (-startSeedingTime) + "ms.");
			return backupFrameSelector.newFrame();
		}

		Frame f = Serializer.sequenceFromJson(lineIterator.nextLine());

		if (firstFrameTimeStamp == 0) {
			firstFrameTimeStamp = f.timestamp();
		}

		long seededTimePassed = f.timestamp() - firstFrameTimeStamp;

		long currentTimePassed = seededTime - startSeedingTime;

		while (currentTimePassed > seededTimePassed){
			f = Serializer.sequenceFromJson(lineIterator.nextLine());

			if (firstFrameTimeStamp == 0) {
				firstFrameTimeStamp = f.timestamp();
			}

			seededTimePassed = f.timestamp() - firstFrameTimeStamp;

			currentTimePassed = seededTime - startSeedingTime;
		}

		frameStack.add(f);
		
		return frameStack.remove(0);
	}

	public FrameSelector getBackupFrameSelector (){
		return backupFrameSelector;
	}

	public boolean finished (){
		return seeded;
	}

}
