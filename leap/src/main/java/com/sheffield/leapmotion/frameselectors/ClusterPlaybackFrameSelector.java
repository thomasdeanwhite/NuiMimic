package com.sheffield.leapmotion.frameselectors;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Vector;
import com.sheffield.instrumenter.Properties;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.FileHandler;
import com.sheffield.leapmotion.analyzer.AnalyzerApp;
import com.sheffield.leapmotion.analyzer.ProbabilityListener;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.framemodifier.FrameModifier;
import com.sheffield.leapmotion.mocks.HandFactory;
import com.sheffield.leapmotion.mocks.SeededFrame;
import com.sheffield.leapmotion.mocks.SeededHand;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class ClusterPlaybackFrameSelector extends FrameSelector implements FrameModifier {

	private HashMap<String, SeededHand> hands;
    private NGramLog currentHand = null;

    private ClusterPlayback[] clusterPlaybacks;

	private int currentAnimationTime = 0;
	private long lastSwitchTime = 0;
	private SeededHand lastHand;
	private SeededHand nextHand;

    private NGramLog currentPosition;
    private HashMap<String, Vector> vectors;
    private Vector lastPosition;
    private Vector newPosition;
    private long lastModSwitchTime = 0;
    private int currentModTime = 0;

    private NGramLog currentRotation;
    private HashMap<String, Vector[]> rotations;
    private Vector[] lastRotation;
    private Vector[] newRotation;

	public ClusterPlaybackFrameSelector(String filename, ArrayList<NGramLog>[] ngLogs) {
		try {
            clusterPlaybacks = new ClusterPlayback[ngLogs.length];
            for (int i = 0; i < ngLogs.length; i++){
                clusterPlaybacks[i] = new ClusterPlayback(ngLogs[i]);
            }
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

            //--------------------------- Setup of position and rotations
            String positionFile = Properties.DIRECTORY + "/" + filename + ".hand_position_data";
            lastModSwitchTime = System.currentTimeMillis();
            currentModTime = Properties.SWITCH_TIME;
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

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace(App.out);
			System.exit(0);
		}

	}

	@Override
	public Frame newFrame() {

        NGramLog hand = clusterPlaybacks[0].getCurrentNGramLog(currentAnimationTime);

		if (currentHand == null || !currentHand.equals(hand)) {
            //a switch has occurred
			lastSwitchTime = System.currentTimeMillis();
            currentHand = hand;
            currentAnimationTime = 0;
            lastHand = nextHand;
            nextHand = hands.get(currentHand.element);
		} else {
			currentAnimationTime = (int) (System.currentTimeMillis() - lastSwitchTime);
		}
		Frame f = SeededController.newFrame();
		float modifier = Math.min(1, currentAnimationTime / (float) currentHand.timeSeeded);
		Hand newHand = nextHand;
        if (lastHand != null){
            newHand = lastHand.fadeHand(nextHand, modifier);
        }
		f = HandFactory.injectHandIntoFrame(f, newHand);

		return f;
	}

	@Override
	public void modifyFrame(SeededFrame frame) {
        NGramLog position = clusterPlaybacks[1].getCurrentNGramLog(currentModTime);
        NGramLog rotation = clusterPlaybacks[2].getCurrentNGramLog(currentModTime);
        if (currentPosition == null || !position.equals(currentPosition)
                || !rotation.equals(currentRotation)){
            lastPosition = newPosition;
            newPosition = vectors.get(currentPosition.element);

            lastRotation = newRotation;
            newRotation = rotations.get(currentRotation.element);

            currentModTime = 0;
            lastModSwitchTime = System.currentTimeMillis();

        }
        Hand h = Hand.invalid();
        for (Hand hand : frame.hands()) {
            h = hand;
        }
        if (h instanceof SeededHand) {
            float modifier = currentModTime / (float) currentPosition.timeSeeded;
            SeededHand sh = (SeededHand) h;
            sh.setBasis(fadeVector(lastRotation[0], newRotation[0], modifier),
                    fadeVector(lastRotation[1], newRotation[1], modifier),
                    fadeVector(lastRotation[2], newRotation[2], modifier));
            sh.setOrigin(fadeVector(lastPosition, newPosition, modifier));
        }
        currentModTime = (int) (System.currentTimeMillis() - lastSwitchTime);
	}

    public Vector fadeVector(Vector prev, Vector next, float modifier){
        Vector v = next;
        if (next == null){
            v = prev;
        }
        if (prev != null && next != null){
            v = prev.plus(next.minus(prev).times(modifier));
        }
        return v;
    }
}
