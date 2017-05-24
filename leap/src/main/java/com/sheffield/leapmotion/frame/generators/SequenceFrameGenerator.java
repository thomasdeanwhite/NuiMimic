package com.sheffield.leapmotion.frame.generators;

import com.leapmotion.leap.*;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.controller.mocks.HandFactory;
import com.sheffield.leapmotion.controller.mocks.SeededFrame;
import com.sheffield.leapmotion.controller.mocks.SeededHand;
import com.sheffield.leapmotion.frame.analyzer.machinelearning.ngram.NGramModel;
import com.sheffield.leapmotion.frame.generators.gestures.GestureHandler;
import com.sheffield.leapmotion.frame.playback.NGramLog;
import com.sheffield.leapmotion.frame.util.Quaternion;
import com.sheffield.leapmotion.util.FileHandler;
import com.sheffield.leapmotion.util.ProgressBar;
import com.sheffield.output.Csv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by thomas on 19/05/17.
 */
public abstract class SequenceFrameGenerator extends FrameGenerator {
    public Csv getCsv() {
        return new Csv();
    }

    protected HashMap<String, SeededHand> joints;

    protected ArrayList<NGramLog> logs;

    protected SeededHand lastHand;
    protected String lastLabel = "";
    protected File outputJointsFile;

    protected HashMap<String, Vector> positions;
    protected HashMap<String, Quaternion> rotations;

    protected Vector lastPosition;
    protected String lastPositionLabel = "";

    protected Quaternion lastRotation;
    protected String lastRotationLabel = "";

    protected File outputPosFile;
    protected File outputRotFile;

    public void setOutputFiles(File pos, File rot) {
        outputPosFile = pos;
        outputRotFile = rot;
    }

    public void setOutputJointsFile(File outputJointsFile) {
        this.outputJointsFile = outputJointsFile;
    }

    public ArrayList<NGramLog> getLogs() {
        return logs;
    }

    public static HashMap<String, SeededHand> getJoints(String filename) throws IOException {
        HashMap<String, SeededHand> joints = new HashMap<String, SeededHand>();
        String clusterFile = filename;
        if (!Properties.SINGLE_DATA_POOL) {
            clusterFile += "/joint_position_data";
        } else {
            clusterFile += "/hand_joints_data";
        }

        String contents = FileHandler.readFile(new File(clusterFile));
        String[] lines = contents.split("\n");
        for (String line : lines) {
            Frame f = SeededController.newFrame();
            SeededHand hand = HandFactory.createHand(line, f);

            joints.put(hand.getUniqueId(), hand);
            // order.add(hand.getUniqueId());

            HandFactory.injectHandIntoFrame(f, hand);

        }

        return joints;
    }


    public static HashMap<String, Vector> getPositions(String filename) throws IOException {
        HashMap<String, Vector> positions = new HashMap<String, Vector>();

        String positionFile = filename + "/hand_position_data";
        String contents = FileHandler.readFile(new File(positionFile));
        String[] lines = contents.split("\n");
        for (String line : lines) {
            Vector v = new Vector();
            String[] vect = line.split(",");
            v.setX(Float.parseFloat(vect[1]));
            v.setY(Float.parseFloat(vect[2]));
            v.setZ(Float.parseFloat(vect[3]));

            positions.put(vect[0], v);

        }

        return positions;
    }


    public static HashMap<String, Quaternion> getRotations(String filename) throws IOException {
        String rotationFile = filename + "/hand_rotation_data";
        String contents = FileHandler.readFile(new File(rotationFile));
        String[] lines = contents.split("\n");
        HashMap<String, Quaternion> rotations = new HashMap<String, Quaternion>();
        for (String line : lines) {
            String[] vect = line.split(",");
            Quaternion q = new Quaternion(Float.parseFloat(vect[1]),
                    Float.parseFloat(vect[2]),
                    Float.parseFloat(vect[3]),
                    Float.parseFloat(vect[4])).normalise();

            rotations.put(vect[0], q);

        }
        return rotations;
    }

    public static HashMap<String, SeededHand> getRawJoints(String filename) throws IOException {
        String clusterFile = Properties.DIRECTORY + "/" + filename + "/" +
                (Properties.SINGLE_DATA_POOL ? "hand_joints_pool.ARFF" :
                        "joint_positions_pool.ARFF");
        HashMap<String, SeededHand> joints = new HashMap<String, SeededHand>();

        String contents = FileHandler.readFile(new File(clusterFile));
        String[] lines = contents.split("\n");

        boolean data = false;

        App.out.println(ProgressBar.getHeaderBar(21));

        for (String line : lines) {

            if (data && line.trim().length() > 0) {
                Frame f = SeededController.newFrame();
                SeededHand hand = HandFactory.createHand(line, f);

                joints.put(hand.getUniqueId(), hand);

                HandFactory.injectHandIntoFrame(f, hand);
            } else {
                if (line.contains("@DATA")) {
                    data = true;
                }
            }

        }

        return joints;
    }


    public static HashMap<String, Vector> getRawPositions(String filename) throws IOException {
        HashMap<String, Vector> vectors = new HashMap<String, Vector>();
        String positionFile = Properties.DIRECTORY + "/" + filename +
                "/hand_positions_pool.ARFF";
        String contents = FileHandler.readFile(new File(positionFile));
        String[] lines = contents.split("\n");
        boolean data = false;

        for (String line : lines) {


            if (data && line.trim().length() > 0) {
                Vector v = new Vector();
                String[] vect = line.split(",");
                v.setX(Float.parseFloat(vect[1]));
                v.setY(Float.parseFloat(vect[2]));
                v.setZ(Float.parseFloat(vect[3]));

                vectors.put(vect[0], v);
            } else {
                if (line.contains("@DATA")) {
                    data = true;
                }
            }

        }
        return vectors;
    }


    public static HashMap<String, Quaternion> getRawRotations(String filename) throws IOException {
        String rotationFile = Properties.DIRECTORY + "/" + filename +
                "/hand_rotations_pool.ARFF";
        String contents = FileHandler.readFile(new File(rotationFile));
        String[] lines = contents.split("\n");
        HashMap<String, Quaternion> rotations = new HashMap<String, Quaternion>();

        boolean data = false;

        for (String line : lines) {

            if (data && line.trim().length() > 0) {
                String[] vect = line.split(",");
                Quaternion q = new Quaternion(Float.parseFloat(vect[1]),
                        Float.parseFloat(vect[2]),
                        Float.parseFloat(vect[3]),
                        Float.parseFloat(vect[4])).normalise();

                rotations.put(vect[0], q);
            } else {
                if (line.contains("@DATA")) {
                    data = true;
                }
            }

        }
        return rotations;
    }


    public SequenceFrameGenerator(String file) {
        try {

            String processed = Properties.DIRECTORY + "/" + file + "/processed";

            joints = getJoints(processed);
            positions = getPositions(processed);
            rotations = getRotations(processed);
        } catch (IOException e) {
            e.printStackTrace(App.out);

        }
    }

    public SequenceFrameGenerator(HashMap<String, SeededHand> joints,
                                  HashMap<String, Vector> positions,
                                  HashMap<String, Quaternion> rotations) {
        this.joints = joints;
        this.positions = positions;
        this.rotations = rotations;
    }

    @Override
    public Frame newFrame() {
        Frame f = SeededController.newFrame();
//
        if (lastHand == null){
            return null;
        }

        Hand newHand = lastHand.copy();

        f = HandFactory.injectHandIntoFrame(f, newHand);

        return f;
    }

    @Override
    public String status() {
        return null;
    }

    @Override
    public void modifyFrame(SeededFrame frame) {
        Hand h = Hand.invalid();
        for (Hand hand : frame.hands()) {
            h = hand;
        }
        if (h instanceof SeededHand) {

            SeededHand sh = (SeededHand) h;

            Quaternion q = lastRotation;

            q.setBasis(sh);
            sh.setOrigin(lastPosition);
        }

    }

    @Override
    public boolean allowProcessing() {
        return true;
    }

    @Override
    public abstract String getName();

    protected long lastUpdate = 0;

    @Override
    public void tick(long time) {
        lastUpdate = time;

        fillLists();

    }

    private void fillLists() {
        lastLabel = nextSequenceJoints();
        lastHand = joints.get(lastLabel);

        lastPositionLabel = nextSequencePosition();
        lastPosition = positions.get(lastPositionLabel);

        lastRotationLabel = nextSequenceRotation();
        lastRotation = rotations.get(lastRotationLabel);
    }

    public abstract String nextSequenceJoints();

    public abstract String nextSequencePosition();

    public abstract String nextSequenceRotation();

    public long lastTick() {
        return lastUpdate;
    }

    @Override
    public void cleanUp() {

    }
}
