package com.sheffield.leapmotion.frame.generators;

import com.google.gson.Gson;
import com.leapmotion.leap.*;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.controller.mocks.*;
import com.sheffield.leapmotion.frame.generators.gestures.GestureHandler;
import com.sheffield.leapmotion.frame.generators.gestures.SequenceGestureHandler;
import com.sheffield.leapmotion.frame.playback.NGramLog;
import com.sheffield.leapmotion.frame.util.Quaternion;
import com.sheffield.leapmotion.output.TestingStateComparator;
import com.sheffield.leapmotion.util.FileHandler;
import com.sheffield.leapmotion.util.ProgressBar;
import com.scythe.output.Csv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by thomas on 19/05/17.
 */
public abstract class SequenceFrameGenerator extends FrameGenerator implements GestureHandler {


    public Csv getCsv() {
        return new Csv();
    }

    protected SequenceGestureHandler sgh;

    protected void disableOutput (){
        Properties.OUTPUT_SEQUENCE = false;
    }

    private HashMap<Integer, Integer[]> states = new HashMap<Integer, Integer[]>();

    protected HashMap<String, SeededHand> joints;

    protected ArrayList<NGramLog> logs;

    protected SeededHand lastHand;
    protected String lastLabel = "";
    protected File outputJointsFile;

    protected HashMap<String, Vector> positions;
    protected HashMap<String, Quaternion> rotations;
    protected HashMap<String, Vector[]> stabilisedTipPositions;

    protected Vector lastPosition;
    protected String lastPositionLabel = "";

    protected Quaternion lastRotation;
    protected String lastRotationLabel = "";

    private String lastStabilisedLabel;
    private Vector[] lastStabilised;

    private String nextGesture = "TYPE_INVALID";
    private String nextCircleGesture = "0";

    protected long lastUpdate = 0;

    protected File outputPosFile;
    protected File outputRotFile;

    protected File regressionFile;
    protected File statesFile;


    public static HashMap<String, SeededHand> getFeaturelessHands(String
                                                                          filename)

            throws IOException {
        HashMap<String, SeededHand> joints = new HashMap<String, SeededHand>();
        String clusterFile = filename;
        clusterFile += "/feature_hand_data";


        String contents = FileHandler.readFile(new File(clusterFile));
        String[] lines = contents.split("\n");
        for (String line : lines) {
            Frame f = SeededController.newFrame();
            SeededHand hand = HandFactory.createHandFeatureless(line, f);

            joints.put(hand.getUniqueId(), hand);
            // order.add(hand.getUniqueId());

            HandFactory.injectHandIntoFrame(f, hand);

        }

        return joints;
    }

    public static HashMap<String, SeededHand> getJoints(String filename) throws IOException {
        HashMap<String, SeededHand> joints = new HashMap<String, SeededHand>();
        String clusterFile = filename;
        clusterFile += "/joint_position_data";

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

    public static HashMap<String, Vector[]> getStabilizedTips(String filename)
            throws IOException {
        HashMap<String, Vector[]> positions = new HashMap<String, Vector[]>();

        String positionFile = filename + "/hand_position_data";
        String contents = FileHandler.readFile(new File(positionFile));
        String[] lines = contents.split("\n");
        for (String line : lines) {
            Vector[] v = new Vector[6];
            String[] vect = line.split(",");

            for (int i = 0; i < v.length; i++) {
                v[i] = new Vector(Float.parseFloat(vect[1]),
                        Float.parseFloat(vect[2]),
                        Float.parseFloat(vect[3]));
            }
            positions.put(vect[0], v);

        }

        return positions;
    }

    public static HashMap<String, SeededCircleGesture> getCircleGestures(String filename) {
        HashMap<String, SeededCircleGesture> positions = new HashMap<String, SeededCircleGesture>();

        String positionFile = filename + "/gesture_circle_data";

        String contents = null;
        try {
            contents = FileHandler.readFile(new File(positionFile));
        } catch (IOException e) {
            App.out.println("- No circle gestures found!");
            return new HashMap<String, SeededCircleGesture>();
        }
        String[] lines = contents.split("\n");

        SeededGesture sg = new SeededGesture(Gesture.Type.TYPE_CIRCLE, Gesture.State.STATE_UPDATE,
                Frame.invalid(), 0, 0);

        for (String line : lines) {
            SeededCircleGesture scg = new SeededCircleGesture(sg);
            String[] vect = line.split(",");

            scg.setCenter(new Vector(Float.parseFloat(vect[1]),
                    Float.parseFloat(vect[2]),
                    Float.parseFloat(vect[3])));

            scg.setNormal(new Vector(Float.parseFloat(vect[4]),
                    Float.parseFloat(vect[5]),
                    Float.parseFloat(vect[6])));

            scg.setRadius(Float.parseFloat(vect[7]));

            positions.put(vect[0], scg);

        }

        return positions;
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

    public static HashMap<String, Vector[]> getRawStabilisedTips(String
                                                                         filename)
            throws IOException {
        HashMap<String, Vector[]> vectors = new HashMap<String, Vector[]>();
        String positionFile = Properties.DIRECTORY + "/" + filename +
                "/stabilised_tip_pool.ARFF";
        String contents = FileHandler.readFile(new File(positionFile));
        String[] lines = contents.split("\n");
        boolean data = false;

        for (String line : lines) {


            if (data && line.trim().length() > 0) {
                Vector[] v = new Vector[6];
                String[] vect = line.split(",");
                for (int i = 0; i < v.length; i++) {
                    v[i] = new Vector(Float.parseFloat(vect[1 + i*3]), Float
                            .parseFloat(vect[2 + i*3]), Float.parseFloat
                            (vect[3 + i*3]));
                }

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

    public void setupOutputFiles() throws IOException {
        if (Properties.OUTPUT_SEQUENCE) {

            regressionFile = new File(Properties.TESTING_OUTPUT + "/" + Properties.CURRENT_RUN + "/" + Properties.FRAME_SELECTION_STRATEGY + "/regression_orders.json");
            statesFile = new File(Properties.TESTING_OUTPUT + "/" + Properties.CURRENT_RUN + "/" + Properties.FRAME_SELECTION_STRATEGY + "/states.json");

            if (!regressionFile.exists()) {
                if (!regressionFile.getParentFile().exists()) {
                    regressionFile.getParentFile().mkdirs();
                }
                regressionFile.createNewFile();
            }

            if (!statesFile.exists()) {
                statesFile.createNewFile();
            }
        }
    }

    public SequenceFrameGenerator(String file) {
        try {

            String processed = Properties.DIRECTORY + "/" + file +
                    "/processed/" + Properties.CLUSTERS + "-" + Properties.N;

            //setup clusters
            if (Properties.SINGLE_DATA_POOL) {
                joints = getFeaturelessHands(processed);
            } else {
                joints = getJoints(processed);
            }
            positions = getPositions(processed);
            rotations = getRotations(processed);
            stabilisedTipPositions = getStabilizedTips(processed);
            this.sgh = new SequenceGestureHandler(getCircleGestures(processed));
//            Properties.CLUSTERS = joints.keySet().size();

            //load ordering of clusters and observed states
            setupOutputFiles();


        } catch (IOException e) {
            e.printStackTrace(App.out);

        }
    }

    public SequenceFrameGenerator(HashMap<String, SeededHand> joints,
                                  HashMap<String, Vector> positions,
                                  HashMap<String, Quaternion> rotations,
                                  HashMap<String, Vector[]>
                                          stabilisedTipPositions,
                                  HashMap<String, SeededCircleGesture> seededCircleGestures) {
        this.joints = joints;
        this.positions = positions;
        this.rotations = rotations;
        this.stabilisedTipPositions = stabilisedTipPositions;
        this.sgh = new SequenceGestureHandler(seededCircleGestures);

//        Properties.CLUSTERS = joints.keySet().size();

        try {
            setupOutputFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        if (Properties.SINGLE_DATA_POOL){
            return;
        }

        Hand h = Hand.invalid();
        for (Hand hand : frame.hands()) {
            h = hand;
        }
        if (h instanceof SeededHand) {

            SeededHand sh = (SeededHand) h;

            for (int i = 0; i < Finger.Type.values().length; i++){
                Finger f = sh.fingers().fingerType(Finger.Type.values()[i]).get(0);

                if (f != null && f instanceof SeededFinger) {
                    ((SeededFinger)f).setStabilizedTipPosition(lastStabilised[i + 1]);
                }
            }

            sh.setStabilizedPalmPosition(lastStabilised[0]);

            assert lastPosition != null;
            assert lastRotation != null;

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

    @Override
    public void tick(long time) {
        lastUpdate = time;


        sgh.setNextGesture(nextGesture);

        if (nextGesture.contains("TYPE_CIRCLE")) {
            sgh.setNextCircleGesture(nextCircleGesture);
        }

        sgh.tick(time);

        fillLists();

    }

    private void fillLists() {
        lastLabel = nextSequenceJoints();
        lastHand = joints.get(lastLabel);

        lastPositionLabel = nextSequencePosition();
        lastPosition = positions.get(lastPositionLabel);

        lastRotationLabel = nextSequenceRotation();
        lastRotation = rotations.get(lastRotationLabel);
        
        lastStabilisedLabel = nextSequenceStabilisedTips();
        lastStabilised = stabilisedTipPositions.get(lastStabilisedLabel);

        nextGesture = nextSequenceGesture();
        nextCircleGesture = nextSequenceCircleGesture();

    }

    //TODO: Model as parameter
    public abstract String nextSequenceJoints();

    public abstract String nextSequencePosition();

    public abstract String nextSequenceRotation();

    public abstract String nextSequenceGesture();

    public abstract String nextSequenceCircleGesture();

    public abstract String nextSequenceStabilisedTips();

    public long lastTick() {
        return lastUpdate;
    }

    @Override
    public void cleanUp() {

    }

    Gson gson = new Gson();

    @Override
    public GestureList handleFrame(Frame f, Controller c){

        //output regression suite
        if (Properties.OUTPUT_SEQUENCE){ // we should write the output (e.g.
            // false if regression testing)
            try {
                int state = TestingStateComparator.getCurrentState();

                if (state != -1 && !states.containsKey(state)){

                    StringBuilder sb = new StringBuilder();

                    Integer[] states = TestingStateComparator.getState(state);

                    for (Integer i : states){
                        sb.append(i);
                        sb.append(",");
                    }

                    FileHandler.appendToFile(statesFile, state + ":" + sb.toString() + "\n");

                    this.states.put(state, states);
                }

                RegressionOrder rg = new RegressionOrder(lastLabel, lastPositionLabel, lastRotationLabel,
                        lastStabilisedLabel, nextGesture, nextCircleGesture, lastUpdate, state);



                FileHandler.appendToFile(regressionFile, gson.toJson(rg) + "\n");
            } catch (IOException e) {
                e.printStackTrace(App.out);
            }
        }


        return sgh.handleFrame(f, c);
    }


}
