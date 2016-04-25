package com.sheffield.leapmotion.frameselectors;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Vector;
import com.sheffield.instrumenter.Properties;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.framemodifier.FrameModifier;
import com.sheffield.leapmotion.mocks.HandFactory;
import com.sheffield.leapmotion.mocks.SeededFrame;
import com.sheffield.leapmotion.mocks.SeededHand;

import java.util.Random;

public class RandomFrameSelector extends FrameSelector implements FrameModifier {
	private int currentAnimationTime = 0;
	private long lastSwitchTime = 0;
	private SeededHand lastHand;
	private SeededHand nextHand;
	private int switchTime = 0;
	private int handId = 0;
	private Vector newPosition;
	private Vector[] newRotation;
	private Vector lastPosition;
	private Vector[] lastRotation;
	private Random random = new Random();

	public RandomFrameSelector() {
		lastSwitchTime = System.currentTimeMillis();
		currentAnimationTime = Properties.SWITCH_TIME;
		String clusterFile = Properties.DIRECTORY + "/processed/centers100";

	}

	@Override
	public Frame newFrame() {
		//SeededController.getSeededController().setGestureHandler(new RandomGestureHandler());
		Frame f = SeededController.newFrame();
		if (nextHand == null) {
			nextHand = HandFactory.createRandomHand(f, "hand" + handId++);
		}

		if (currentAnimationTime >= switchTime) {
			// load next frame
			switchTime = (int) (Properties.SWITCH_TIME * (Math.random() * 2));
			currentAnimationTime -= Properties.SWITCH_TIME;
			lastHand = nextHand;
			nextHand = HandFactory.createRandomHand(f, "hand" + handId++);
			lastSwitchTime = System.currentTimeMillis();
		}
		currentAnimationTime = (int) (System.currentTimeMillis() - lastSwitchTime);
		float modifier = Math.min(1, currentAnimationTime / (float) switchTime);
		Hand newHand = lastHand.fadeHand(nextHand, modifier);
		f = HandFactory.injectHandIntoFrame(f, newHand);
		return f;
	}

	private Vector randomVector(float scale, float transpose){
		return new Vector(transpose + (random.nextFloat()*scale),
				transpose + (random.nextFloat()*scale),
				transpose + (random.nextFloat()*scale));
	}

	@Override
	public void modifyFrame(SeededFrame frame) {
		if (newPosition == null) {
			newPosition = randomVector(400, -200);
			lastPosition = newPosition;
		}
		if (newRotation == null){
			newRotation = new Vector[3];
			newRotation[0] = randomVector(1, 0);
			newRotation[1] = randomVector(1, 0);
			newRotation[2] = randomVector(1, 0);
			lastRotation = newRotation;
		}
		if (currentAnimationTime >= switchTime) {
			lastPosition = newPosition;
			newPosition = randomVector(400, -200);
			lastRotation = newRotation;
			newRotation = new Vector[3];
			newRotation[0] = randomVector(1, 0);
			newRotation[1] = randomVector(1, 0);
			newRotation[2] = randomVector(1, 0);
			lastSwitchTime = System.currentTimeMillis();

		}
		Hand h = Hand.invalid();
		for (Hand hand : frame.hands()) {
			if (hand instanceof SeededHand)
				h = hand;
		}

		if (h instanceof SeededHand) {
			float modifier = currentAnimationTime / (float) Properties.SWITCH_TIME;
			SeededHand sh = (SeededHand) h;
			sh.setBasis(fadeVector(lastRotation[0], newRotation[0], modifier),
					fadeVector(lastRotation[1], newRotation[1], modifier),
					fadeVector(lastRotation[2], newRotation[2], modifier));
			sh.setOrigin(fadeVector(lastPosition, newPosition, modifier));
		}
		currentAnimationTime = (int) (System.currentTimeMillis() - lastSwitchTime);
	}

	public Vector fadeVector(Vector prev, Vector next, float modifier){
		return prev.plus(next.minus(prev).times(modifier));
	}
}
