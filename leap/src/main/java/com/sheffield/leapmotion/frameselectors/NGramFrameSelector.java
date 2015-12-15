package com.sheffield.leapmotion.frameselectors;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.FileHandler;
import com.sheffield.leapmotion.analyzer.AnalyzerApp;
import com.sheffield.leapmotion.mocks.HandFactory;
import com.sheffield.leapmotion.mocks.SeededHand;
import com.sheffield.instrumenter.Properties;
import com.sheffield.leapmotion.controller.SeededController;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class NGramFrameSelector extends FrameSelector {
	private HashMap<String, SeededHand> hands;

	private int currentFrame;
	private int currentAnimationTime = 0;
	private long lastSwitchTime = 0;
	private SeededHand lastHand;
	private SeededHand nextHand;
	private AnalyzerApp analyzer;

	public NGramFrameSelector(String filename) {
		try {
			App.out.println("* Setting up NGram Frame Selection");
			lastSwitchTime = System.currentTimeMillis();
			currentAnimationTime = Properties.SWITCH_TIME;
			String clusterFile = Properties.DIRECTORY + "/" + filename + ".handcluster";
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

			String sequenceFile = Properties.DIRECTORY  + "/" + filename + ".handdata";
			analyzer = new AnalyzerApp(sequenceFile);
			analyzer.analyze();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(App.out);
		}

	}

	@Override
	public Frame newFrame() {
		if (nextHand == null) {
			nextHand = hands.get(analyzer.getDataAnalyzer().nextHand());
		}

		if (currentAnimationTime >= Properties.SWITCH_TIME) {
			// load next frame
			currentAnimationTime -= Properties.SWITCH_TIME;
			lastHand = nextHand;
			do {

				nextHand = hands.get(analyzer.getDataAnalyzer().nextHand());
			} while (nextHand == null || nextHand.fingers() == null);
			lastSwitchTime = System.currentTimeMillis();
		}
		currentAnimationTime = (int) (System.currentTimeMillis() - lastSwitchTime);
		Frame f = SeededController.newFrame();
		float modifier = Math.min(1, currentAnimationTime / (float) Properties.SWITCH_TIME);
		Hand newHand = lastHand.fadeHand(nextHand, modifier);
		f = HandFactory.injectHandIntoFrame(f, newHand);

		return f;
	}
}
