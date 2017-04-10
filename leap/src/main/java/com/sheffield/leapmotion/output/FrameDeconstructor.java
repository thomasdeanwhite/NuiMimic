package com.sheffield.leapmotion.output;

import com.leapmotion.leap.CircleGesture;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.KeyTapGesture;
import com.leapmotion.leap.ScreenTapGesture;
import com.leapmotion.leap.SwipeGesture;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.controller.mocks.HandFactory;
import com.sheffield.leapmotion.frame.util.QuaternionHelper;
import com.sheffield.leapmotion.util.FileHandler;
import com.sheffield.leapmotion.util.Serializer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by thomas on 11/03/2016.
 */
public class FrameDeconstructor {

    private String uniqueId = "";
    private String currentGesture = "";
    private String filenameStart = "";
    private String addition = "";

    public static long[] BREAK_TIMES = new long[]{Long.MAX_VALUE};

    private boolean calculatingScreenshot = false;


    private File currentDct;
    private File currentDctGestures;
    private File currentSequence;
    private File currentHands;
    private File currentHandJoints;
    private File currentPosition;
    private File currentRotation;
    private File currentGestures;
    private File currentGesturesCircle;
    private File currentGesturesSwipe;
    private File currentGesturesScreenTap;
    private File currentGesturesKeyTap;
    private File sequenceFile = null;

    private int breakIndex = 0;

    private ArrayList<String> handIds;
    private ArrayList<String> gestures;


    //1 state capture/second
    //private static final int STATE_CAPTURE_TIME = 300;
    private long lastStateCapture = 0;

    public FrameDeconstructor() {
        handIds = new ArrayList<String>();
        gestures = new ArrayList<String>();
    }

    public void addHandId(String id){
        handIds.add(id);
    }

    public void addGesture(String gesture){
        if (gesture != null) {
            gestures.add(gesture);
        }
    }

    public void setUniqueId(String uId) {
        uniqueId = uId;
    }
    
    public void setCurrentGesture(String g) {
        gestures.add(g);
    }

    public void setFilenameStart(String fns) {
        filenameStart = fns;
    }

    public void setAddition(String add) {
        addition = add;
    }

    public void resetFiles(int breakIndex) {
        sequenceFile = null;
        currentHands = null;
        currentRotation = null;
        currentPosition = null;
        currentSequence = null;
        currentGestures = null;
        currentDct = null;
        currentDctGestures = null;

        this.breakIndex = breakIndex;
    }

    public void outputRawFrameData(Frame frame) {
        try {
            boolean start = false;

            if (sequenceFile == null) {
                sequenceFile = new File(FileHandler.generateFileWithName(filenameStart) + "/raw_frame_data.bin");
                sequenceFile.getParentFile().mkdirs();
                sequenceFile.createNewFile();
                //com.sheffield.leapmotion.util.FileHandler.appendToFile(sequenceFile, "[");
                start = true;
            }
            String content = Serializer.sequenceToJson(frame);
            if (start) {
                FileHandler.writeToFile(sequenceFile, content);
            } else {
                FileHandler.appendToFile(sequenceFile, "\n" + content);
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void outputSequence() throws IOException {
        if (currentSequence == null) {
            currentSequence = new File(FileHandler.generateFileWithName(filenameStart) + addition + "/sequence_hand_data");
            currentSequence.getParentFile().mkdirs();
            currentSequence.createNewFile();
        }
        FileHandler.appendToFile(currentSequence, uniqueId + ",");
    }

    public void outputJointPositionModel(String frameAsString) throws IOException {
        if (currentHands == null) {
            currentHands = new File(FileHandler.generateFileWithName(filenameStart) + addition + "/joint_positions_pool.ARFF");
            currentHands.getParentFile().mkdirs();
            currentHands.createNewFile();

            FileHandler.writeToFile(currentHands, getHeaders(frameAsString, "jointposition"));
        }
        FileHandler.appendToFile(currentHands, frameAsString + "\n");
    }

    public void outputHandJointModel(Hand h) throws IOException {

        String frameAsString = HandFactory.handToHandJoint(uniqueId, h);

        if (currentHandJoints == null) {
            currentHandJoints = new File(FileHandler.generateFileWithName(filenameStart) + addition + "/hand_joints_pool.ARFF");
            currentHandJoints.getParentFile().mkdirs();
            currentHandJoints.createNewFile();

            FileHandler.writeToFile(currentHandJoints, getHeaders(frameAsString, "handjoint"));
        }
        FileHandler.appendToFile(currentHandJoints, frameAsString + "\n");
    }

    public String getHandPosition(Hand h){
        String position = uniqueId + "," + h.palmPosition().getX() + ","
                + h.palmPosition().getY() + "," + h.palmPosition().getZ() + "\n";

        return position;
    }

    public void outputHandPositionModel(Hand h) throws IOException {

        String position = getHandPosition(h);

        if (currentPosition == null) {
            currentPosition = new File(FileHandler.generateFileWithName(filenameStart) + addition + "/hand_positions_pool.ARFF");
            currentPosition.getParentFile().mkdirs();
            currentPosition.createNewFile();

            FileHandler.writeToFile(currentPosition, getHeaders(position, "handposition"));
        }
        FileHandler.appendToFile(currentPosition, position);
    }

    public String getHandRotationModel(Hand h) {
        String rotation = uniqueId + ",";
        Vector[] vectors = new Vector[3];

        //h.basis().r

//        vectors[0] = h.basis().getXBasis();
//        vectors[1] = h.basis().getYBasis();
//        vectors[2] = h.basis().getZBasis();

        vectors[0] = h.palmNormal().cross(h.direction()).normalized();
        vectors[1]  = h.palmNormal().opposite();
        vectors[2]  = h.direction().opposite();



        rotation += QuaternionHelper.toQuaternion(vectors).inverse().toCsv();
        rotation += "\n";


        return rotation;
    }

    public void outputHandRotationModel(Hand h) throws IOException {
        String rotation = getHandRotationModel(h);

        if (currentRotation == null) {
            currentRotation = new File(FileHandler.generateFileWithName(filenameStart) + addition + "/hand_rotations_pool.ARFF");
            currentRotation.getParentFile().mkdirs();
            currentRotation.createNewFile();

            FileHandler.writeToFile(currentRotation, getHeaders(rotation, "handrotation"));
        }


        FileHandler.appendToFile(currentRotation, rotation);
    }


    public String getHeaders(String data, String relation){
        int features = data.split(",").length;


        String header = "% 1. Leap Motion Hands\n"+
                "% 2. Created by NuiMimic\n" +
                "@RELATION " + relation + "\n" +
                "\n" +
                "@ATTRIBUTE id STRING\n";

        for (int i = 1; i < features; i++){
            header += "@ATTRIBUTE v" + i + " NUMERIC\n";
        }


        header += "@DATA\n";

        return header;
    }

    public boolean isCalculating() {
        return calculatingScreenshot;
    }

    public void outputCurrentState() throws IOException {
        if (calculatingScreenshot) {
            return;
        }

        calculatingScreenshot = true;
        if (currentDct == null) {
            currentDct = new File(FileHandler.generateFileWithName(filenameStart) + addition + "/dct_pool");
            currentDct.getParentFile().mkdirs();
            currentDct.createNewFile();
        }
        if (currentDctGestures == null) {
            currentDctGestures = new File(FileHandler.generateFileWithName(filenameStart) + addition + "/dct_gestures_pool");
            currentDctGestures.getParentFile().mkdirs();
            currentDctGestures.createNewFile();
        }
        if (lastStateCapture + App.STATE_CHECK_TIME < System.currentTimeMillis()) {
            lastStateCapture = System.currentTimeMillis();

            try {
                String output = StateComparator.peekState();

                if (output != null && output.length() > 0) {
                    FileHandler.appendToFile(currentDct, "\n" + output + ":");
                    FileHandler.appendToFile(currentDctGestures, "\n" + output + ":");

                }
            } catch (Exception e){
                e.printStackTrace(App.out);
            }


        }
        handIds.add(uniqueId);
//        if (currentGesture != null) {
//            gestures.add(currentGesture);
//        }
        calculatingScreenshot = false;
        if (handIds.size() > 0) {
            String hands = "";
            for (int i = 0; i < handIds.size(); i++) {
                hands += handIds.get(i) + ",";
            }
            FileHandler.appendToFile(currentDct, hands);
            handIds.clear();
        }
        
        if (gestures.size() > 0) {
            String gests= "";
            for (int i = 0; i < gestures.size(); i++) {
                if (gestures.get(i) != null && gestures.get(i).length() > 1) {
                    gests += gestures.get(i) + ",";
                } else {
                    gests += Gesture.Type.TYPE_INVALID + ",";
                }
            }
            if (gests.length() > 1) {
                FileHandler.appendToFile(currentDctGestures, gests);
            }
            gestures.clear();
        }
    }

    public void outputGestureModel(Frame frame) throws IOException {
        String gestureString = "";
        if (frame.gestures().count() > 0) {


            for (Gesture g : frame.gestures()) {
                gestureString += g.type() + "+";

                switch (g.type()) {
                    case TYPE_CIRCLE:
                        CircleGesture cg = new CircleGesture(g);
                        String circleGesture = uniqueId + cg.center().getX() + "," +
                                cg.center().getY() + "," +
                                cg.center().getZ() + ",";

                        circleGesture += cg.normal().getX() + "," +
                                cg.normal().getY() + "," +
                                cg.normal().getZ() + ",";

                        circleGesture += cg.radius();

                        circleGesture += cg.duration() + "\n";

                        if (currentGesturesCircle == null) {
                            currentGesturesCircle = new File(FileHandler.generateFileWithName(filenameStart) + addition + "/gesture_circle_pool.ARFF");
                            currentGesturesCircle.getParentFile().mkdirs();
                            currentGesturesCircle.createNewFile();

                            FileHandler.writeToFile(currentGesturesCircle, getHeaders(circleGesture, "circlegesture"));
                        }

                        FileHandler.appendToFile(currentGesturesCircle, circleGesture);
                        break;
                    case TYPE_SWIPE:
                        SwipeGesture sg = new SwipeGesture(g);
                        String swipeGesture = uniqueId + sg.startPosition().getX() + "," +
                                sg.startPosition().getY() + "," +
                                sg.startPosition().getZ() + ",";

                        swipeGesture += sg.position().getX() + "," +
                                sg.position().getY() + "," +
                                sg.position().getZ() + ",";

                        swipeGesture += sg.direction().getX() + "," +
                                sg.direction().getY() + "," +
                                sg.direction().getZ() + ",";

                        swipeGesture += sg.speed() + "\n";


                        if (currentGesturesSwipe == null) {
                            currentGesturesSwipe = new File(FileHandler.generateFileWithName(filenameStart) + addition + "/gesture_swipe_pool.ARFF");
                            currentGesturesSwipe.getParentFile().mkdirs();
                            currentGesturesSwipe.createNewFile();

                            FileHandler.writeToFile(currentGesturesSwipe, getHeaders(swipeGesture, "swipegesture"));
                        }

                        FileHandler.appendToFile(currentGesturesSwipe, swipeGesture);
                        break;
                    case TYPE_SCREEN_TAP:
                        ScreenTapGesture stg = new ScreenTapGesture(g);

                        String screenTapGesture = uniqueId + stg.position().getX() + "," +
                                stg.position().getY() + "," +
                                stg.position().getZ() + ",";

                        screenTapGesture += stg.direction().getX() + "," +
                                stg.direction().getY() + "," +
                                stg.direction().getZ() + ",";

                        screenTapGesture += stg.progress();

                        if (currentGesturesScreenTap == null) {
                            currentGesturesScreenTap = new File(FileHandler.generateFileWithName(filenameStart) + addition + "/gesture_screentap_pool.ARFF");
                            currentGesturesScreenTap.getParentFile().mkdirs();
                            currentGesturesScreenTap.createNewFile();

                            FileHandler.writeToFile(currentGesturesScreenTap, getHeaders(screenTapGesture, "screentapgesture"));
                        }

                        FileHandler.appendToFile(currentGesturesScreenTap, screenTapGesture);
                        break;

                    case TYPE_KEY_TAP:
                        KeyTapGesture ktg = new KeyTapGesture(g);

                        String keyTapGesture = uniqueId + ktg.position().getX() + "," +
                                ktg.position().getY() + "," +
                                ktg.position().getZ() + ",";

                        keyTapGesture += ktg.direction().getX() + "," +
                                ktg.position().getY() + "," +
                                ktg.position().getZ() + ",";

                        keyTapGesture += ktg.progress() + "\n";

                        if (currentGesturesKeyTap == null) {
                            currentGesturesKeyTap = new File(FileHandler.generateFileWithName(filenameStart) + addition + "/gesture_keytap_pool.ARFF");
                            currentGesturesKeyTap.getParentFile().mkdirs();
                            currentGesturesKeyTap.createNewFile();

                            FileHandler.writeToFile(currentGesturesKeyTap, getHeaders(keyTapGesture, "keytapgesture"));
                        }

                        FileHandler.appendToFile(currentGesturesKeyTap, keyTapGesture);
                        break;
                }
            }
        }
        if (gestureString.length() == 0) {
            gestureString = Gesture.Type.TYPE_INVALID.toString();
        } else {
            gestureString = gestureString.substring(0, gestureString.length() - 1);
        }

        if (currentGestures == null) {
            currentGestures = new File(FileHandler.generateFileWithName(filenameStart) + addition + "/sequence_gesture_data.csv");
            currentGestures.getParentFile().mkdirs();
            currentGestures.createNewFile();
        }
        FileHandler.appendToFile(currentGestures, gestureString + " ");
    }


}
