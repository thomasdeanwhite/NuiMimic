package com.sheffield.instrumenter.framemodifier;

import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.analyzer.AnalyzerApp;
import com.sheffield.leapmotion.mocks.SeededFrame;
import com.sheffield.leapmotion.mocks.SeededHand;
import com.sheffield.instrumenter.App;
import com.sheffield.instrumenter.FileHandler;
import com.sheffield.instrumenter.Properties;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class NGramFrameModifier implements FrameModifier {
    private HashMap<String, SeededHand> hands;
    private AnalyzerApp positionAnalyzer;
    private AnalyzerApp rotationAnalyzer;
    private HashMap<String, Vector> vectors;
    private HashMap<String, Vector[]> rotations;
    private Vector lastPosition;
    private Vector newPosition;

    private Vector[] lastRotation;
    private Vector[] newRotation;

    private int currentAnimationTime = 0;
    private long lastSwitchTime = 0;

    public NGramFrameModifier(String filename) {
        try {
            String positionFile = Properties.DIRECTORY + "/" + filename + ".positioncluster";
            lastSwitchTime = System.currentTimeMillis();
            currentAnimationTime = Properties.SWITCH_TIME;
            String contents = FileHandler.readFile(new File(positionFile));
            String[] lines = contents.split("\n");
            vectors = new HashMap<String, Vector>();
            for (String line : lines) {
                Vector v = new Vector();
                String[] vect = line.split(",");
                v.setX(Float.parseFloat(vect[1]));
                v.setY(Float.parseFloat(vect[2]));
                v.setZ(Float.parseFloat(vect[3]));

                vectors.put(vect[0], v);

            }

            String sequenceFile = Properties.DIRECTORY + "/" + filename + ".positiondata";
            positionAnalyzer = new AnalyzerApp(sequenceFile);
            positionAnalyzer.analyze();

            String rotationFile = Properties.DIRECTORY + "/" + filename + ".rotationcluster";
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

            sequenceFile = Properties.DIRECTORY + "/" + filename + ".rotationdata";
            rotationAnalyzer = new AnalyzerApp(sequenceFile);
            rotationAnalyzer.analyze();
        } catch (IOException e) {
            e.printStackTrace(App.out);
        }

    }

    @Override
    public void modifyFrame(SeededFrame frame) {
        if (newPosition == null) {
            newPosition = vectors.values().iterator().next();
        }
        if (newRotation == null){
            newRotation = rotations.values().iterator().next();
        }
        if (currentAnimationTime >= Properties.SWITCH_TIME) {
            lastPosition = newPosition;
            do {
                newPosition = vectors.get(positionAnalyzer.getDataAnalyzer().nextHand());
            } while (newPosition == null);
            lastRotation = newRotation;
            do {
                newRotation = rotations.get(rotationAnalyzer.getDataAnalyzer().nextHand());
            } while (newRotation == null);
            lastSwitchTime = System.currentTimeMillis();

        }
        Hand h = Hand.invalid();
        for (Hand hand : frame.hands()) {
            h = hand;
        }
        if (h instanceof SeededHand) {
            float modifier = Math.min(1, currentAnimationTime / (float) Properties.SWITCH_TIME);
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
