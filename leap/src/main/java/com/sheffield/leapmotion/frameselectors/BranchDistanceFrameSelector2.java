package com.sheffield.leapmotion.frameselectors;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.framemodifier.FrameModifier;
import com.sheffield.leapmotion.mocks.SeededFrame;
import com.sheffield.leapmotion.mocks.SeededHand;
import com.sheffield.instrumenter.analysis.BranchType;
import com.sheffield.instrumenter.analysis.ClassAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class BranchDistanceFrameSelector2 extends FrameSelector implements FrameModifier {
	private HashMap<String, Frame> frames;

	public static final float JITTER = 5f;
	private static final float JITTER_OVER_2 = JITTER / 2;

	public static final float Z_ROTATION_JITTER = 0.1f;
	public static final float Z_ROTATION_JITTER_OVER_2 = Z_ROTATION_JITTER / 2;

	private Random r = new Random();
	private float x = 0;
	private float y = 200;
	private float z = 0;

	// 500^2
	public static final float MAX_DIFFERENCE = 10000f;

	private FrameSelector backup = new NGramFrameSelector("");

	private String currentBranch = null;
	private double currentBranchDistance = 1;
	private double lastBranchDistance = 1;
	private Frame lastFrame;

	private int playback = 0;

	private static final int MAX_ITERATIONS_TO_IMPROVE = 10;
	private int iterationsForCurrentBranchDistance = MAX_ITERATIONS_TO_IMPROVE;

	private static final int MAX_ITERATIONS_FOR_FRAME = 2;
	private int iterationsForCurrentFrame = MAX_ITERATIONS_FOR_FRAME;

	private Frame currentBestFrame;

	private ArrayList<Frame> currentCollection;

	private BranchType currentBranchType;

	public BranchDistanceFrameSelector2() {
		currentCollection = new ArrayList<Frame>();
	}

	@Override
	public Frame newFrame() {
		if (currentBranch == null) {
			currentBranch = ClassAnalyzer.getNextBranchDistanceOrdered();
			currentBranchType = ClassAnalyzer.getBranchType(currentBranch);
			if (currentBranch == null) {// all distance-branches have been
										// covered
				return backup.newFrame();
			}
		}

		// App.out.print("\r" + currentBranch + " " + currentBranchDistance);

		if (lastFrame == null) {
			Frame frame = backup.newFrame();
			currentBestFrame = frame;
			lastFrame = frame;
			return frame;
		} else if (playback < currentCollection.size()) {
			return currentCollection.get(playback++);
		} else if (iterationsForCurrentFrame > 0) {
			double bd = ClassAnalyzer.getBranchDistance(currentBranch);
			iterationsForCurrentFrame--;
			if (bd < currentBranchDistance) {
				currentBranchDistance = bd;
				currentBestFrame = lastFrame;
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
			double bd = ClassAnalyzer.getBranchDistance(currentBranch);

			if (bd < currentBranchDistance) {
				currentBranchDistance = bd;
				currentBestFrame = lastFrame;
			}

			iterationsForCurrentBranchDistance--;
			if (currentBranchDistance == 0) {
				iterationsForCurrentBranchDistance = 0;
			}

			if (iterationsForCurrentBranchDistance == MAX_ITERATIONS_TO_IMPROVE - 5) {
				// had 5 iterations to improve, have we? If no, we may not be
				// using this branch!
				if (currentBranchDistance == 1.0 && lastBranchDistance == 1.0) {
					// we haven't improved at all! Switch branches
					iterationsForCurrentBranchDistance = 0;
				}

			}

			if (iterationsForCurrentBranchDistance <= 0) {
				iterationsForCurrentBranchDistance = MAX_ITERATIONS_TO_IMPROVE;
				currentCollection.clear();
				currentBranch = ClassAnalyzer.getNextBranchDistanceOrdered();
				currentBranchType = ClassAnalyzer.getBranchType(currentBranch);
				currentBranchDistance = ClassAnalyzer.getBranchDistance(currentBranch);
			}

			iterationsForCurrentFrame = MAX_ITERATIONS_FOR_FRAME;

			if (currentBranchDistance < lastBranchDistance) {
				lastBranchDistance = currentBranchDistance;
				currentCollection.add(currentBestFrame);
				playback = 0;
				App.out.println("New branch distance record: " + currentBranchDistance);
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

		float currentBranchDistance = 1f - (float) ClassAnalyzer.getBranchDistance(currentBranch);

		for (Hand h : frame.hands()) {
			if (h instanceof SeededHand) {
				SeededHand hand = (SeededHand) h;
				x += (r.nextFloat() * JITTER - JITTER_OVER_2) * currentBranchDistance;
				y += (r.nextFloat() * JITTER - JITTER_OVER_2) * currentBranchDistance;
				z += (r.nextFloat() * JITTER - JITTER_OVER_2) * currentBranchDistance;
				hand.setOrigin(new Vector(x, y, z));
			}
		}
	}
}
