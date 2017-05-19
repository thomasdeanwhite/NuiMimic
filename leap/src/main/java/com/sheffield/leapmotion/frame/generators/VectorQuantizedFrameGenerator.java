package com.sheffield.leapmotion.frame.generators;

import com.sheffield.output.Csv;

import java.util.Random;

public class VectorQuantizedFrameGenerator extends SequenceFrameGenerator {
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
		return candidateHands[random.nextInt(joints.keySet().size())];
	}

	public String randomPosition(){
		if (candidatePositions == null){
			candidatePositions = new String[positions.keySet().size()];
			positions.keySet().toArray(candidatePositions);
		}
		return candidatePositions[random.nextInt(positions.keySet().size())];
	}

	public String randomRotation(){
		if (candidateRotations == null){
			candidateRotations = new String[rotations.keySet().size()];
			rotations.keySet().toArray(candidateRotations);
		}
		return candidateRotations[random.nextInt(rotations.keySet().size())];
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

	public long lastTick(){
		return lastUpdate;
	}

	@Override
	public void cleanUp() {

	}
}
