package com.sheffield.leapmotion.frameselectors;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.framemodifier.FrameModifier;
import com.sheffield.leapmotion.mocks.SeededFrame;
import com.sheffield.leapmotion.mocks.SeededHand;
import com.sheffield.instrumenter.analysis.ClassAnalyzer;

import java.util.HashMap;
import java.util.Random;

public class BranchDistanceAverageFrameSelector extends FrameSelector implements FrameModifier {
	private HashMap<String, Frame> frames;

	public static final float JITTER = 10f;
	private static final float JITTER_OVER_2 = JITTER / 2;

	public static final float Z_ROTATION_JITTER = (float) (Math.PI / 100);
	public static final float Z_ROTATION_JITTER_OVER_2 = Z_ROTATION_JITTER / 2;

	// 500^2
	public static final float MAX_DIFFERENCE = 10000f;

	public static final float MAX_SEQUENCE_LENGTH = 5;

	private FrameSelector backup = new NGramFrameSelector("");

	private Random r = new Random();
	private float x = 0;
	private float y = 200;
	private float z = 0;
	private float zRotation = 0;
	private float bestX = x;
	private float bestY = y;
	private float bestZ = z;

	private boolean retracing = false;

	private double currentBranchDistance = 1;
	private double lastBranchDistance = 1;
	private Frame lastFrame;

	private static final int MAX_ITERATIONS_TO_IMPROVE = 30;
	private int iterationsForCurrentBranchDistance = MAX_ITERATIONS_TO_IMPROVE;

	private static final int MAX_ITERATIONS_FOR_FRAME = 5;
	private int iterationsForCurrentFrame = MAX_ITERATIONS_FOR_FRAME;

	private Frame currentBestFrame;

	public BranchDistanceAverageFrameSelector() {
	}

	@Override
	public Frame newFrame() {
		if (iterationsForCurrentFrame > 0) {
			double bd = ClassAnalyzer.averageBranchDistance();
			iterationsForCurrentFrame--;
			if (bd < currentBranchDistance) {
				currentBranchDistance = bd;
				currentBestFrame = lastFrame;
				bestX = x;
				bestY = y;
				bestZ = z;
			}

			if (currentBranchDistance == 0) {
				iterationsForCurrentFrame = 0;
				return newFrame();
			}

			Frame f = null;
			f = backup.newFrame();
			lastFrame = f;
			return f;
		} else {
			double bd = ClassAnalyzer.averageBranchDistance();

			if (bd < currentBranchDistance) {
				currentBranchDistance = bd;
				currentBestFrame = lastFrame;
			}

			iterationsForCurrentBranchDistance--;
			if (currentBranchDistance == 0) {
				iterationsForCurrentBranchDistance = 0;
			}

			if (iterationsForCurrentBranchDistance <= MAX_ITERATIONS_TO_IMPROVE - 5) {
				// had 5 iterations to improve, have we? If no, we may not be
				// using this branch!
				if (currentBranchDistance == 1.0 && lastBranchDistance == 1.0) {
					// we haven't improved at all! Switch branches
					iterationsForCurrentBranchDistance = 0;
				}

			}

			if (iterationsForCurrentBranchDistance <= 0) {
				iterationsForCurrentBranchDistance = MAX_ITERATIONS_TO_IMPROVE;
				currentBranchDistance = ClassAnalyzer.averageBranchDistance();
			}

			iterationsForCurrentFrame = MAX_ITERATIONS_FOR_FRAME;

			if (currentBranchDistance < lastBranchDistance) {
				lastBranchDistance = currentBranchDistance;

			}

			iterationsForCurrentFrame = MAX_ITERATIONS_FOR_FRAME;

			if (currentBranchDistance == 0) {
				return backup.newFrame();
			}

			return newFrame();

		}

	}

	@Override
	public void modifyFrame(SeededFrame frame) {

		for (Hand h : frame.hands()) {
			if (h instanceof SeededHand) {
				SeededHand hand = (SeededHand) h;

				if (retracing && ClassAnalyzer.frameStateNew) {
					retracing = false;
					x = bestX;
					y = bestY;
					z = bestZ;
				} else {
					retracing = true;

					float dispX = r.nextFloat() * JITTER - JITTER_OVER_2;
					float dispY = r.nextFloat() * JITTER - JITTER_OVER_2;
					float dispZ = r.nextFloat() * JITTER - JITTER_OVER_2;

					float dispZRot = r.nextFloat() * Z_ROTATION_JITTER - Z_ROTATION_JITTER_OVER_2;

					x += dispX;
					y += dispY;
					z += dispZ;

					zRotation += dispZRot;

				}

				hand.setOrigin(new Vector(x, y, z));
				hand.setRotation(Vector.zAxis(), zRotation);
			}
		}
	}
}
