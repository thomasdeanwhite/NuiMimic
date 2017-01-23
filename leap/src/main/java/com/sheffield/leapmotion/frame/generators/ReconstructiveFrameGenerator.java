package com.sheffield.leapmotion.frame.generators;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.GestureList;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.frame.util.BezierHelper;
import com.sheffield.leapmotion.frame.util.QuaternionHelper;
import com.sheffield.leapmotion.util.FileHandler;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.frame.util.Quaternion;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.frame.generators.gestures.GestureHandler;
import com.sheffield.leapmotion.frame.generators.gestures.ReconstructiveGestureHandler;
import com.sheffield.leapmotion.controller.mocks.HandFactory;
import com.sheffield.leapmotion.controller.mocks.SeededFrame;
import com.sheffield.leapmotion.controller.mocks.SeededHand;
import com.sheffield.output.Csv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by thoma on 11/05/2016.
 */
public class ReconstructiveFrameGenerator extends FrameGenerator implements GestureHandler {

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
    private ArrayList<String> positionLabelStack;
    private ArrayList<String> rotationLabelStack;

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


    public ReconstructiveFrameGenerator(String filename){
        try {
            currentRotations = new ArrayList<Quaternion>();
            currentPositions = new ArrayList<Vector>();
            currentHands = new ArrayList<SeededHand>();
            tpgh = new ReconstructiveGestureHandler(filename);
            App.out.println("* Setting up Reconstruction");
            lastSwitchTime = 0;
            currentAnimationTime = 0;
            handLabelStack = new ArrayList<String>();
            positionLabelStack = new ArrayList<String>();
            rotationLabelStack = new ArrayList<String>();

            handLabelStack.add(0, null);
            positionLabelStack.add(0, null);
            rotationLabelStack.add(0, null);

            String clusterFile = Properties.DIRECTORY + "/" + filename + ".joint_position_data";
            hands = new HashMap<String, SeededHand>();

            currentLabel = 0;

            String contents = FileHandler.readFile(new File(clusterFile));
            String[] lines = contents.split("\n");
            for (String line : lines) {
                Frame f = SeededController.newFrame();
                SeededHand hand = HandFactory.createHand(line, f);

                hands.put(hand.getUniqueId(), hand);

                HandFactory.injectHandIntoFrame(f, hand);

            }

            String sequenceFile = Properties.DIRECTORY + "/" + filename + ".raw_sequence.joint_position_data";
            String sequenceInfo = FileHandler.readFile(new File(sequenceFile));
            String[] seqData = sequenceInfo.split("\n")[0].split(",");

            for (String s : seqData){
                if (s.length() > 0)
                    handLabelStack.add(s);
            }

            timings = new ArrayList<Long>();
            //final ArrayList<Long> timings = new ArrayList<Long>();

            String[] tim = sequenceInfo.split("\n")[1].split(",");



            for (String s : tim){
                if (s.length() > 0)
                    // x / 1000 microsec to millisec
                    timings.add(Long.parseLong(s.split("@")[0])/1000);
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

            handLabelStack.sort(new ListComparator<String>(indices));

            for (int i = 1; i < timings.size()-1; i++){
                long l = timings.get(i);
                if (l > timings.get(i+1)) {
                    throw new IllegalArgumentException("Timings must increase chronologically");
                }
            }


            timings.remove(0);
            timings.add(0, 0L);
            timings.add(0, 0L);

            String positionFile = Properties.DIRECTORY + "/" + filename + ".hand_position_data";
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

            sequenceFile = Properties.DIRECTORY + "/" + filename + ".raw_sequence.hand_position_data";
            sequenceInfo = FileHandler.readFile(new File(sequenceFile));
            seqData = sequenceInfo.split("\n")[0].split(",");

            for (String s : seqData){
                if (s.length() > 0)
                    positionLabelStack.add(s);
            }

            positionLabelStack.sort(new ListComparator<String>(indices));

            String rotationFile = Properties.DIRECTORY + "/" + filename + ".hand_rotation_data";
            contents = FileHandler.readFile(new File(rotationFile));
            lines = contents.split("\n");
            rotations = new HashMap<String, Quaternion>();
            for (String line : lines) {
                String[] vect = line.split(",");
                Quaternion q = new Quaternion(Float.parseFloat(vect[1]),
                        Float.parseFloat(vect[2]),
                        Float.parseFloat(vect[3]),
                        Float.parseFloat(vect[4])).normalise();

                rotations.put(vect[0], q.inverse());

            }

            sequenceFile = Properties.DIRECTORY + "/" + filename + ".raw_sequence.hand_rotation_data";
            sequenceInfo = FileHandler.readFile(new File(sequenceFile));
            seqData = sequenceInfo.split("\n")[0].split(",");

            for (String s : seqData){
                if (s.length() > 0)
                    rotationLabelStack.add(s);
            }

            rotationLabelStack.sort(new ListComparator<String>(indices));

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
        if (handLabelStack.size() == 0){
//          App.out.println("Finished seeding hands after " + (System.currentTimeMillis() - startSeededTime) + "secs "  + SeededController.getSeededController().now());
            return Frame.invalid();
        }
        Frame f = SeededController.newFrame();
//        ArrayList<SeededHand> hs = new ArrayList<SeededHand>();
//        hs.add(hands.get(handLabelStack.get(0)));

        modifier = Math.min(1f, currentAnimationTime / animationTime);


        Hand hand = currentHands.get(0).fadeHand(currentHands, modifier);

        if (hand != null) {
            f = HandFactory.injectHandIntoFrame(f, hand);
        }

        return f;
    }

    @Override
    public String status() {
        return (handLabelStack.size() - currentHandIndex) + " hands";
    }

    @Override
    public GestureList handleFrame(Frame frame) {
        return tpgh.handleFrame(frame);
    }

    public int size(){
        return handLabelStack.size();
    }


    private long lastUpdate = 0;

    int currentHandIndex = 0;

    @Override
    public void tick(long time) {
        if (lastSwitchTime == 0){
            lastSwitchTime = time;
        }

        if (handLabelStack.size() == 0){
            return;
        }

        if (startSeededTime == 0){
            startSeededTime = time;
        }

        seededTime = time - startSeededTime;

        if (seededTime >= timings.get(currentHandIndex)) {

            App.out.println(seededTime - timings.get(currentHandIndex));

            lastSwitchTime = seededTime;

            currentHands.clear();
            currentPositions.clear();
            currentRotations.clear();

            while (timings.get(currentHandIndex) < seededTime + Properties.SWITCH_TIME) {

                String currentHand = null;
                String currentPosition = null;
                String currentRotation = null;

                do {
                    if (handLabelStack.size() > 0) {
                        currentHand = handLabelStack.get(currentHandIndex);
                        currentPosition = positionLabelStack.get
                                (currentHandIndex);
                        currentRotation = rotationLabelStack.get
                                (currentHandIndex);
                        tpgh.changeGesture();

                        currentHandIndex++;
                    }
                }
                while (currentHand == null || currentPosition == null || currentRotation == null);

                currentHands.add(hands.get(currentHand));
                currentPositions.add(vectors.get(currentPosition));
                currentRotations.add(rotations.get(currentRotation));
            }
        }

        currentAnimationTime = (int) (time - lastSwitchTime);
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
}
