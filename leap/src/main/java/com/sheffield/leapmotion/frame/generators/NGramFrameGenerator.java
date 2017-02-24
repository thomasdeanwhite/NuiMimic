package com.sheffield.leapmotion.frame.generators;

import com.google.gson.Gson;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.GestureList;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.frame.analyzer.machinelearning.ngram.NGram;
import com.sheffield.leapmotion.frame.analyzer.machinelearning.ngram.NGramModel;
import com.sheffield.leapmotion.frame.generators.gestures.GestureHandler;
import com.sheffield.leapmotion.frame.generators.gestures.NGramGestureHandler;
import com.sheffield.leapmotion.frame.util.BezierHelper;
import com.sheffield.leapmotion.util.FileHandler;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.frame.util.Quaternion;
import com.sheffield.leapmotion.frame.util.QuaternionHelper;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.frame.playback.NGramLog;
import com.sheffield.leapmotion.controller.mocks.HandFactory;
import com.sheffield.leapmotion.controller.mocks.SeededFrame;
import com.sheffield.leapmotion.controller.mocks.SeededHand;
import com.sheffield.output.Csv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class NGramFrameGenerator extends FrameGenerator implements GestureHandler {
	public Csv getCsv() {
		return new Csv();
	}
	protected HashMap<String, SeededHand> joints;

	protected NGram jointNgram;
	
	protected ArrayList<NGramLog> logs;

	protected int currentAnimationTime = 0;
	protected long lastSwitchTime = 0;

	protected ArrayList<SeededHand> seededHands;
	protected ArrayList<String> seededLabels;

	protected SeededHand lastHand;
	protected String lastLabel = "";
	protected File outputFile;

	protected NGram positionNgram;
	protected NGram rotationNgram;
	protected HashMap<String, Vector> positions;
	protected HashMap<String, Quaternion> rotations;

	protected Vector lastPosition;
	protected String lastPositionLabel = "";
	protected ArrayList<Vector> seededPositions;
	protected ArrayList<String> positionLabels;

	protected Quaternion lastRotation;
	protected String lastRotationLabel = "";
	protected ArrayList<Quaternion> seededRotations;
	protected ArrayList<String> rotationLabels;

	protected File outputPosFile;
	protected File outputRotFile;

    protected float modifier = 0f;

    protected NGramGestureHandler nggh;

    public void setOutputFiles(File pos, File rot){
        outputPosFile = pos;
        outputRotFile = rot;
    }

    public void setGestureOutputFile(File file){
        nggh.setOutputFile(file);
    }

	public void setOutputFile(File outputFile){
		this.outputFile = outputFile;
	}
	
	public ArrayList<NGramLog> getLogs(){
		return logs;
	}

	public static HashMap<String, SeededHand> getJoints(String filename) throws IOException {
		HashMap<String, SeededHand> joints = new HashMap<String, SeededHand>();
		String clusterFile = filename;
		if (!Properties.SINGLE_DATA_POOL) {
			clusterFile += "/joint_position_data";
		} else {
			clusterFile += "/hand_joints_data";
		}

		String contents = FileHandler.readFile(new File(clusterFile));
		String[] lines = contents.split("\n");
		for (String line : lines) {
			Frame f = SeededController.newFrame();
			SeededHand hand = HandFactory.createHand(line, f);

			joints.put(hand.getUniqueId(), hand);
			// order.add(hand.getUniqueId());

			HandFactory.injectHandIntoFrame(f, hand);

		}

		return joints;
	}



	public static HashMap<String, Vector> getPositions(String filename) throws IOException {
		HashMap<String, Vector> positions = new HashMap<String, Vector>();

		String positionFile = filename + "/hand_position_data";
		String contents = FileHandler.readFile(new File(positionFile));
		String[] lines = contents.split("\n");
		for (String line : lines) {
			Vector v = new Vector();
			String[] vect = line.split(",");
			v.setX(Float.parseFloat(vect[1]));
			v.setY(Float.parseFloat(vect[2]));
			v.setZ(Float.parseFloat(vect[3]));

			positions.put(vect[0], v);

		}

		return positions;
	}



	public static HashMap<String, Quaternion> getRotations(String filename) throws IOException {
		String rotationFile = filename + "/hand_rotation_data";
		String contents = FileHandler.readFile(new File(rotationFile));
		String[] lines = contents.split("\n");
		HashMap<String, Quaternion> rotations = new HashMap<String, Quaternion>();
		for (String line : lines) {
			String[] vect = line.split(",");
			Quaternion q = new Quaternion(Float.parseFloat(vect[1]),
					Float.parseFloat(vect[2]),
					Float.parseFloat(vect[3]),
					Float.parseFloat(vect[4])).normalise();

			rotations.put(vect[0], q.inverse());

		}
		return rotations;
	}

	public NGramFrameGenerator(String filename) {
		try {
			String rawFile = Properties.DIRECTORY + "/" + filename + "/processed";

			HashMap<String, SeededHand> joints = getJoints(rawFile);
			HashMap<String, Vector> positions = getPositions(rawFile);
			HashMap<String, Quaternion> rotations = getRotations(rawFile);

			String sequenceFile = rawFile + "/joint_position_ngram";

			if (!Properties.SINGLE_DATA_POOL) {
				sequenceFile += "/joint_position_ngram";
			} else {
				sequenceFile += "/hand_joints_ngram";
			}

			String ngString = FileHandler.readFile(new File(sequenceFile));

			Gson gson = new Gson();
			NGram jointNgram = gson.fromJson(ngString, NGram.class);

			jointNgram.calculateProbabilities();

			ngString = rawFile + "/hand_position_ngram";

			NGram positionNgram = gson.fromJson(FileHandler.readFile(new File(ngString)), NGram.class);

			positionNgram.calculateProbabilities();

            ngString = rawFile + "/hand_rotation_ngram";

            NGram rotationNgram = gson.fromJson(FileHandler.readFile(new File(ngString)), NGram.class);

            rotationNgram.calculateProbabilities();


            ngString = rawFile + "/gesture_type_ngram";

            NGram gestureNgram = gson.fromJson(FileHandler.readFile(new File(ngString)), NGram.class);

            gestureNgram.calculateProbabilities();

			setup(jointNgram, positionNgram, rotationNgram, gestureNgram, joints, positions, rotations);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace(App.out);
			System.exit(0);
		}

	}

	public NGramFrameGenerator(NGram jointNgram, NGram positionNgram, NGram rotationNgram, NGram gestureNGram,
							   HashMap<String, SeededHand> joints, HashMap<String, Vector> positions, HashMap<String, Quaternion> rotations) {
		setup(jointNgram, positionNgram, rotationNgram, gestureNGram, joints, positions, rotations);
	}

	public void merge(NGramFrameGenerator ngfs){
		jointNgram.merge(ngfs.jointNgram);
		positionNgram.merge(ngfs.positionNgram);
		rotationNgram.merge(ngfs.rotationNgram);
		nggh.merge(ngfs.nggh);

	}

	private void setup(NGram jointNgram, NGram positionNgram, NGram rotationNgram, NGram gestureNGram,
					   HashMap<String, SeededHand> joints, HashMap<String, Vector> positions, HashMap<String, Quaternion> rotations){
		App.out.println("* Setting up NGramModel Frame Selection");
		lastSwitchTime = 0;
		currentAnimationTime = Properties.SWITCH_TIME;
		logs = new ArrayList<NGramLog>();

		this.joints = joints;
        this.positions = positions;
        this.rotations = rotations;

		this.jointNgram = jointNgram;
		this.jointNgram.calculateProbabilities();

		this.positionNgram = positionNgram;
		this.positionNgram.calculateProbabilities();

		this.rotationNgram = rotationNgram;
		this.rotationNgram.calculateProbabilities();

		this.nggh = new NGramGestureHandler(gestureNGram);


        seededHands = new ArrayList<SeededHand>();
        seededLabels = new ArrayList<String>();

        seededPositions = new ArrayList<Vector>();
        positionLabels = new ArrayList<String>();

        seededRotations = new ArrayList<Quaternion>();
        rotationLabels = new ArrayList<String>();
	}

	@Override
	public Frame newFrame() {
		Frame f = SeededController.newFrame();
		modifier = Math.min(1, currentAnimationTime / (float) Properties.SWITCH_TIME);
		Hand newHand = lastHand.fadeHand(seededHands, modifier);
		f = HandFactory.injectHandIntoFrame(f, newHand);

		return f;
	}

	@Override
	public String status() {
		return null;
	}

	@Override
	public void modifyFrame(SeededFrame frame) {
		Hand h = Hand.invalid();
		for (Hand hand : frame.hands()) {
			h = hand;
		}
		if (h instanceof SeededHand) {
			//float modifier = Math.min(1f, currentAnimationTime / (float)
			//		Properties.SWITCH_TIME);
			SeededHand sh = (SeededHand) h;

			Quaternion q = QuaternionHelper.fadeQuaternions(seededRotations, modifier);

			q.setBasis(sh);
			sh.setOrigin(BezierHelper.bezier(seededPositions, modifier));
		}

	}

	@Override
	public boolean allowProcessing() {
		return true;
	}

	protected long lastUpdate = 0;
	@Override
	public void tick(long time) {
		lastUpdate = time;

		fillLists();

		nggh.tick(time);


		if (currentAnimationTime >= Properties.SWITCH_TIME) {
			// load next frame
			currentAnimationTime = 0;
			lastHand = seededHands.get(seededHands.size() - 1);
			lastLabel = seededLabels.get(seededLabels.size() - 1);
			String handValue = "";

			for (int i = 0; i < seededLabels.size(); i++){
				handValue += seededLabels.get(i) + NGramModel.DELIMITER;
			}

			seededHands.clear();
			seededLabels.clear();

			NGramLog ngLog = new NGramLog();
			ngLog.element = handValue;
			ngLog.timeSeeded = (int) (time - lastSwitchTime);
			logs.add(ngLog);
			if (outputFile != null){
				try {
					FileHandler.appendToFile(outputFile, ngLog.toString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace(App.out);
				}
			}

			NGramLog posLog = new NGramLog();
			posLog.element = "";

			for (String s : positionLabels){
				posLog.element += s + NGramModel.DELIMITER;
			}

			posLog.timeSeeded = (int) (time - lastSwitchTime);

			NGramLog rotLog = new NGramLog();
			rotLog.element = "";

			for (String s : rotationLabels){
				rotLog.element += s + NGramModel.DELIMITER;
			}

			rotLog.timeSeeded = posLog.timeSeeded;
			if (outputPosFile != null){
				try {
					FileHandler.appendToFile(outputPosFile, posLog.toString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace(App.out);
				}
			}

			if (outputRotFile != null){
				try {
					FileHandler.appendToFile(outputRotFile, rotLog.toString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace(App.out);
				}
			}

			lastPosition = seededPositions.get(seededPositions.size()-1);
			lastPositionLabel = positionLabels.get(positionLabels.size()-1);
			lastRotation = seededRotations.get(seededRotations.size()-1);
			lastRotationLabel = rotationLabels.get(rotationLabels.size()-1);

			seededPositions.clear();
			seededRotations.clear();
			positionLabels.clear();
			rotationLabels.clear();
			fillLists();
			lastSwitchTime = time;
		} else {
			currentAnimationTime = (int) (time - lastSwitchTime);
		}

	}

	private String currentSequence = "";
	private String currentSequencePosition = "";
	private String currentSequenceRotation = "";

	public static String getLastLabel(String s){
		int delimSubstring = s.lastIndexOf(NGramModel.DELIMITER)+1;
		if (delimSubstring > 0) {
			s = s.substring(delimSubstring);
		}

		return s;
	}

	public void fillLists(){
		while (lastHand == null){
			currentSequence = jointNgram.babbleNext(currentSequence);
			lastLabel = getLastLabel(currentSequence);
			lastHand = joints.get(lastLabel);

		}
		while (seededHands.size() < com.sheffield.leapmotion.Properties.BEZIER_POINTS){
			if (!seededHands.contains(lastHand)){
				seededHands.clear();
				seededHands.add(0, lastHand);
				seededLabels.clear();
				seededLabels.add(lastLabel);
			} else {

				currentSequence = jointNgram.babbleNext(currentSequence);
				String label = getLastLabel(currentSequence);

				Hand h = joints.get(label);
				if (h != null && h instanceof SeededHand) {
					seededHands.add((SeededHand) h);
					seededLabels.add(label);
				}
			}
		}
//		if (nextHand == null) {
//			nextHand = hands.get(analyzer.getDataAnalyzer().next());
//		}

		while (lastPosition == null){

			currentSequencePosition = positionNgram.babbleNext(currentSequencePosition);
			lastPositionLabel = getLastLabel(currentSequencePosition);

			if (lastPositionLabel != null && !lastPositionLabel.equals("null")){
				lastPosition = positions.get(lastPositionLabel);
			}
		}

		while (lastRotation== null){


			currentSequenceRotation = rotationNgram.babbleNext(currentSequenceRotation);
			lastRotationLabel = getLastLabel(currentSequenceRotation);

			if (lastRotationLabel != null && !lastRotationLabel.equals("null")){
				lastRotation = rotations.get(lastRotationLabel);
			}
		}


		while (seededPositions.size() < com.sheffield.leapmotion.Properties.BEZIER_POINTS){
			if (seededPositions.contains(lastPosition)){
				Vector position = null;
				String pLabel = null;
				while (position == null){

					currentSequencePosition = positionNgram.babbleNext(currentSequencePosition);
					pLabel = getLastLabel(currentSequencePosition);

					if (pLabel != null){
						position = positions.get(pLabel);
						if (position != null) {
							positionLabels.add(pLabel);
							seededPositions.add(position);
						}
					}
				}
			} else {
				seededPositions.add(0, lastPosition);
				positionLabels.add(0, lastPositionLabel);
			}
		}

		while (seededRotations.size() < com.sheffield.leapmotion.Properties.BEZIER_POINTS){
			if (seededRotations.contains(lastRotation)){
				Quaternion rotation = null;
				String rLabel = null;
				while (rotation == null){
					currentSequenceRotation = rotationNgram.babbleNext(currentSequenceRotation);
					rLabel = getLastLabel(currentSequenceRotation);

					if (rLabel != null){
						rotation = rotations.get(rLabel);
						if (rotation != null) {
							rotationLabels.add(rLabel);
							seededRotations.add(rotation);
						}
					}
				}
			} else {
				seededRotations.add(0, lastRotation);
				rotationLabels.add(0, lastRotationLabel);
			}
		}
	}

	public long lastTick(){
		return lastUpdate;
	}

	@Override
	public void cleanUp() {

	}

    @Override
    public GestureList handleFrame(Frame frame) {
        return nggh.handleFrame(frame);
    }
}
