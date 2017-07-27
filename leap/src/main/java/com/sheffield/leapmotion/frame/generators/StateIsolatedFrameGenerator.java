package com.sheffield.leapmotion.frame.generators;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.GestureList;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.controller.mocks.SeededCircleGesture;
import com.sheffield.leapmotion.controller.mocks.SeededFrame;
import com.sheffield.leapmotion.controller.mocks.SeededHand;
import com.sheffield.leapmotion.frame.analyzer.machinelearning.ngram.NGram;
import com.sheffield.leapmotion.frame.generators.gestures.GestureHandler;
import com.sheffield.leapmotion.frame.util.Quaternion;
import com.sheffield.leapmotion.output.StateComparator;
import com.sheffield.leapmotion.util.FileHandler;
import com.sheffield.output.Csv;
import java.lang.reflect.Type;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StateIsolatedFrameGenerator extends FrameGenerator implements GestureHandler {
	@Override
	public Csv getCsv() {
		return new Csv();
	}

	private HashMap<Integer, NGramFrameGenerator> generators;

	private NGramFrameGenerator currentGenerator;

	private int currentState = 0;

    public void setOutputFiles(File pos, File rot){
		for (NGramFrameGenerator ngfg : generators.values()){
			ngfg.setOutputFiles(pos, rot);
		}
    }

	public void setJointsOutputFile(File outputFile){
		for (NGramFrameGenerator ngfg : generators.values()){
			ngfg.setOutputJointsFile(outputFile);
		}
	}

	public void setGestureOutputFile(File outputFile){
		for (NGramFrameGenerator ngfg : generators.values()){
			ngfg.setGestureOutputFile(outputFile);
		}
	}

	public StateIsolatedFrameGenerator(String filename) throws IOException {

		String fileStart = Properties.DIRECTORY + "/" + filename + "/processed/";

		HashMap<String, SeededHand> joints = NGramFrameGenerator.getJoints(fileStart );

		HashMap<String, Vector> positions = NGramFrameGenerator.getPositions(fileStart);

		HashMap<String, Quaternion> rotations = NGramFrameGenerator.getRotations(fileStart);

		HashMap<String, Vector[]> stabilisedTips = NGramFrameGenerator
				.getStabilizedTips(fileStart);

		HashMap<String, SeededCircleGesture> circleGestures = NGramFrameGenerator
				.getCircleGestures(fileStart);

		Type type =  new TypeToken<Map<Integer, Integer[]>>(){}.getType();

		HashMap<Integer, Integer[]> states = gson.fromJson(FileHandler.readFile(new File(fileStart + "raw_states")), type);

		HashMap<Integer, Integer> stateAssignment = new HashMap<>();

		for (Integer state : states.keySet()){
			int newState = StateComparator.addState(states.get(state));

			stateAssignment.put(state, newState);
		}

		Map<Integer, NGram> jointNgrams = null;
		if (!Properties.SINGLE_DATA_POOL){
			jointNgrams = loadStateNgram(fileStart + "joint_position_stategram", stateAssignment);
		} else {
			jointNgrams = loadStateNgram(fileStart + "hand_joints_stategram", stateAssignment);
		}
		Map<Integer, NGram> positionNgrams = loadStateNgram(fileStart + "hand_position_stategram", stateAssignment);
		Map<Integer, NGram> rotationNgrams = loadStateNgram(fileStart + "hand_rotation_stategram", stateAssignment);
		Map<Integer, NGram> gestureNgrams = loadStateNgram(fileStart + "gesture_type_stategram", stateAssignment);
		Map<Integer, NGram> tipNgrams = loadStateNgram(fileStart +
				"stabilised_tip_stategram", stateAssignment);

		Map<Integer, NGram> circleNgrams = loadStateNgram(fileStart +
				"gesture_circle_stategram", stateAssignment);


		for (Integer i : positionNgrams.keySet()){
			if (!gestureNgrams.containsKey(i)){
				gestureNgrams.put(i, gestureNgrams.get(-1));
			}
		}

		generators = new HashMap<Integer, NGramFrameGenerator>();

		for (Integer i : stateAssignment.keySet()){
			if (jointNgrams.containsKey(i) && positionNgrams.containsKey(i) && rotationNgrams.containsKey(i)) {
				Integer newState = stateAssignment.get(i);
				NGramFrameGenerator newFs = new NGramFrameGenerator(jointNgrams.get(i), positionNgrams.get(i), rotationNgrams.get(i),
						gestureNgrams.get(i), tipNgrams.get(i), circleNgrams.get(i),
						joints, positions, rotations, stabilisedTips, circleGestures);
				if (generators.containsKey(newState)) {
					generators.get(newState).merge(newFs);
				} else {
					generators.put(newState, newFs);
				}
			}
		}

		NGram jNgram = jointNgrams.get(-1);
		NGram pNgram = positionNgrams.get(-1);
		NGram rNgram = rotationNgrams.get(-1);
		NGram gNgram = gestureNgrams.get(-1);
		NGram stNgram = tipNgrams.get(-1);
		NGram circleNgram = circleNgrams.get(-1);

		generators.put(-1, new NGramFrameGenerator(jNgram, pNgram, rNgram,
				gNgram, stNgram, circleNgram,
				joints, positions, rotations, stabilisedTips, circleGestures));

		currentGenerator = generators.get(-1);


	}

	private	Gson gson = new Gson();

	public Map<Integer, NGram> loadStateNgram(String file, HashMap<Integer, Integer> stateAssignment) throws IOException {

		Type type =  new TypeToken<Map<Integer, NGram>>(){}.getType();

		Map<Integer, NGram> stateNg = gson.fromJson(FileHandler.readFile(new File(file)), type);

		HashMap<Integer, NGram> reassigned = new HashMap<>();

		for (Integer state : stateNg.keySet()){
			if (state != -1) {
				reassigned.put(stateAssignment.get(state), stateNg.get(state));
			}
		}

		NGram defaultNgram = stateNg.get(-1);

		reassigned.put(-1, defaultNgram);

		return reassigned;

	}


	@Override
	public Frame newFrame() {

		try {
			return currentGenerator.newFrame();
		} catch (DataSparsityException e){
			return generators.get(-1).newFrame();
		}

	}

	@Override
	public String status() {
		return "ss|" + StateComparator.getStates().size() + "|:" + StateComparator.getCurrentState();
	}

	@Override
	public void modifyFrame(SeededFrame frame) {

		try {
			currentGenerator.modifyFrame(frame);
		} catch (DataSparsityException e){
			generators.get(-1).modifyFrame(frame);
		}

	}

	@Override
	public boolean allowProcessing() {
		return true;
	}

	@Override
	public String getName() {
		return "State Dependent";
	}

	private long lastUpdate = 0;
	@Override
	public void tick(long time) {
		lastUpdate = time;

		try {

			currentState = updateState();

			if (generators.containsKey(currentState)) {
				currentGenerator = generators.get(currentState);
			} else {
				//-1 contains single model NGram
				currentGenerator = generators.get(-1);
			}

			currentGenerator.tick(time);

		} catch (DataSparsityException e){
			generators.get(-1).tick(time);
		}

	}

	private int updateState() {
		return StateComparator.getCurrentState();
	}

	public long lastTick(){
		return lastUpdate;
	}

	@Override
	public void cleanUp() {
		for (NGramFrameGenerator ngfg : generators.values()){
			ngfg.cleanUp();
		}
	}

	@Override
	public GestureList handleFrame(Frame frame, Controller controller) {
		try {
			return currentGenerator.handleFrame(frame, controller);
		} catch (DataSparsityException e){
			return generators.get(-1).handleFrame(frame, controller);
		}
	}


}
