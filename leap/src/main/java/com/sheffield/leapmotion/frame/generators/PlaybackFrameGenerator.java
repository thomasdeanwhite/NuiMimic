package com.sheffield.leapmotion.frame.generators;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;import com.sheffield.leapmotion.util.FileHandler;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.controller.mocks.HandFactory;
import com.sheffield.leapmotion.controller.mocks.SeededFrame;
import com.sheffield.leapmotion.controller.mocks.SeededHand;
import com.sheffield.output.Csv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class PlaybackFrameGenerator extends FrameGenerator {
	private HashMap<String, SeededHand> hands;
	public Csv getCsv() {
		return new Csv();
	}

	@Override
	public void modifyFrame(SeededFrame frame) {

	}

	@Override
	public boolean allowProcessing() {
		return true;
	}

	private int currentFrame;
	private String[] frameOrder;
	private int currentAnimationTime = 0;
	private long lastSwitchTime = 0;
	private SeededHand lastHand;
	private SeededHand nextHand;

	public PlaybackFrameGenerator() {
		try {
			lastSwitchTime = System.currentTimeMillis();
			currentAnimationTime = Properties.SWITCH_TIME;
			String clusterFile = Properties.DIRECTORY + "/processed/3centers100";
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

			String sequenceFile = Properties.DIRECTORY + "/processed/3ng100.seq";
			String[] sequence = FileHandler.readFile(new File(sequenceFile)).split(" ");

			ArrayList<String> order = new ArrayList<String>();

			for (String s : sequence) {
				// if (hands.get(s) != null && (order.isEmpty() ||
				// !order.get(order.size()-1).equals(s)))
				order.add(s);

			}

			frameOrder = new String[order.size()];
			order.toArray(frameOrder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public Frame newFrame() {
		if (currentFrame >= frameOrder.length) {
			currentFrame = 0;
		}

		if (nextHand == null) {
			nextHand = hands.get(frameOrder[currentFrame++]);
		}

		if (currentAnimationTime >= Properties.SWITCH_TIME) {
			// load next frame
			currentAnimationTime -= Properties.SWITCH_TIME;
			lastHand = nextHand;
			nextHand = hands.get(frameOrder[currentFrame++]);
			lastSwitchTime = System.currentTimeMillis();
		}
		currentAnimationTime = (int) (System.currentTimeMillis() - lastSwitchTime);
		Frame f = SeededController.newFrame();
		float modifier = Math.min(1, currentAnimationTime / (float) Properties.SWITCH_TIME);
		Hand newHand = lastHand.fadeHand(nextHand, modifier);
		f = HandFactory.injectHandIntoFrame(f, newHand);
		return f;
	}

	@Override
	public String status() {
		return null;
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
