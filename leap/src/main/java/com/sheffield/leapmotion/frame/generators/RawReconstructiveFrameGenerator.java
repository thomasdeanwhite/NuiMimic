package com.sheffield.leapmotion.frame.generators;

import com.leapmotion.leap.*;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.controller.mocks.HandFactory;
import com.sheffield.leapmotion.controller.mocks.SeededFrame;
import com.sheffield.leapmotion.controller.mocks.SeededHand;
import com.sheffield.leapmotion.frame.generators.gestures.GestureHandler;
import com.sheffield.leapmotion.frame.generators.gestures
        .ReconstructiveGestureHandler;
import com.sheffield.leapmotion.frame.util.BezierHelper;
import com.sheffield.leapmotion.frame.util.Quaternion;
import com.sheffield.leapmotion.frame.util.QuaternionHelper;
import com.sheffield.leapmotion.util.AppStatus;
import com.sheffield.leapmotion.util.FileHandler;
import com.sheffield.leapmotion.util.ProgressBar;
import com.sheffield.output.Csv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by thoma on 11/05/2016.
 */
public class RawReconstructiveFrameGenerator extends FrameGenerator
        implements GestureHandler, Reconstruction {

    @Override
    public Csv getCsv() {
        return new Csv();
    }

    int currentAnimationTime = 0;
    int currentLabel = 0;

    private HashMap<String, SeededHand> hands;
    private HashMap<String, Vector> vectors;
    private HashMap<String, Quaternion> rotations;

    private ArrayList<String> handLabelStack;

    private SeededHand currentHand;
    private Vector currentPosition;
    private Quaternion currentRotation;
    private ArrayList<Long> timings;

    private long lastSwitchTime = 0;

    private long startTime = 0;

    private ReconstructiveGestureHandler tpgh;

    private long startSeededTime = 0;
    private long seededTime = 0;

    private int animationTime = 0;

    private long lastUpdate = 0;

    private int currentHandIndex = 0;


    public RawReconstructiveFrameGenerator(String filename) {
        try {
            tpgh = new ReconstructiveGestureHandler(filename);
            App.out.println("* Setting up Raw Reconstruction");
            lastSwitchTime = 0;
            currentAnimationTime = 0;
            handLabelStack = new ArrayList<String>();

            handLabelStack.add(0, null);

            String clusterFile = Properties.DIRECTORY + "/" + filename + "/" +
                    (Properties.SINGLE_DATA_POOL ? "hand_joints_pool.ARFF" :
                            "joint_positions_pool.ARFF");
            hands = new HashMap<String, SeededHand>();

            currentLabel = 0;

            String contents = FileHandler.readFile(new File(clusterFile));
            String[] lines = contents.split("\n");

            boolean data = false;

            App.out.println(ProgressBar.getHeaderBar(21));

            int counter = 0;

            for (String line : lines) {

                App.out.print("\r"+ProgressBar.getProgressBar(21, counter++ / (float)lines.length) + "[1 of 4]");

                if (data && line.trim().length() > 0) {
                    Frame f = SeededController.newFrame();
                    SeededHand hand = HandFactory.createHand(line, f);

                    hands.put(hand.getUniqueId(), hand);

                    HandFactory.injectHandIntoFrame(f, hand);
                } else {
                    if (line.contains("@DATA")) {
                        data = true;
                    }
                }

            }

            String sequenceFile =
                    Properties.DIRECTORY + "/" + filename + "/processed/" +
                            (Properties.SINGLE_DATA_POOL ?
                                    "hand_joints.raw_sequence" :
                                    "joint_position.raw_sequence");
            String sequenceInfo = FileHandler.readFile(new File(sequenceFile));

            timings = new ArrayList<Long>();
            //final ArrayList<Long> timings = new ArrayList<Long>();

            String[] tim = sequenceInfo.split("\n")[1].split(",");

            counter = 0;

            for (String s : tim) {
                App.out.print("\r"+ProgressBar.getProgressBar(21, counter++ / (float)tim.length) + "[2 of 4]");
                if (s.length() > 0) {
                    // x / 1000 microsec to millisec
                    timings.add(Long.parseLong(s.split("@")[0]));
                    handLabelStack.add(s);
                }
            }

            final ArrayList<Integer> indices = new ArrayList<Integer>();

            for (int i = 0; i < timings.size(); i++) {
                indices.add(i);
            }

            final ArrayList<Long> tims = timings;

            //This will sort the indices array so other lists can be
            // reordered using the indices
            indices.sort(new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return (int) (tims.get(o1) - tims.get(o2));
                }
            });

            timings.sort(new ListComparator<Long>(indices));

            handLabelStack.sort(new ListComparator<String>(indices));

            long first = tims.get(indices.get(0));

            for (int i = timings.size() - 2; i >= 0; i--) {
                long l = timings.get(i);
                long l1 = timings.get(i + 1);
                if (l > l1) {
                    throw new IllegalArgumentException(
                            "Timings must increase chronologically");
                } else {
                    long m = timings.remove(i + 1);
                    timings.add(i + 1, (m - first) / 1000);
                }
            }


            timings.remove(0);
            timings.add(0, 0L);

            String positionFile = Properties.DIRECTORY + "/" + filename +
                    "/hand_positions_pool.ARFF";
            contents = FileHandler.readFile(new File(positionFile));
            lines = contents.split("\n");
            vectors = new HashMap<String, Vector>();

            data = false;

            counter = 0;

            for (String line : lines) {

                App.out.print("\r"+ProgressBar.getProgressBar(21, counter++ / (float)lines.length) + "[3 of 4]");
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

            String rotationFile = Properties.DIRECTORY + "/" + filename +
                    "/hand_rotations_pool.ARFF";
            contents = FileHandler.readFile(new File(rotationFile));
            lines = contents.split("\n");
            rotations = new HashMap<String, Quaternion>();

            data = false;

            counter = 0;

            for (String line : lines) {

                App.out.print("\r"+ProgressBar.getProgressBar(21, counter++ / (float)lines.length) + "[4 of 4]");

                if (data && line.trim().length() > 0) {
                    String[] vect = line.split(",");
                    Quaternion q = new Quaternion(Float.parseFloat(vect[1]),
                            Float.parseFloat(vect[2]),
                            Float.parseFloat(vect[3]),
                            Float.parseFloat(vect[4])).normalise();

                    rotations.put(vect[0], q.inverse());
                } else {
                    if (line.contains("@DATA")) {
                        data = true;
                    }
                }

            }

            App.out.println(" done!");
        } catch (IOException e) {
            e.printStackTrace(App.out);
        }
    }

    private class ListComparator<T> implements Comparator<T> {

        private ArrayList<Integer> indices;

        public ListComparator(ArrayList<Integer> indices) {
            this.indices = indices;
        }

        @Override
        public int compare(T o1, T o2) {
            return indices.indexOf(timings.indexOf(o1)) -
                    indices.indexOf(timings.indexOf(o2));
        }
    }

    @Override
    public void modifyFrame(SeededFrame frame) {
        if (handLabelStack.size() == 0 || currentPosition == null
                || currentRotation == null) {
            return;
        }

        Hand h = Hand.invalid();
        for (Hand hand : frame.hands()) {
            if (hand.isValid() && h.isRight()) {
                h = hand;
                break;
            }
        }
        if (h instanceof SeededHand) {
            SeededHand sh = (SeededHand) h;

            currentRotation.setBasis(sh);

            sh.setOrigin(currentPosition);
        }
    }

    @Override
    public boolean allowProcessing() {
        return true;
    }

    float modifier = 0f;


    @Override
    public Frame newFrame() {

        if (currentHandIndex >= timings.size()) {
            //App.getApp().setStatus(AppStatus.FINISHED);
            return null;
        }

        if (handLabelStack.size() == 0) {
            return Frame.invalid();
        }

        SeededFrame f = null;

        if (currentHand != null) {
            f = (SeededFrame) currentHand.frame();
            f.setId(currentHand.id());

            f.setTimestamp(timings.get(currentHandIndex)*1000);
        }

        currentHandIndex++;

        return f;
    }

    @Override
    public String status() {
        return (handLabelStack.size() - currentHandIndex) + " hands";
    }

    @Override
    public GestureList handleFrame(Frame frame, Controller controller) {
        return tpgh.handleFrame(frame, controller);
    }

    public int size() {
        return handLabelStack.size();
    }

    private int discardedHands = 0;

    @Override
    public void tick(long time) {
        if (handLabelStack.size() == 0) {
            return;
        }
        if (lastSwitchTime == 0) {
            lastSwitchTime = time;
        }

        if (currentHandIndex >= timings.size()) {
            return;
        }

        if (startSeededTime == 0) {
            startSeededTime = time;
        }

        seededTime = time - startSeededTime;

        long frameTime = timings.get(currentHandIndex);

        if (currentHandIndex + Properties.GESTURE_CIRCLE_FRAMES >=
                timings.size() || currentHandIndex >= timings.size()) {
            App.getApp().setStatus(AppStatus.FINISHED);
            return;
        }

//
//        if (seededTime > frameTime) {

            lastSwitchTime = seededTime - Properties.SWITCH_TIME;

            currentHand = null;
            currentPosition = null;
            currentRotation = null;


            int skippedHands = 0;
            long newFrameTime = timings.get(currentHandIndex);

//            while (newFrameTime <= seededTime) {
//                newFrameTime = timings.get(currentHandIndex + skippedHands);
//                skippedHands++;
//            }
//
//            if (skippedHands != 0) {
//                currentHandIndex += (skippedHands - 1);
//            }
//
//            discardedHands += skippedHands;
//
//            frameTime = timings.get(currentHandIndex);

            String currentHand = null;
            String currentPosition = null;
            String currentRotation = null;

            if (handLabelStack.size() > 0) {
                currentHand = handLabelStack.get(currentHandIndex);
                currentPosition = handLabelStack.get
                        (currentHandIndex);
                currentRotation = handLabelStack.get
                        (currentHandIndex);

                frameTime = timings.get(currentHandIndex);
            }

            tpgh.changeGesture(
                    currentHandIndex + Properties.GESTURE_CIRCLE_FRAMES);


            assert (hands.get(currentHand) == null ||
                    vectors.get(currentPosition) == null ||
                    rotations.get(currentRotation) == null);

            this.currentHand = hands.get(currentHand);
            this.currentPosition = vectors.get(currentPosition);

            this.currentRotation = rotations.get(currentRotation);
//        }


        tpgh.tick(time);

        lastUpdate = time;
    }

    @Override
    public boolean hasNextFrame() {
        return currentHandIndex < handLabelStack.size();
    }

    @Override
    public String getName() {
        return "Raw Reconstruction";
    }

    public long lastTick() {
        return lastUpdate;
    }

    @Override
    public void cleanUp() {

    }

    @Override
    public int getClusters() {
        return hands.size();
    }

    @Override
    public void setFrame(int index) {
        currentHandIndex = index;
    }
}
