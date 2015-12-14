package com.sheffield.leapmotion.tester.frameselectors;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.sheffield.leapmotion.mocks.HandFactory;
import com.sheffield.leapmotion.mocks.SeededHand;
import com.sheffield.leapmotion.tester.FileHandler;
import com.sheffield.leapmotion.tester.Properties;
import com.sheffield.leapmotion.tester.controller.SeededController;

public class RandomFrameSelector extends FrameSelector {
	private ArrayList<SeededHand> hands;
	Random random = new Random();
	private int currentAnimationTime = 0;
	private long lastSwitchTime = 0;
	private SeededHand lastHand;
	private SeededHand nextHand;
	private int switchTime = 0;

	public RandomFrameSelector() {
		lastSwitchTime = System.currentTimeMillis();
		currentAnimationTime = Properties.SWITCH_TIME;
		String clusterFile = Properties.DIRECTORY + "/processed/centers100";
		hands = new ArrayList<SeededHand>();

		String contents;
		try {
			contents = FileHandler.readFile(new File(clusterFile));
			String[] lines = contents.split("\n");
			for (String line : lines) {
				Frame f = SeededController.newFrame();
				SeededHand hand = HandFactory.createHand(line, f);

				hands.add(hand);
				// order.add(hand.getUniqueId());

				HandFactory.injectHandIntoFrame(f, hand);

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public Frame newFrame() {

		if (nextHand == null) {
			nextHand = hands.get(random.nextInt(hands.size()));
		}

		if (currentAnimationTime >= switchTime) {
			// load next frame
			switchTime = (int) (Properties.SWITCH_TIME * (Math.random() * 2));
			currentAnimationTime -= Properties.SWITCH_TIME;
			lastHand = nextHand;
			nextHand = hands.get(random.nextInt(hands.size()));
			lastSwitchTime = System.currentTimeMillis();
		}
		currentAnimationTime = (int) (System.currentTimeMillis() - lastSwitchTime);
		Frame f = SeededController.newFrame();
		float modifier = Math.min(1, currentAnimationTime / (float) switchTime);
		Hand newHand = lastHand.fadeHand(nextHand, modifier);
		f = HandFactory.injectHandIntoFrame(f, newHand);
		return f;
	}
}
