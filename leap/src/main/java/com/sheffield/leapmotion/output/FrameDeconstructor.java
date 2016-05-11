package com.sheffield.leapmotion.output;

import com.leapmotion.leap.*;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.FileHandler;
import com.sheffield.leapmotion.Serializer;

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

    public static long[] BREAK_TIMES = new long[]{};

    private boolean calculatingScreenshot = false;


    private File currentDct;
    private File currentDctGestures;
    private File currentSequence;
    private File currentHands;
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
    private static final int STATE_CAPTURE_TIME = 300;
    private long lastStateCapture = 0;

    public FrameDeconstructor() {
        handIds = new ArrayList<String>();
        gestures = new ArrayList<String>();
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
            String dir = "/sequences";
            try {
                boolean start = false;

                if (sequenceFile == null) {
                    String addition = "-" + BREAK_TIMES[breakIndex];
                    sequenceFile = new File(FileHandler.generateFileWithName(filenameStart) + addition + "ms.raw_frame_data");
                    sequenceFile.getParentFile().mkdirs();
                    sequenceFile.createNewFile();
                    //com.sheffield.leapmotion.FileHandler.appendToFile(sequenceFile, "[");
                    start = true;
                }
                String content = Serializer.sequenceToJson(frame);
                if (!start) {
                    content = "\n" + content;
                }
                FileHandler.appendToFile(sequenceFile, content);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

    }

    public void outputSequence() throws IOException {
        if (currentSequence == null) {
            currentSequence = new File(FileHandler.generateFileWithName(filenameStart) + addition + "ms.sequence_hand_data");
            currentSequence.getParentFile().mkdirs();
            currentSequence.createNewFile();
        }
        FileHandler.appendToFile(currentSequence, uniqueId + ",");
    }

    public void outputJointPositionModel(String frameAsString) throws IOException {
        if (currentHands == null) {
            currentHands = new File(FileHandler.generateFileWithName(filenameStart) + addition + "ms.pool_joint_positions");
            currentHands.getParentFile().mkdirs();
            currentHands.createNewFile();
        }
        FileHandler.appendToFile(currentHands, frameAsString + "\n");
    }

    public void outputHandPositionModel(Hand h) throws IOException {
        if (currentPosition == null) {
            currentPosition = new File(FileHandler.generateFileWithName(filenameStart) + addition + "ms.pool_hand_positions");
            currentPosition.getParentFile().mkdirs();
            currentPosition.createNewFile();
        }
        String position = uniqueId + "," + h.palmPosition().getX() + ","
                + h.palmPosition().getY() + "," + h.palmPosition().getZ() + "\n";
        FileHandler.appendToFile(currentPosition, position);
    }

    public void outputHandRotationModel(Hand h) throws IOException {

        if (currentRotation == null) {
            currentRotation = new File(FileHandler.generateFileWithName(filenameStart) + addition + "ms.pool_hand_rotations");
            currentRotation.getParentFile().mkdirs();
            currentRotation.createNewFile();
        }

        String rotation = uniqueId + ",";
        Vector[] vectors = new Vector[3];
        vectors[0] = h.basis().getXBasis();
        vectors[1] = h.basis().getYBasis();
        vectors[2] = h.basis().getZBasis();
        for (Vector v : vectors) {
            rotation += v.getX() + "," + v.getY() + "," + v.getZ() + ",";
        }
        rotation.substring(0, rotation.length() - 1);
        rotation += "\n";
        FileHandler.appendToFile(currentRotation, rotation);
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
            currentDct = new File(FileHandler.generateFileWithName(filenameStart) + addition + "ms.pool_dct");
            currentDct.getParentFile().mkdirs();
            currentDct.createNewFile();
        }
        if (currentDctGestures == null) {
            currentDctGestures = new File(FileHandler.generateFileWithName(filenameStart) + addition + "ms.pool_dct_gestures");
            currentDctGestures.getParentFile().mkdirs();
            currentDctGestures.createNewFile();
        }
        if (lastStateCapture + STATE_CAPTURE_TIME < System.currentTimeMillis()) {
            App.out.print(" Capturing State");
            lastStateCapture = System.currentTimeMillis();

            try {
                String output = DctStateComparator.captureState();

                if (output != null && output.length() > 0) {
                    FileHandler.appendToFile(currentDct, "\n" + output + ":");
                    FileHandler.appendToFile(currentDctGestures, "\n" + output + ":");

                }
            } catch (Exception e){
                e.printStackTrace(App.out);
            }


        }
        handIds.add(uniqueId);
        gestures.add(currentGesture);
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
                if (gestures.get(i).length() > 1) {
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
        if (currentGestures == null) {
            App.out.println("- New Gestures File: " + filenameStart);
            currentGestures = new File(FileHandler.generateFileWithName(filenameStart) + addition + "ms.sequence_gesture_data");
            currentGestures.getParentFile().mkdirs();
            currentGestures.createNewFile();

            currentGesturesCircle = new File(FileHandler.generateFileWithName(filenameStart) + addition + "ms.pool_gesture_circle");
            currentGesturesCircle.getParentFile().mkdirs();
            currentGesturesCircle.createNewFile();

            currentGesturesSwipe = new File(FileHandler.generateFileWithName(filenameStart) + addition + "ms.pool_gesture_swipe");
            currentGesturesSwipe.getParentFile().mkdirs();
            currentGesturesSwipe.createNewFile();

            currentGesturesScreenTap = new File(FileHandler.generateFileWithName(filenameStart) + addition + "ms.pool_gesture_screentap");
            currentGesturesScreenTap.getParentFile().mkdirs();
            currentGesturesScreenTap.createNewFile();

            currentGesturesKeyTap = new File(FileHandler.generateFileWithName(filenameStart) + addition + "ms.pool_gesture_keytap");
            currentGesturesKeyTap.getParentFile().mkdirs();
            currentGesturesKeyTap.createNewFile();
        }
        String gestureString = "";
        if (frame.gestures().count() > 0) {


            for (Gesture g : frame.gestures()) {
                gestureString += g.type() + "+";

                switch (g.type()) {
                    case TYPE_CIRCLE:
                        CircleGesture cg = new CircleGesture(g);
                        String circleGesture = cg.center().getX() + "," +
                                cg.center().getY() + "," +
                                cg.center().getZ() + ",";

                        circleGesture += cg.normal().getX() + "," +
                                cg.normal().getY() + "," +
                                cg.normal().getZ() + ",";

                        circleGesture += cg.radius();

                        circleGesture += cg.duration() + "\n";
                        FileHandler.appendToFile(currentGesturesCircle, circleGesture);
                        break;
                    case TYPE_SWIPE:
                        SwipeGesture sg = new SwipeGesture(g);
                        String swipeGesture = sg.startPosition().getX() + "," +
                                sg.startPosition().getY() + "," +
                                sg.startPosition().getZ() + ",";

                        swipeGesture += sg.position().getX() + "," +
                                sg.position().getY() + "," +
                                sg.position().getZ() + ",";

                        swipeGesture += sg.direction().getX() + "," +
                                sg.direction().getY() + "," +
                                sg.direction().getZ() + ",";

                        swipeGesture += sg.speed() + "\n";

                        FileHandler.appendToFile(currentGesturesSwipe, swipeGesture);
                        break;
                    case TYPE_SCREEN_TAP:
                        ScreenTapGesture stg = new ScreenTapGesture(g);

                        String screenTapGesture = stg.position().getX() + "," +
                                stg.position().getY() + "," +
                                stg.position().getZ() + ",";

                        screenTapGesture += stg.direction().getX() + "," +
                                stg.direction().getY() + "," +
                                stg.direction().getZ() + ",";

                        screenTapGesture += stg.progress();

                        FileHandler.appendToFile(currentGesturesScreenTap, screenTapGesture);
                        break;

                    case TYPE_KEY_TAP:
                        KeyTapGesture ktg = new KeyTapGesture(g);

                        String keyTapGesture = ktg.position().getX() + "," +
                                ktg.position().getY() + "," +
                                ktg.position().getZ() + ",";

                        keyTapGesture += ktg.direction().getX() + "," +
                                ktg.position().getY() + "," +
                                ktg.position().getZ() + ",";

                        keyTapGesture += ktg.progress() + "\n";

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
        FileHandler.appendToFile(currentGestures, gestureString + " ");
    }


}
