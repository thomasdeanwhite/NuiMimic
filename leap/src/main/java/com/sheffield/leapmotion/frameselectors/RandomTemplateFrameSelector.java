package com.sheffield.leapmotion.frameselectors;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Vector;
import com.sheffield.instrumenter.Properties;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.FileHandler;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.controller.gestures.RandomGestureHandler;
import com.sheffield.leapmotion.framemodifier.FrameModifier;
import com.sheffield.leapmotion.mocks.HandFactory;
import com.sheffield.leapmotion.mocks.SeededFrame;
import com.sheffield.leapmotion.mocks.SeededHand;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class RandomTemplateFrameSelector extends FrameSelector implements FrameModifier {
	private HashMap<String, SeededHand> hands;

	private int currentAnimationTime = 0;
	private long lastSwitchTime = 0;
	private SeededHand lastHand;
	private SeededHand nextHand;
	private Random random = new Random();

	private HashMap<String, Vector> vectors;
	private HashMap<String, Vector[]> rotations;
	private Vector lastPosition;
	private Vector newPosition;

	private Vector[] lastRotation;
	private Vector[] newRotation;

	public RandomTemplateFrameSelector(String filename) {
		try {
			SeededController.getSeededController().setGestureHandler(new RandomGestureHandler());
			App.out.println("* Setting up NGram Frame Selection");
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
			rotations = new HashMap<String, Vector[]>();
			for (String line : lines) {
				String[] vect = line.split(",");
				Vector[] vs = new Vector[3];
				for (int i = 0; i < 3; i++) {
					Vector v = new Vector();
					int index = (i*3)+1;
					v.setX(Float.parseFloat(vect[index]));
					v.setY(Float.parseFloat(vect[index+1]));
					v.setZ(Float.parseFloat(vect[index+2]));
					vs[i] = v;
				}

				rotations.put(vect[0], vs);

			}
		} catch (IOException e) {
			e.printStackTrace(App.out);
		}

	}

	@Override
	public Frame newFrame() {
		if (nextHand == null) {
			nextHand = hands.get(hands.keySet().iterator().next());
		}

		if (currentAnimationTime >= Properties.SWITCH_TIME) {
			// load next frame
			currentAnimationTime -= Properties.SWITCH_TIME;
			lastHand = nextHand;
			do {
				String key = (String) hands.keySet().toArray()[random.nextInt(hands.keySet().size())];
				nextHand = hands.get(key);
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

	@Override
	public void modifyFrame(SeededFrame frame) {
		if (newPosition == null) {
			newPosition = vectors.values().iterator().next();
		}
		if (newRotation == null){
			newRotation = rotations.values().iterator().next();
		}
		if (currentAnimationTime >= Properties.SWITCH_TIME || lastPosition == null || lastRotation == null) {
			lastPosition = newPosition;
			do {
				String key = (String) vectors.keySet().toArray()[random.nextInt(vectors.keySet().size())];
				newPosition = vectors.get(key);
			} while (newPosition == null);
			lastRotation = newRotation;
			do {
				String key = (String) rotations.keySet().toArray()[random.nextInt(rotations.keySet().size())];
				newRotation = rotations.get(key);
			} while (newRotation == null);
			lastSwitchTime = System.currentTimeMillis();

		}
		Hand h = Hand.invalid();
		for (Hand hand : frame.hands()) {
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
