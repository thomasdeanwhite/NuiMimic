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

	private int currentFrame;
	private int currentAnimationTime = 0;
	private long lastSwitchTime = 0;
	private SeededHand lastHand;
	private SeededHand nextHand;
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
		if (nextHand == null) {
			nextHand = hands.get(analyzer.getDataAnalyzer().next());
		}

		if (currentAnimationTime >= Properties.SWITCH_TIME) {
			// load next frame
			currentAnimationTime -= Properties.SWITCH_TIME;
			lastHand = nextHand;
			String handValue = "";
			do {
				handValue = analyzer.getDataAnalyzer().next();
				nextHand = hands.get(handValue);
			} while (nextHand == null || nextHand.fingers() == null);
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
		} else {
			currentAnimationTime = (int) (System.currentTimeMillis() - lastSwitchTime);
		}
		Frame f = SeededController.newFrame();
		float modifier = Math.min(1, currentAnimationTime / (float) Properties.SWITCH_TIME);
		Hand newHand = lastHand.fadeHand(nextHand, modifier);
		f = HandFactory.injectHandIntoFrame(f, newHand);

		return f;
	}
}
