package com.sheffield.leapmotion.framemodifier;

import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Vector;
import com.sheffield.instrumenter.Properties;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.BezierHelper;
import com.sheffield.leapmotion.FileHandler;
import com.sheffield.leapmotion.analyzer.AnalyzerApp;
import com.sheffield.leapmotion.analyzer.ProbabilityListener;
import com.sheffield.leapmotion.frameselectors.NGramFrameSelector;
import com.sheffield.leapmotion.frameselectors.NGramLog;
import com.sheffield.leapmotion.mocks.SeededFrame;
import com.sheffield.leapmotion.mocks.SeededHand;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.HashMap;

public class NGramFrameModifier implements FrameModifier {
    private HashMap<String, SeededHand> hands;
    protected AnalyzerApp positionAnalyzer;
    protected AnalyzerApp rotationAnalyzer;
    private HashMap<String, Vector> vectors;
    private HashMap<String, Vector[]> rotations;

    private Vector lastPosition;
    private String lastPositionLabel;
    private ArrayList<Vector> seededPositions = new ArrayList<Vector>();
    private ArrayList<String> positionLabels = new ArrayList<String>();

    private Vector[] lastRotation;
    private String lastRotationLabel;
    private ArrayList<Vector[]> seededRotations = new ArrayList<Vector[]>();
    private ArrayList<String> rotationLabels = new ArrayList<String>();

    private int currentAnimationTime = 0;
    private long lastSwitchTime = 0;
    
    private File outputPosFile;
    private File outputRotFile;
    
    public void setOutputFiles(File pos, File rot){
    	outputPosFile = pos;
    	outputRotFile = rot;
    }

    public void addPositionProbabilityListener(ProbabilityListener pbl){
        positionAnalyzer.addProbabilityListener(pbl);
    }

    public void addRotationProbabilityListener(ProbabilityListener pbl){
        rotationAnalyzer.addProbabilityListener(pbl);
    }

    public NGramFrameModifier(String filename) {
        try {
            String positionFile = Properties.DIRECTORY + "/" + filename + ".hand_position_data";
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

            String sequenceFile = Properties.DIRECTORY + "/" + filename + ".hand_position_ngram";
            positionAnalyzer = new AnalyzerApp(sequenceFile);
            positionAnalyzer.analyze();

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

            sequenceFile = Properties.DIRECTORY + "/" + filename + ".hand_rotation_ngram";
            rotationAnalyzer = new AnalyzerApp(sequenceFile);
            rotationAnalyzer.analyze();
        } catch (IOException e) {
            e.printStackTrace(App.out);
        }

    }

    @Override
    public void modifyFrame(SeededFrame frame) {
        while (lastPosition == null){
            lastPositionLabel = positionAnalyzer.getDataAnalyzer().next();
            if (lastPositionLabel != null && !lastPositionLabel.equals("null")){
                lastPosition = vectors.get(lastPositionLabel);
            }
        }

        while (lastRotation== null){
            lastRotationLabel = rotationAnalyzer.getDataAnalyzer().next();
            if (lastRotationLabel != null && !lastRotationLabel.equals("null")){
                lastRotation = rotations.get(lastRotationLabel);
            }
        }


        while (seededPositions.size() < BezierHelper.BEZIER_NUMBER){
            if (seededPositions.contains(lastPosition)){
                Vector position = null;
                String pLabel = null;
                while (position == null){
                    pLabel = positionAnalyzer.getDataAnalyzer().next();

                    if (pLabel != null){
                        position = vectors.get(pLabel);
                        if (position != null) {
                            positionLabels.add(pLabel);
                            seededPositions.add(position);
                        }
                    }
                }
            } else {
                seededPositions.add(0, lastPosition);
                positionLabels.add(0, lastPositionLabel);
            }
        }

        while (seededRotations.size() < BezierHelper.BEZIER_NUMBER){
            if (seededRotations.contains(lastRotation)){
                Vector[] rotation = null;
                String rLabel = null;
                while (rotation == null){
                    rLabel = rotationAnalyzer.getDataAnalyzer().next();

                    if (rLabel != null){
                        rotation = rotations.get(rLabel);
                        if (rotation != null) {
                            rotationLabels.add(rLabel);
                            seededRotations.add(rotation);
                        }
                    }
                }
            } else {
                seededRotations.add(0, lastRotation);
                rotationLabels.add(0, lastRotationLabel);
            }
        }

        if (currentAnimationTime >= Properties.SWITCH_TIME) {
            NGramLog posLog = new NGramLog();
            posLog.element = "";
            currentAnimationTime = 0;

            for (String s : positionLabels){
                posLog.element += s + ",";
            }

            posLog.timeSeeded = (int) (System.currentTimeMillis() - lastSwitchTime);
            
            NGramLog rotLog = new NGramLog();
            rotLog.element = "";

            for (String s : rotationLabels){
                rotLog.element += s + ",";
            }

            rotLog.timeSeeded = posLog.timeSeeded;
			if (outputPosFile != null){
				try {
					FileHandler.appendToFile(outputPosFile, posLog.toString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace(App.out);
				}
			}
			
			if (outputRotFile != null){
				try {
					FileHandler.appendToFile(outputRotFile, rotLog.toString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace(App.out);
				}
			}

            lastSwitchTime = System.currentTimeMillis();


            lastPosition = seededPositions.get(seededPositions.size()-1);
            lastPositionLabel = positionLabels.get(positionLabels.size()-1);
            lastRotation = seededRotations.get(seededRotations.size()-1);
            lastRotationLabel = rotationLabels.get(rotationLabels.size()-1);

            seededPositions.clear();
            seededRotations.clear();
            positionLabels.clear();
            rotationLabels.clear();

            modifyFrame(frame);
            return;

        }
        Hand h = Hand.invalid();
        for (Hand hand : frame.hands()) {
            h = hand;
        }
        if (h instanceof SeededHand) {
            float modifier = currentAnimationTime / (float) Properties.SWITCH_TIME;
            SeededHand sh = (SeededHand) h;

            Vector[] rotationVectors = new Vector[lastRotation.length];
            for (int i = 0; i < lastRotation.length; i++){
                ArrayList<Vector> vs = new ArrayList<Vector>();
                for (Vector[] vects : seededRotations){
                    vs.add(vects[i]);
                }
                rotationVectors[i] = BezierHelper.bezier(vs, modifier);
            }

            sh.setBasis(rotationVectors[0], rotationVectors[1],
                    rotationVectors[2]);
            sh.setOrigin(BezierHelper.bezier(seededPositions, modifier));
        }
        currentAnimationTime = (int) (System.currentTimeMillis() - lastSwitchTime);

    }
}
