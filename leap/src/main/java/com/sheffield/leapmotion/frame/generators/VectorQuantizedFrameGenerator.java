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
	private long lastSwitchTime = 0;

	private SeededHand lastHand;
	private String lastLabel = null;
	private Random random = new Random();

	private HashMap<String, Vector> vectors;
	private HashMap<String, Quaternion> rotations;

	private Vector lastPosition;
	private String lastPositionLabel;

	private Quaternion lastRotation;
	private String lastRotationLabel;

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

			App.out.println("* Setting up VQ Frame Selector");

			String clusterFile = Properties.DIRECTORY + "/" + filename + "/processed/" +
					(Properties.SINGLE_DATA_POOL ? "hand_joints_data" : "joint_position_data");

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

			Properties.CLUSTERS = this.hands.size();

			String positionFile = Properties.DIRECTORY + "/" + filename + "/processed/hand_position_data";
			lastSwitchTime = System.currentTimeMillis();

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

			String rotationFile = Properties.DIRECTORY + "/" + filename + "/processed/hand_rotation_data";
			contents = FileHandler.readFile(new File(rotationFile));
			lines = contents.split("\n");
			rotations = new HashMap<String, Quaternion>();
			for (String line : lines) {
				String[] vect = line.split(",");
				Quaternion q = new Quaternion(Float.parseFloat(vect[1]),
						Float.parseFloat(vect[2]),
						Float.parseFloat(vect[3]),
						Float.parseFloat(vect[4])).normalise();

				rotations.put(vect[0], q);

				//App.out.println(vect[0] + ": " + q);

			}
		} catch (IOException e) {
			e.printStackTrace(App.out);
		}

	}

	private String[] candidateHands = null;
	private String[] candidatePositions = null;
	private String[] candidateRotations = null;

	public String randomHand(){
		if (candidateHands == null){
			candidateHands = new String[hands.keySet().size()];
			hands.keySet().toArray(candidateHands);
		}
		return candidateHands[random.nextInt(hands.keySet().size())];
	}

	public String randomPosition(){
		if (candidatePositions == null){
			candidatePositions = new String[vectors.keySet().size()];
			vectors.keySet().toArray(candidatePositions);
		}
		return candidatePositions[random.nextInt(vectors.keySet().size())];
	}

	public String randomRotation(){
		if (candidateRotations == null){
			candidateRotations = new String[rotations.keySet().size()];
			rotations.keySet().toArray(candidateRotations);
		}
		return candidateRotations[random.nextInt(rotations.keySet().size())];
	}

	@Override
	public Frame newFrame() {

		Frame f = SeededController.newFrame();
		Hand newHand = lastHand.copy();
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
			SeededHand sh = ((SeededHand) h);

			lastRotation.setBasis(sh);

			sh.setOrigin(lastPosition);
		}

	}

	@Override
	public boolean allowProcessing() {
		return true;
	}

	@Override
	public String getName() {
		return "Random Clusters Generation";
	}

	private long lastUpdate = 0;
	@Override
	public void tick(long time) {

		lastLabel = randomHand();
		lastHand = hands.get(lastLabel);

		lastPositionLabel =  randomPosition();
		lastPosition = vectors.get(lastPositionLabel);

		lastRotationLabel = randomRotation();
		lastRotation = rotations.get(lastPositionLabel);

		lastUpdate = time;
	}

	public long lastTick(){
		return lastUpdate;
	}

	@Override
	public void cleanUp() {

	}
}
