package com.sheffield.leapmotion.frameselectors;

import com.leapmotion.leap.Frame;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.sampler.Serializer;
import com.sheffield.leapmotion.FileHandler;
import com.sheffield.instrumenter.Properties;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class UserPlaybackFrameSelector extends FrameSelector {

	private FrameSelector backupFrameSelector;
	private ArrayList<Frame> frameStack;
	private long startSeedingTime = 0;

	private long lastSwitchRate = 0;

	private boolean seeded = false;
	
	public UserPlaybackFrameSelector(FrameSelector frameSelector) {
		lastSwitchRate = Properties.SWITCH_RATE;
		Properties.SWITCH_RATE = lastSwitchRate;
		backupFrameSelector = frameSelector;
		try {
			String frames = FileHandler.readFile(new File(Properties.PLAYBACK_FILE));

			frameStack = Serializer.sequenceFromJson(frames);

			App.out.println("- Seeding " + frameStack.size() + " frames before generation starts.");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			seeded = true;
		}

	}

	@Override
	public Frame newFrame() {
		if (seeded) {
			return backupFrameSelector.newFrame();
		}
		if (startSeedingTime == 0){
//			try {
//				Thread.sleep(10000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
			startSeedingTime = System.currentTimeMillis();
		}

		if (frameStack.size() <= 0) {
			seeded = true;
			Properties.SWITCH_RATE = lastSwitchRate;
			App.out.println("- Finished seeding after " + (System.currentTimeMillis()-startSeedingTime) + "ms.");
			return backupFrameSelector.newFrame();
		}
		
		return frameStack.remove(0);
	}

}
