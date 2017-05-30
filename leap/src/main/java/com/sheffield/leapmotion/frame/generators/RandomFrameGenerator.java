package com.sheffield.leapmotion.frame.generators;

import com.leapmotion.leap.*;
import com.sheffield.leapmotion.frame.generators.gestures.RandomGestureHandler;
import com.sheffield.leapmotion.frame.util.BezierHelper;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.frame.util.Quaternion;
import com.sheffield.leapmotion.frame.util.QuaternionHelper;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.controller.mocks.HandFactory;
import com.sheffield.leapmotion.controller.mocks.SeededFrame;
import com.sheffield.leapmotion.controller.mocks.SeededHand;
import com.sheffield.output.Csv;

import java.util.ArrayList;
import java.util.Random;

public class RandomFrameGenerator extends FrameGenerator {

	private RandomGestureHandler rgh = new RandomGestureHandler();

	@Override
	public GestureList handleFrame(Frame frame, Controller controller) {
		return rgh.handleFrame(frame, controller);
	}


	@Override
	public Csv getCsv() {
		return new Csv();
	}

	private int currentAnimationTime = 0;
	private long lastSwitchTime = 0;
	private int handId = 0;

	private SeededHand lastHand;
	private ArrayList<SeededHand> seededHands = new ArrayList<SeededHand>();

	private Random random = new Random();

	private Vector lastPosition;
	private ArrayList<Vector> seededPositions = new ArrayList<Vector>();

	private Vector[] lastRotation;
	private ArrayList<Vector[]> seededRotations = new ArrayList<Vector[]>();

	public RandomFrameGenerator() {
		lastSwitchTime = System.currentTimeMillis();
		currentAnimationTime = 0;
	}

	@Override
	public Frame newFrame() {
		//SeededController.getSeededController().setGestureHandler(new RandomGestureHandler());
		Frame f = SeededController.newFrame();
		while (lastHand == null){
			lastHand = HandFactory.createRandomHand(f, "hand" + handId++);
		}
		while (seededHands.size() < com.sheffield.leapmotion.Properties.BEZIER_POINTS){
			if (!seededHands.contains(lastHand)){
				seededHands.clear();
				seededHands.add(0, lastHand);
			} else {
				Hand h = HandFactory.createRandomHand(f, "hand" + handId++);
				if (h != null && h instanceof SeededHand) {
					seededHands.add((SeededHand) h);
				}
			}
		}
		if (currentAnimationTime >= Properties.SWITCH_TIME) {
			currentAnimationTime = 0;
			lastHand = seededHands.get(seededHands.size() - 1);
			seededHands.clear();

			if (seededPositions.size() > 0 && seededRotations.size() > 0) {
				lastPosition = seededPositions.get(seededPositions.size() - 1);
				lastRotation = seededRotations.get(seededRotations.size() - 1);
			}

			seededPositions.clear();
			seededRotations.clear();

			lastSwitchTime = System.currentTimeMillis();
			return newFrame();
		}
		currentAnimationTime = (int) (System.currentTimeMillis() - lastSwitchTime);
		float modifier = Math.min(1, currentAnimationTime / (float) Properties.SWITCH_TIME);
		Hand newHand = lastHand.fadeHand(seededHands, modifier);
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
	public void modifyFrame(SeededFrame frame) {

	}

	@Override
	public boolean allowProcessing() {
		return true;
	}

	@Override
	public String getName() {
		return "Purely Random Generation";
	}

	@Override
	public void cleanUp() {

	}
}
