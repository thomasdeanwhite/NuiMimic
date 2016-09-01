package com.sheffield.leapmotion.frameselectors;

import com.leapmotion.leap.Bone;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Vector;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

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

	private long maxFrames = 0;

	private DisplayWindow dw;
	
	public UserPlaybackFrameSelector(FrameSelector frameSelector) {
		lastSwitchRate = Properties.FRAMES_PER_SECOND;
		Properties.FRAMES_PER_SECOND = 30;
		backupFrameSelector = frameSelector;
		String playback = Properties.PLAYBACK_FILE;
		try {
			maxFrames = Files.lines(Paths.get(playback)).count() - 1;
			lineIterator = FileUtils.lineIterator(new File(playback));
			frameStack = new ArrayList<Frame>();
			//int counter = Properties.MAX_LOADED_FRAMES;

			while (lineIterator.hasNext()){
				frameStack.add(Serializer.sequenceFromJson(lineIterator.nextLine()));
				//counter--;
			}

			if (frameSelector instanceof ReconstructiveFrameSelector){
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


				ReconstructiveFrameSelector tdps = (ReconstructiveFrameSelector)frameSelector;

				if (tdps.size() != maxFrames){
					new IllegalArgumentException("Frame stack: " + maxFrames+ ", Training Stack: " + tdps.size() + ". Should be equal.").printStackTrace(App.out);
				}


			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(App.out);
			seeded = true;
		}

		App.out.println("- Finished loading frames. Starting playback.");

	}

	private Frame fhFrame = null;

	@Override
	public Frame newFrame() {

		if (seeded) {
			return backupFrameSelector.newFrame();
		}

		Frame f = null;


		if (fh != null){
			fh.loadNewFrame();
			fhFrame = fh.getFrame();
		}


		
		f =  frameStack.get(0);


		return f;
	}

	private HashMap<Finger.Type, HashMap<Bone.Type, Float>> totalDifferences;

	@Override
	public String status() {

		float differences = 0;

		int counter = 0;

 		if (totalDifferences != null){
			for (Finger.Type ft : Finger.Type.values()){
				for (Bone.Type bt : Bone.Type.values()){
					if (totalDifferences.containsKey(ft) &&
							totalDifferences.get(ft).containsKey(bt)){
						float mean = totalDifferences.get(ft).get(bt) / handsSeen;
						counter++;
						differences += mean*mean;
					}
				}
			}
			return "rms: " + Math.sqrt(differences / counter);
		} else {
			return "";
		}
	}

	public FrameSelector getBackupFrameSelector (){
		return backupFrameSelector;
	}

	public boolean finished (){
		return seeded;
	}

	private long handsSeen = 1;
	private long lastUpdate = 0;
	@Override
	public void tick(long time) {

		lastUpdate = time;

		if (frameStack.size() <= 0){// || !lineIterator.hasNext()) {
			seeded = true;
			Properties.FRAMES_PER_SECOND = lastSwitchRate;
			App.out.println("- Finished seeding after " + (seededTime-startSeedingTime) + "ms. " +  + SeededController.getSeededController().now());
		}


		if (startSeedingTime == 0){
			startSeedingTime = time;
		}

		seededTime = time;

		long currentTimePassed = seededTime - startSeedingTime;

		Frame f = frameStack.get(0);

		while (currentTimePassed > seededTimePassed){// && lineIterator.hasNext()){
//			f = Serializer.sequenceFromJson(lineIterator.nextLine());
//
//			frameStack.add(f);

			f = frameStack.get(1);

			if (firstFrameTimeStamp == 0) {
				firstFrameTimeStamp = f.timestamp()/1000;
			} else {
				frameStack.remove(0);
			}

			seededTimePassed = (((f.timestamp()/1000) - firstFrameTimeStamp));
		}

		if (firstFrameTimeStamp == 0) {
			firstFrameTimeStamp = f.timestamp()/1000;
		} else {
			handsSeen++;
		}

		if (fh != null){
			fh.tick(time);
			if (fhFrame != null && fhFrame.isValid()){
				if (totalDifferences == null){
					totalDifferences = new HashMap<Finger.Type, HashMap<Bone.Type, Float>>();
					for (Finger.Type ft : Finger.Type.values()){

						totalDifferences.put(ft, new HashMap<Bone.Type, Float>());
						for (Bone.Type bt : Bone.Type.values()){
							totalDifferences.get(ft).put(bt, 0f);
						}

					}

				}
				Hand fh = fhFrame.hands().iterator().next();
				Hand h = frameStack.get(0).hands().iterator().next();

				for (Finger rf : h.fingers()) {
					Finger f1 = null;
					for (Finger fhf : fh.fingers()){
						if (fhf.type().equals(rf.type())){
							f1 = fhf;
							break;
						}
					}
					if (rf == null || !rf.isValid() || f1 == null || !f1.isValid()) {
						continue;
					}

					for (Bone.Type t : Bone.Type.values()) {
						Bone b = rf.bone(t);
						Bone b1 = f1.bone(t);
						if (!b.isValid()) {
							continue;
						}
						if (b.isValid()) {

							Vector origin = h.palmPosition();


							float difference = b1.center().minus(origin).minus(b.center().minus(origin)).magnitude();

							totalDifferences.get(rf.type()).put(b.type(),
									totalDifferences.get(rf.type()).get(b.type()) + difference);


						}
					}
				}

			}
		}



	}

	public long lastTick(){
		return lastUpdate;
	}

}
