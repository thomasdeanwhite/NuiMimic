package com.sheffield.leapmotion.frame.generators;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.GestureList;
import com.sheffield.leapmotion.frame.generators.gestures.RandomGestureHandler;
import com.sheffield.output.Csv;

import java.io.File;
import java.util.Random;

public class VectorQuantizedFrameGenerator extends SequenceFrameGenerator {

	@Override
	public void setGestureOutputFile(File f) {

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
	private String[] candidateGestures = null;
	private String[] candidateCircleGestures = null;
	private String[] candidateStabilised;

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

	public String randomGesture(){
		if (candidateGestures == null){
			Gesture.Type[] gts = Gesture.Type.values();
			candidateGestures = new String[gts.length];

			for (int i = 0; i < gts.length; i++){
				candidateGestures[i] = gts[i].toString();
			}
		}
		return candidateGestures[random.nextInt(candidateGestures.length)];
	}

	public String randomCircleGesture(){
		if (candidateCircleGestures == null){
			candidateCircleGestures = new String[sgh.getCircleGestures().keySet().size()];
			sgh.getCircleGestures().keySet().toArray(candidateCircleGestures);
		}
		return candidateCircleGestures[random.nextInt(candidateCircleGestures.length)];
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
		return randomGesture();
	}

	@Override
	public String nextSequenceCircleGesture() {
		return randomCircleGesture();
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
