package com.sheffield.leapmotion.frame.generators;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.GestureList;
import com.sheffield.leapmotion.frame.generators.gestures.RandomGestureHandler;
import com.sheffield.output.Csv;

import java.util.Random;

public class VectorQuantizedFrameGenerator extends SequenceFrameGenerator {
	private RandomGestureHandler rgh = new RandomGestureHandler();
	private String[] candidateStabilised;

	@Override
	public GestureList handleFrame(Frame frame, Controller controller) {
		return rgh.handleFrame(frame, controller);
	}

	@Override
	public Csv getCsv() {
		return new Csv();
	}

	public VectorQuantizedFrameGenerator(String filename) {
		super(filename);
	}

	private String[] candidateHands = null;
	private String[] candidatePositions = null;
	private String[] candidateRotations = null;

	private Random random = new Random();

	public String randomHand(){
		if (candidateHands == null){
			candidateHands = new String[joints.keySet().size()];
			joints.keySet().toArray(candidateHands);
		}
		return candidateHands[random.nextInt(candidateHands.length)];
	}

	public String randomPosition(){
		if (candidatePositions == null){
			candidatePositions = new String[positions.keySet().size()];
			positions.keySet().toArray(candidatePositions);
		}
		return candidatePositions[random.nextInt(candidatePositions.length)];
	}

	public String randomStabilised(){
		if (candidateStabilised == null){
			candidateStabilised = new String[stabilisedTipPositions.keySet().size()];
			stabilisedTipPositions.keySet().toArray(candidateStabilised);
		}
		return candidateStabilised[random.nextInt(candidateStabilised
				.length)];
	}

	public String randomRotation(){
		if (candidateRotations == null){
			candidateRotations = new String[rotations.keySet().size()];
			rotations.keySet().toArray(candidateRotations);
		}
		return candidateRotations[random.nextInt(candidateRotations.length)];
	}

	@Override
	public boolean allowProcessing() {
		return true;
	}

	@Override
	public String getName() {
		return "Random Clusters Generation";
	}

	@Override
	public String nextSequenceJoints() {
		return randomHand();
	}

	@Override
	public String nextSequencePosition() {
		return randomPosition();
	}

	@Override
	public String nextSequenceRotation() {
		return randomRotation();
	}

	@Override
	public String nextSequenceGesture() {
		return null;
	}

	@Override
	public String nextSequenceStabilisedTips() {
		return randomStabilised();
	}

	public long lastTick(){
		return lastUpdate;
	}

	@Override
	public void cleanUp() {

	}
}
