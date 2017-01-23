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
import com.sheffield.leapmotion.frame.analyzer.AnalyzerApp;
import com.sheffield.leapmotion.frame.analyzer.ProbabilityListener;
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

public class NGramFrameGenerator extends FrameGenerator {
	public Csv getCsv() {
		return new Csv();
	}
	protected HashMap<String, SeededHand> hands;
	
	protected ArrayList<NGramLog> logs;

	protected int currentAnimationTime = 0;
	protected long lastSwitchTime = 0;

	protected ArrayList<SeededHand> seededHands;
	protected ArrayList<String> seededLabels;

	protected SeededHand lastHand;
	protected String lastLabel;
	protected AnalyzerApp analyzer;
	protected File outputFile;

	protected AnalyzerApp positionAnalyzer;
	protected AnalyzerApp rotationAnalyzer;
	protected HashMap<String, Vector> vectors;
	protected HashMap<String, Quaternion> rotations;

	protected Vector lastPosition;
	protected String lastPositionLabel;
	protected ArrayList<Vector> seededPositions = new ArrayList<Vector>();
	protected ArrayList<String> positionLabels = new ArrayList<String>();

	protected Quaternion lastRotation;
	protected String lastRotationLabel;
	protected ArrayList<Quaternion> seededRotations = new ArrayList<Quaternion>();
	protected ArrayList<String> rotationLabels = new ArrayList<String>();

	protected File outputPosFile;
	protected File outputRotFile;

	public NGramFrameGenerator(){

	}

    public void setOutputFiles(File pos, File rot){
        outputPosFile = pos;
        outputRotFile = rot;
    }

    public void addPositionProbabilityListener(ProbabilityListener pbl){
        positionAnalyzer.addProbabilityListener(pbl);
    }

    public void addRotationProbabilityListener(ProbabilityListener pbl){
        rotationAnalyzer.addProbabilityListener(pbl);
    }
	
	public void setOutputFile(File outputFile){
		this.outputFile = outputFile;
	}
	
	public ArrayList<NGramLog> getLogs(){
		return logs;
	}

	public void addProbabilityListener(ProbabilityListener pbl){
		analyzer.addProbabilityListener(pbl);
	}

	public NGramFrameGenerator(String filename) {
		try {
			App.out.println("* Setting up NGram Frame Selection");
			lastSwitchTime = 0;
			currentAnimationTime = Properties.SWITCH_TIME;
			logs = new ArrayList<NGramLog>();
			String clusterFile = Properties.DIRECTORY + "/" + filename + ".joint_position_data";
			hands = new HashMap<String, SeededHand>();

			seededHands = new ArrayList<SeededHand>();
			seededLabels = new ArrayList<String>();

			String contents = FileHandler.readFile(new File(clusterFile));
			String[] lines = contents.split("\n");
			for (String line : lines) {
				Frame f = SeededController.newFrame();
				SeededHand hand = HandFactory.createHand(line, f);

				hands.put(hand.getUniqueId(), hand);
				// order.add(hand.getUniqueId());

				HandFactory.injectHandIntoFrame(f, hand);

			}

			String sequenceFile = Properties.DIRECTORY  + "/" + filename + ".joint_position_ngram";
			analyzer = new AnalyzerApp(sequenceFile);
			analyzer.analyze();

			String positionFile = Properties.DIRECTORY + "/" + filename + ".hand_position_data";
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

			sequenceFile = Properties.DIRECTORY + "/" + filename + ".hand_position_ngram";
			positionAnalyzer = new AnalyzerApp(sequenceFile);
			positionAnalyzer.analyze();

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

			sequenceFile = Properties.DIRECTORY + "/" + filename + ".hand_rotation_ngram";
			rotationAnalyzer = new AnalyzerApp(sequenceFile);
			rotationAnalyzer.analyze();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace(App.out);
			System.exit(0);
		}

	}

	private float modifier = 0f;

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


		if (currentAnimationTime >= Properties.SWITCH_TIME) {
			// load next frame
			currentAnimationTime = 0;
			lastHand = seededHands.get(seededHands.size() - 1);
			lastLabel = seededLabels.get(seededLabels.size() - 1);
			String handValue = "";

			for (int i = 0; i < seededLabels.size(); i++){
				handValue += seededLabels.get(i) + ",";
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
				posLog.element += s + ",";
			}

			posLog.timeSeeded = (int) (time - lastSwitchTime);

			NGramLog rotLog = new NGramLog();
			rotLog.element = "";

			for (String s : rotationLabels){
				rotLog.element += s + ",";
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

	public void fillLists(){
		while (lastHand == null){
			lastLabel = analyzer.getDataAnalyzer().next();
			lastHand = hands.get(lastLabel);
		}
		while (seededHands.size() < com.sheffield.leapmotion.Properties.BEZIER_POINTS){
			if (!seededHands.contains(lastHand)){
				seededHands.clear();
				seededHands.add(0, lastHand);
				seededLabels.clear();
				seededLabels.add(lastLabel);
			} else {
				//skip ahead in N-Gram
				for (int i = 0; i < Properties.NGRAM_SKIP; i++){
					analyzer.getDataAnalyzer().next();
				}
				String label = analyzer.getDataAnalyzer().next();
				Hand h = hands.get(label);
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
			for (int i = 0; i < Properties.NGRAM_SKIP; i++) {
				positionAnalyzer.getDataAnalyzer().next();
			}
			lastPositionLabel = positionAnalyzer.getDataAnalyzer().next();
			if (lastPositionLabel != null && !lastPositionLabel.equals("null")){
				lastPosition = vectors.get(lastPositionLabel);
			}
		}

		while (lastRotation== null){
			for (int i = 0; i < Properties.NGRAM_SKIP; i++) {
				rotationAnalyzer.getDataAnalyzer().next();			}
			lastRotationLabel = rotationAnalyzer.getDataAnalyzer().next();
			if (lastRotationLabel != null && !lastRotationLabel.equals("null")){
				lastRotation = rotations.get(lastRotationLabel);
			}
		}


		while (seededPositions.size() < com.sheffield.leapmotion.Properties.BEZIER_POINTS){
			if (seededPositions.contains(lastPosition)){
				Vector position = null;
				String pLabel = null;
				while (position == null){
					pLabel = positionAnalyzer.getDataAnalyzer().next();

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
					rLabel = rotationAnalyzer.getDataAnalyzer().next();

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
}
