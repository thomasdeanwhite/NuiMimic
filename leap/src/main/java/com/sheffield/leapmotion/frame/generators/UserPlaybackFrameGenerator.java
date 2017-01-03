package com.sheffield.leapmotion.frame.generators;

import com.google.gson.JsonSyntaxException;
import com.leapmotion.leap.Bone;
import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.controller.FrameHandler;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.controller.listeners.FrameSwitchListener;
import com.sheffield.leapmotion.controller.mocks.SeededFrame;
import com.sheffield.leapmotion.display.DisplayWindow;
import com.sheffield.leapmotion.instrumentation.MockSystem;
import com.sheffield.leapmotion.sampler.SamplerApp;
import com.sheffield.leapmotion.util.AppStatus;
import com.sheffield.leapmotion.util.Serializer;
import com.sheffield.output.Csv;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class UserPlaybackFrameGenerator extends FrameGenerator implements App.TimeHandler {


	int currentFrame = 0;

	@Override
	public Csv getCsv() {
		Csv csv = new Csv();

		csv.add("discardedFrames", (currentFrame - handsSeeded) + "");

		csv.finalize();

		return csv;
	}

	@Override
	public void modifyFrame(SeededFrame frame) {

	}

	@Override
	public boolean allowProcessing() {
		return true;
	}

	private FrameGenerator backupFrameGenerator;
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
	
	public UserPlaybackFrameGenerator(FrameGenerator frameGenerator,
                                      Controller controller) {

		Properties.FRAME_SELECTION_STRATEGY = Properties
				.FrameSelectionStrategy.USER_PLAYBACK;

		if (Properties.PROCESS_PLAYBACK || Properties.PROCESS_SCREENSHOTS){
			// This happens when we want to split frames into separate models
			SamplerApp.LOOP = false;
			SamplerApp.main(new String[]{Properties.PLAYBACK_FILE.substring
					(0, Properties.PLAYBACK_FILE.indexOf("."))});

			SamplerApp.RECORDING_USERS = false;

            controller.addListener(SamplerApp.getApp());
		}

		lastSwitchRate = Properties.FRAMES_PER_SECOND;
		Properties.FRAMES_PER_SECOND = 120;
		backupFrameGenerator = frameGenerator;
		String playback = Properties.PLAYBACK_FILE;
		try {
			maxFrames = Files.lines(Paths.get(playback)).count() - 1;
			lineIterator = FileUtils.lineIterator(new File(playback));
			frameStack = new ArrayList<Frame>();
			//int counter = Properties.MAX_LOADED_FRAMES;

			while (lineIterator.hasNext()){
				try {
					frameStack.add(Serializer
							.sequenceFromJson(lineIterator.nextLine()));
				} catch (JsonSyntaxException e){
					// some bad JSON data;
				}
				//counter--;
			}

			frameStack.sort(new Comparator<Frame>() {
				@Override
				public int compare(Frame o1, Frame o2) {
					return (int) (o1.timestamp() - o2.timestamp());
				}
			});

			if (frameGenerator instanceof ReconstructiveFrameGenerator){
				Properties.PLAYBACK_FILE = null;
				boolean vis = Properties.VISUALISE_DATA;
				Properties.VISUALISE_DATA = false;
				fh = new FrameHandler();
				fh.init(controller);

				Properties.VISUALISE_DATA = vis;
				if (Properties.VISUALISE_DATA) {
					dw = new DisplayWindow();
					fh.addFrameSwitchListener(new FrameSwitchListener() {
						@Override
						public void onFrameSwitch(Frame lastFrame, Frame nextFrame) {
							dw.setFrame(nextFrame);
						}
					});
					dw.setLocation(dw.getWidth(), 0);
				}
				Properties.PLAYBACK_FILE = playback;


				ReconstructiveFrameGenerator
                        tdps = (ReconstructiveFrameGenerator) frameGenerator;

//				if (tdps.size() != maxFrames){
//					new IllegalArgumentException("Frame stack: " + maxFrames+ ", Training Stack: " + tdps.size() + ". Should be equal.").printStackTrace(App.out);
//				}



			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(App.out);
			seeded = true;
		}

		//App.TIME_HANDLER = this;

		App.out.println("- Finished loading frames. Starting playback.");



	}

	private Frame fhFrame = null;

	@Override
	public Frame newFrame() {

		if (seeded) {
			App.getApp().setStatus(AppStatus.FINISHED);
			return backupFrameGenerator.newFrame();
		}

		if (frameStack.size() == 0){
			App.getApp().setStatus(AppStatus.FINISHED);
			return Frame.invalid();
		}

		Frame f = null;


		if (fh != null){
			fh.loadNewFrame();
			fhFrame = fh.getFrame();
		}


		
		f =  frameStack.get(currentFrame);


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
						float mean = totalDifferences.get(ft).get(bt) / currentFrame;
						counter++;
						differences += mean*mean;
					}
				}
			}
			return "rms: " + Math.sqrt(differences / counter);
		}
		return (frameStack.size() - currentFrame) + "s, " + (currentFrame -
				handsSeeded) + "d";
	}

	public FrameGenerator getBackupFrameGenerator(){
		return backupFrameGenerator;
	}

	public boolean finished (){
		return seeded;
	}

	private long handsSeeded = 0;
	private long lastUpdate = 0;

	@Override
	public void tick(long time) {

		if (frameStack.size() == 0){
			return;
		}

		lastUpdate = time;

		if (currentFrame >= frameStack.size()-1){ // last frame!
			seeded = true;
			Properties.FRAMES_PER_SECOND = lastSwitchRate;
			App.out.println("- Finished seeding after " + (seededTime-startSeedingTime) + "ms. " +  + SeededController.getSeededController().now());
		}


		if (startSeedingTime == 0){
			startSeedingTime = time;
		}

		seededTime = time;

		long currentTimePassed = seededTime - startSeedingTime;

		Frame f = frameStack.get(currentFrame);
		if (currentTimePassed >= seededTimePassed && currentFrame <
				frameStack.size()) {
			f = frameStack.get(currentFrame++);

			if (firstFrameTimeStamp == 0) {
				firstFrameTimeStamp = frameStack.get(0).timestamp() / 1000;
			}

			seededTimePassed = (((f.timestamp() / 1000) - firstFrameTimeStamp));

		}

		handsSeeded++;

		// This compares RECONSTRUCTION to USER_PLAYBACK and calculates
        // differences
		if (fh != null){
			fh.tick(time);
			if (fhFrame != null && fhFrame.isValid() && fhFrame.hands().count() > 0){
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

	@Override
	public void cleanUp() {
		if (dw != null){
			dw.setVisible(false);
			dw = null;
		}
	}

	@Override
	public void setMillis(long executionTime) {
		MockSystem.MILLIS = frameStack.get(0).timestamp() / 1000;
	}

	@Override
	public void setNanos(long executionTime) {
		MockSystem.NANOS = frameStack.get(0).timestamp() * 1000;
	}
}
