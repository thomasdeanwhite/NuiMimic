package com.sheffield.leapmotion.frame.generators;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.App;
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
import java.util.Random;

public class VectorQuantizedFrameGenerator extends FrameGenerator {
	@Override
	public Csv getCsv() {
		return new Csv();
	}

	private HashMap<String, SeededHand> hands;

	private int currentAnimationTime = 0;
	private long lastSwitchTime = 0;

	private SeededHand lastHand;
	private String lastLabel = null;
	private ArrayList<SeededHand> seededHands;
	private ArrayList<String> seededLabels;

	private Random random = new Random();

	private HashMap<String, Vector> vectors;
	private HashMap<String, Quaternion> rotations;

	private Vector lastPosition;
	private String lastPositionLabel;
	private ArrayList<Vector> seededPositions = new ArrayList<Vector>();
	private ArrayList<String> positionLabels = new ArrayList<String>();

	private Quaternion lastRotation;
	private String lastRotationLabel;
	private ArrayList<Quaternion> seededRotations = new ArrayList<Quaternion>();
	private ArrayList<String> rotationLabels = new ArrayList<String>();

	File pFile, rFile, jFile;

	public File generateFile(String filename){
		return FileHandler.generateTestingOutputFile(filename);
	}

	public VectorQuantizedFrameGenerator(String filename) {
		try {
			long testIndex = Properties.CURRENT_RUN;

			pFile = generateFile("hand_positions-" + testIndex);
			pFile.createNewFile();

			rFile = generateFile("hand_rotations-" + testIndex);
			rFile.createNewFile();

			jFile = generateFile("joint_positions-" + testIndex);
			jFile.createNewFile();

			seededHands = new ArrayList<SeededHand>();
			seededLabels = new ArrayList<String>();
			//SeededController.getSeededController().setGestureHandler(new RandomGestureHandler());
			App.out.println("* Setting up VQ Frame Selector");
			lastSwitchTime = System.currentTimeMillis();
			currentAnimationTime = Properties.SWITCH_TIME;
			String clusterFile = Properties.DIRECTORY + "/" + filename + ".joint_position_data";
			hands = new HashMap<String, SeededHand>();

			String contents = FileHandler.readFile(new File(clusterFile));
			String[] lines = contents.split("\n");
			for (String line : lines) {
				Frame f = SeededController.newFrame();
				SeededHand hand = HandFactory.createHand(line, f);

				hands.put(hand.getUniqueId(), hand);
				// order.add(hand.getUniqueId());

				HandFactory.injectHandIntoFrame(f, hand);

			}

			String positionFile = Properties.DIRECTORY + "/" + filename + ".hand_position_data";
			lastSwitchTime = System.currentTimeMillis();
			currentAnimationTime = Properties.SWITCH_TIME;
			contents = FileHandler.readFile(new File(positionFile));
			lines = contents.split("\n");
			vectors = new HashMap<String, Vector>();
			for (String line : lines) {
				Vector v = new Vector();
				String[] vect = line.split(",");
				v.setX(Float.parseFloat(vect[1]));
				v.setY(Float.parseFloat(vect[2]));
				v.setZ(Float.parseFloat(vect[3]));

				vectors.put(vect[0], v);

			}

			String rotationFile = Properties.DIRECTORY + "/" + filename + ".hand_rotation_data";
			contents = FileHandler.readFile(new File(rotationFile));
			lines = contents.split("\n");
			rotations = new HashMap<String, Quaternion>();
			for (String line : lines) {
				String[] vect = line.split(",");
				Quaternion q = new Quaternion(Float.parseFloat(vect[1]),
						Float.parseFloat(vect[2]),
						Float.parseFloat(vect[3]),
						Float.parseFloat(vect[4])).normalise();

				rotations.put(vect[0], q.inverse());

				//App.out.println(vect[0] + ": " + q);

			}
		} catch (IOException e) {
			e.printStackTrace(App.out);
		}

	}

	public String randomHand(){
		return (String) hands.keySet().toArray()[random.nextInt(hands.keySet().size())];
	}

	public String randomPosition(){
		return (String) vectors.keySet().toArray()[random.nextInt(vectors.keySet().size())];
	}

	public String randomRotation(){
		return (String) rotations.keySet().toArray()[random.nextInt(rotations.keySet().size())];
	}

	@Override
	public Frame newFrame() {
		while (lastHand == null){
			lastLabel = randomHand();
			lastHand = hands.get(lastLabel);
		}
		while (seededHands.size() < com.sheffield.leapmotion.Properties.BEZIER_POINTS){
			if (!seededHands.contains(lastHand)){
				seededHands.clear();
				seededHands.add(0, lastHand);
				seededLabels.clear();
				seededLabels.add(lastLabel);
			} else {
				String label = randomHand();
				Hand h = hands.get(label);
				if (h != null && h instanceof SeededHand) {
					seededHands.add((SeededHand) h);
					seededLabels.add(label);
				}
			}
		}

		System.out.println("HELLO");

		if (currentAnimationTime >= Properties.SWITCH_TIME) {
			// load next frame

			try {
				NGramLog posLog = new NGramLog();
				posLog.element = "";
				currentAnimationTime = 0;

				for (String s : positionLabels){
					posLog.element += s + ",";
				}

				posLog.timeSeeded = (int) (System.currentTimeMillis() - lastSwitchTime);

				NGramLog rotLog = new NGramLog();
				rotLog.element = "";

				for (String s : rotationLabels){
					rotLog.element += s + ",";
				}

				rotLog.timeSeeded = posLog.timeSeeded;

				String handValue = "";

				for (int i = 0; i < seededLabels.size(); i++){
					handValue += seededLabels.get(i) + ",";
				}

				NGramLog ngLog = new NGramLog();
				ngLog.element = handValue;
				ngLog.timeSeeded = (int) (System.currentTimeMillis() - lastSwitchTime);
				FileHandler.appendToFile(pFile, posLog.toString());
				FileHandler.appendToFile(rFile, rotLog.toString());
				FileHandler.appendToFile(jFile, ngLog.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}

			lastHand = seededHands.get(seededHands.size() - 1);
			lastLabel = seededLabels.get(seededLabels.size() - 1);
			seededHands.clear();
			seededLabels.clear();

			if (seededPositions.size() > 0 &&
					seededRotations.size() > 0) {

				lastPosition = seededPositions.get(seededPositions.size() - 1);
				lastPositionLabel = positionLabels.get(positionLabels.size() - 1);
				lastRotation = seededRotations.get(seededRotations.size() - 1);
				lastRotationLabel = rotationLabels.get(rotationLabels.size() - 1);

			}

			seededPositions.clear();
			seededRotations.clear();
			positionLabels.clear();
			rotationLabels.clear();

			currentAnimationTime = 0;
			lastSwitchTime = System.currentTimeMillis();
			return newFrame();
		}
		currentAnimationTime = (int) (System.currentTimeMillis() - lastSwitchTime);
		Frame f = SeededController.newFrame();
		float modifier = Math.min(1, currentAnimationTime / (float) Properties.SWITCH_TIME);
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
		while (lastPosition == null){
			lastPositionLabel =  randomPosition();
			if (lastPositionLabel != null && !lastPositionLabel.equals("null")){
				lastPosition = vectors.get(lastPositionLabel);
			}
		}

		while (lastRotation== null){
			lastRotationLabel = randomRotation();
			if (lastRotationLabel != null && !lastRotationLabel.equals("null")){
				lastRotation = rotations.get(lastRotationLabel);
			}
		}


		while (seededPositions.size() < com.sheffield.leapmotion.Properties.BEZIER_POINTS){
			if (seededPositions.contains(lastPosition)){
				Vector position = null;
				String pLabel = null;
				while (position == null){
					pLabel = randomPosition();

					if (pLabel != null){
						position = vectors.get(pLabel);
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
					rLabel = randomRotation();

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

		Hand h = Hand.invalid();
		for (Hand hand : frame.hands()) {
			h = hand;
		}
		if (h instanceof SeededHand) {
			float modifier = currentAnimationTime / (float) Properties.SWITCH_TIME;
			SeededHand sh = (SeededHand) h;

			Quaternion q = QuaternionHelper.fadeQuaternions(seededRotations, modifier);

			q.setBasis(sh);

			sh.setOrigin(BezierHelper.bezier(seededPositions, modifier));
		}
		currentAnimationTime = (int) (System.currentTimeMillis() - lastSwitchTime);
	}

	@Override
	public boolean allowProcessing() {
		return true;
	}

	public Vector fadeVector(Vector prev, Vector next, float modifier){
		return prev.plus(next.minus(prev).times(modifier));
	}

	private long lastUpdate = 0;
	@Override
	public void tick(long time) {
		lastUpdate = time;
	}

	public long lastTick(){
		return lastUpdate;
	}

	@Override
	public void cleanUp() {

	}
}
