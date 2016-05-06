package com.sheffield.leapmotion.frameselectors;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.sheffield.instrumenter.Properties;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.FileHandler;
import com.sheffield.leapmotion.analyzer.AnalyzerApp;
import com.sheffield.leapmotion.analyzer.ProbabilityListener;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.mocks.HandFactory;
import com.sheffield.leapmotion.mocks.SeededHand;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class NGramFrameSelector extends FrameSelector {
	
	private HashMap<String, SeededHand> hands;
	
	private ArrayList<NGramLog> logs;

	private int currentAnimationTime = 0;
	private long lastSwitchTime = 0;

	private ArrayList<SeededHand> seededHands;
	private ArrayList<String> seededLabels;

	private SeededHand lastHand;
	private String lastLabel;
	private AnalyzerApp analyzer;
	private File outputFile;
	
	public void setOutputFile(File outputFile){
		this.outputFile = outputFile;
	}
	
	public ArrayList<NGramLog> getLogs(){
		return logs;
	}

	public void addProbabilityListener(ProbabilityListener pbl){
		analyzer.addProbabilityListener(pbl);
	}

	public NGramFrameSelector(String filename) {
		try {
			App.out.println("* Setting up NGram Frame Selection");
			lastSwitchTime = System.currentTimeMillis();
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace(App.out);
			System.exit(0);
		}

	}

	@Override
	public Frame newFrame() {
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
			ngLog.timeSeeded = (int) (System.currentTimeMillis() - lastSwitchTime);
			logs.add(ngLog);
			if (outputFile != null){
				try {
					FileHandler.appendToFile(outputFile, ngLog.toString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace(App.out);
				}
			}
			lastSwitchTime = System.currentTimeMillis();
			return newFrame();
		} else {
			currentAnimationTime = (int) (System.currentTimeMillis() - lastSwitchTime);
		}
		Frame f = SeededController.newFrame();
		float modifier = Math.min(1, currentAnimationTime / (float) Properties.SWITCH_TIME);
		Hand newHand = lastHand.fadeHand(seededHands, modifier);
		f = HandFactory.injectHandIntoFrame(f, newHand);

		return f;
	}
}
