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
import com.sheffield.output.Csv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by thoma on 11/05/2016.
 */
public class RawReconstructiveFrameGenerator extends FrameGenerator implements GestureHandler, Reconstruction {

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

    private ArrayList<SeededHand> currentHands;
    private ArrayList<Vector> currentPositions;
    private ArrayList<Quaternion> currentRotations;
    private ArrayList<Long> timings;

    private long lastSwitchTime = 0;

    private long startTime = 0;

    private ReconstructiveGestureHandler tpgh;

    private long startSeededTime = 0;
    private long seededTime = 0;

    private int animationTime = 0;


    public RawReconstructiveFrameGenerator(String filename){
        try {
            currentRotations = new ArrayList<Quaternion>();
            currentPositions = new ArrayList<Vector>();
            currentHands = new ArrayList<SeededHand>();
            tpgh = new ReconstructiveGestureHandler(filename);
            App.out.println("* Setting up Reconstruction");
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

            for (String line : lines) {
                if (data && line.trim().length() > 0) {
                    Frame f = SeededController.newFrame();
                    SeededHand hand = HandFactory.createHand(line, f);

                    hands.put(hand.getUniqueId(), hand);

                    HandFactory.injectHandIntoFrame(f, hand);
                } else {
                    if (line.contains("@DATA")){
                        data = true;
                    }
                }

            }

            String sequenceFile = Properties.DIRECTORY + "/" + filename + "/processed/" +
                    (Properties.SINGLE_DATA_POOL ? "hand_joints.raw_sequence" : "joint_position.raw_sequence");
            String sequenceInfo = FileHandler.readFile(new File(sequenceFile));

            timings = new ArrayList<Long>();
            //final ArrayList<Long> timings = new ArrayList<Long>();

            String[] tim = sequenceInfo.split("\n")[1].split(",");



            for (String s : tim){
                if (s.length() > 0) {
                    // x / 1000 microsec to millisec
                    timings.add(Long.parseLong(s.split("@")[0]));
                    handLabelStack.add(s);
                }
            }

            final ArrayList<Integer> indices = new ArrayList<Integer>();

            for (int i = 0; i < timings.size(); i++){
                indices.add(i);
            }

            final ArrayList<Long> tims = timings;

            indices.sort(new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return (int)(tims.get(o1) - tims.get(o2));
                }
            });

            timings.sort(new ListComparator<Long>(indices));

//            //remove repeated times
//            for (int i = 0; i < timings.size(); i++){
//                long time = timings.remove(i);
//
//                if (!timings.contains(time)){
//                    timings.add(i, time);
//                }
//            }

            handLabelStack.sort(new ListComparator<String>(indices));

            long first = tims.get(indices.get(0));

            for (int i = timings.size()-2; i >= 0; i--){
                long l = timings.get(i);
                long l1 = timings.get(i+1);
                if (l > l1) {
                    throw new IllegalArgumentException("Timings must increase chronologically");
                } else {
                    long m = timings.remove(i+1);
                    timings.add(i+1, (m-first)/1000);
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

            for (String line : lines) {
                if (data && line.trim().length() > 0) {
                    Vector v = new Vector();
                    String[] vect = line.split(",");
                    v.setX(Float.parseFloat(vect[1]));
                    v.setY(Float.parseFloat(vect[2]));
                    v.setZ(Float.parseFloat(vect[3]));

                    vectors.put(vect[0], v);
                } else {
                    if (line.contains("@DATA")){
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

            for (String line : lines) {
                if (data && line.trim().length() > 0) {
                    String[] vect = line.split(",");
                    Quaternion q = new Quaternion(Float.parseFloat(vect[1]),
                            Float.parseFloat(vect[2]),
                            Float.parseFloat(vect[3]),
                            Float.parseFloat(vect[4])).normalise();

                    rotations.put(vect[0], q.inverse());
                } else {
                    if (line.contains("@DATA")){
                        data = true;
                    }
                }

            }
        } catch (IOException e){
            e.printStackTrace(App.out);
        }
    }

    private class ListComparator<T> implements Comparator<T> {

        private ArrayList<Integer> indices;

        public ListComparator(ArrayList<Integer> indices){
            this.indices = indices;
        }

        @Override
        public int compare(T o1, T o2) {
            return indices.indexOf(timings.indexOf(o1)) -
                    indices.indexOf(timings.indexOf(o2));
        }
    }

    private int gestureHandIndex = 0;

    @Override
    public void modifyFrame(SeededFrame frame) {
        if (handLabelStack.size() == 0 || currentPositions == null
                || currentRotations == null || currentPositions.size() == 0 || currentRotations.size() == 0){
            return;
        }

        Hand h = Hand.invalid();
        for (Hand hand : frame.hands()) {
            h = hand;
        }
        if (h instanceof SeededHand) {
            SeededHand sh = (SeededHand) h;

            Quaternion q = QuaternionHelper.fadeQuaternions(currentRotations, modifier);
            q.setBasis(sh);

            sh.setOrigin(BezierHelper.bezier(currentPositions, modifier));
        }
    }

    @Override
    public boolean allowProcessing() {
        return true;
    }

    float modifier = 0f;


    @Override
    public Frame newFrame() {

        if (gestureHandIndex > timings.size() || currentHandIndex > timings.size()){
            App.getApp().setStatus(AppStatus.FINISHED);
            return Frame.invalid();
        }

        if (handLabelStack.size() == 0){
            return Frame.invalid();
        }
        Frame f = SeededController.newFrame();

         modifier = Math.min(1f, currentAnimationTime / Properties.SWITCH_TIME);

        if (currentHands.size() > 0){
            Hand hand = currentHands.get(0).fadeHand(currentHands, modifier);

            if (hand != null) {
                f = HandFactory.injectHandIntoFrame(f, hand);
            }
        } else {
            return null;
        }

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

    public int size(){
        return handLabelStack.size();
    }


    private long lastUpdate = 0;

    int currentHandIndex = 0;

    @Override
    public void tick(long time) {
        if (handLabelStack.size() == 0){
            return;
        }
        if (lastSwitchTime == 0){
            lastSwitchTime = time;
        }

        if (currentHandIndex >= timings.size()){
            return;
        }

        if (startSeededTime == 0){
            startSeededTime = time;
        }

        seededTime = time - startSeededTime;

        long frameTime = timings.get(currentHandIndex) - Properties.SWITCH_TIME;

        if (gestureHandIndex > timings.size() || currentHandIndex > timings.size()){
            App.getApp().setStatus(AppStatus.FINISHED);
            return;
        }

        while (seededTime  > timings.get(gestureHandIndex) - Properties.SWITCH_TIME){
            tpgh.changeGesture(gestureHandIndex++);
        }

        if (seededTime > frameTime) {

//            App.out.println((seededTime - timings.get(currentHandIndex)) + " "
//                    + seededTime + " " + timings.get(currentHandIndex));

            lastSwitchTime = seededTime - Properties.SWITCH_TIME;

            currentHands.clear();
            currentPositions.clear();
            currentRotations.clear();

            gestureHandIndex = currentHandIndex;


            int skippedHands = 0;
            long newFrameTime = timings.get(currentHandIndex) - Properties.SWITCH_TIME;

            while(newFrameTime < seededTime - Properties.SWITCH_TIME){
                newFrameTime = timings.get(currentHandIndex+skippedHands) -
                        Properties.SWITCH_TIME;
                skippedHands++;
            }

            if (skippedHands != 0){
                currentHandIndex += (skippedHands-1);
            }

            frameTime = timings.get(currentHandIndex) - Properties.SWITCH_TIME;

            while (frameTime < seededTime) {

                String currentHand = null;
                String currentPosition = null;
                String currentRotation = null;

                do {
                    if (handLabelStack.size() > 0) {
                        currentHand = handLabelStack.get(currentHandIndex);
                        currentPosition = handLabelStack.get
                                (currentHandIndex);
                        currentRotation = handLabelStack.get
                                (currentHandIndex);

                        frameTime = timings.get(currentHandIndex++) -
                                Properties.SWITCH_TIME;
                    }
                }
                while (currentHand == null || currentPosition == null || currentRotation == null);

                if (currentHands.size() == 0 || !currentHands.get(currentHands.size()-1).equals(hands.get(currentHand))) {
                    currentHands.add(hands.get(currentHand));
                }

                if (currentPositions.size() == 0 || !currentPositions.get(currentPositions.size()-1).equals(vectors.get(currentPosition))) {
                    currentPositions.add(vectors.get(currentPosition));
                }

                if (currentRotations.size() == 0 ||!currentRotations.get(currentRotations.size()-1).equals(rotations.get(currentRotation))){
                    currentRotations.add(rotations.get(currentRotation));
                }
            }
        }

        currentAnimationTime = (int) (seededTime - lastSwitchTime);
        if (animationTime <= 0){
            animationTime = 1;
        }

        tpgh.tick(time);

        lastUpdate = time;
    }

    @Override
    public boolean hasNextFrame (){
        return currentHandIndex < handLabelStack.size();
    }

    public long lastTick(){
        return lastUpdate;
    }

    @Override
    public void cleanUp() {

    }

    @Override
    public int getClusters(){
        return hands.size();
    }

    @Override
    public void setFrame(int index) {
        currentHandIndex = index;
    }
}
