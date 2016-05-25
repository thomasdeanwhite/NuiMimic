package com.sheffield.leapmotion.frameselectors;

import com.leapmotion.leap.Frame;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.Serializer;
import com.sheffield.leapmotion.controller.FrameHandler;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.display.DisplayWindow;
import com.sheffield.leapmotion.listeners.FrameSwitchListener;
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

	private long seededTimePassed = 0;
	private FrameHandler fh = null;

	private DisplayWindow dw;
	
	public UserPlaybackFrameSelector(FrameSelector frameSelector) {
		lastSwitchRate = Properties.FRAMES_PER_SECOND;
		Properties.FRAMES_PER_SECOND = 30;
		backupFrameSelector = frameSelector;
		String playback = Properties.PLAYBACK_FILE;
		try {
			lineIterator = FileUtils.lineIterator(new File(playback));
			frameStack = new ArrayList<Frame>();
			int counter = 10;
			while (counter > 0 && lineIterator.hasNext()){
				frameStack.add(Serializer.sequenceFromJson(lineIterator.nextLine()));
				counter--;
			}

			if (frameSelector instanceof TrainingDataPlaybackFrameSelector){
				Properties.PLAYBACK_FILE = null;
				fh = new FrameHandler();
				fh.init();
				dw = new DisplayWindow();
				fh.addFrameSwitchListener(new FrameSwitchListener() {
					@Override
					public void onFrameSwitch(Frame lastFrame, Frame nextFrame) {
						dw.setFrame(nextFrame);
					}
				});
				dw.setLocation(dw.getWidth(), 0);
				Properties.PLAYBACK_FILE = playback;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(App.out);
			seeded = true;
		}

	}

	@Override
	public Frame newFrame() {
		if (fh != null){
			fh.loadNewFrame();
		}

		if (seeded) {
			return backupFrameSelector.newFrame();
		}

		if (frameStack.size() <= 0 || !lineIterator.hasNext()) {
			seeded = true;
			Properties.FRAMES_PER_SECOND = lastSwitchRate;
			App.out.println("- Finished seeding after " + (seededTime-startSeedingTime) + "ms. " +  + SeededController.getSeededController().now());
			return backupFrameSelector.newFrame();
		}

		Frame f = null;


		long time = System.currentTimeMillis();

		if (startSeedingTime == 0){
			startSeedingTime = time;
		}

		seededTime = time;

		long currentTimePassed = seededTime - startSeedingTime;

		while (currentTimePassed > seededTimePassed && lineIterator.hasNext()){
			f = Serializer.sequenceFromJson(lineIterator.nextLine());

			frameStack.add(f);

			frameStack.remove(0);

			if (firstFrameTimeStamp == 0) {
				firstFrameTimeStamp = f.timestamp()/1000;
			}

			seededTimePassed = (f.timestamp()/1000) - firstFrameTimeStamp;
		}
		
		f =  frameStack.get(0);

		//seededTimePassed = f.timestamp() - firstFrameTimeStamp;

		if (firstFrameTimeStamp == 0) {
			firstFrameTimeStamp = f.timestamp()/1000;
		}


		return f;
	}

	public FrameSelector getBackupFrameSelector (){
		return backupFrameSelector;
	}

	public boolean finished (){
		return seeded;
	}

}
