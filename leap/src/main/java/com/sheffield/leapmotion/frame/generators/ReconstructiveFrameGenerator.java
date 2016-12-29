package com.sheffield.leapmotion.frame.generators;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.GestureList;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.App;
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
import java.util.HashMap;

/**
 * Created by thoma on 11/05/2016.
 */
public class ReconstructiveFrameGenerator extends FrameGenerator implements GestureHandler {

    @Override
    public Csv getCsv() {
        return new Csv();
    }

    long lastSwitchTime = 0;
    int currentAnimationTime = 0;
    int currentLabel = 0;

    private HashMap<String, SeededHand> hands;
    private HashMap<String, Vector> vectors;
    private HashMap<String, Quaternion> rotations;

    private ArrayList<String> handLabelStack;
    private ArrayList<String> positionLabelStack;
    private ArrayList<String> rotationLabelStack;

    private String currentHand;
    private String currentPosition;
    private String currentRotation;
    private ArrayList<Long> timings;

    private long startTime = 0;

    private ReconstructiveGestureHandler tpgh;

    private long startSeededTime = 0;
    private long seededTime = 0;

    private int animationTime = 0;


    public ReconstructiveFrameGenerator(String filename){
        try {
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
            ArrayList<Long> timings = new ArrayList<Long>();

            String[] tim = sequenceInfo.split("\n")[1].split(",");

            for (String s : tim){
                if (s.length() > 0)
                    // x / 1000 microsec to millisec
                    timings.add(Long.parseLong(s.split("@")[0])/1000);
            }

            for (int i = 1; i < timings.size()-1; i++){
                long l = timings.get(i);
                if (l < timings.get(i+1)) {
                    long time = l - timings.get(0);
                    this.timings.add(time);
                } else {
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

        } catch (IOException e){
            e.printStackTrace(App.out);
        }
    }

    @Override
    public void modifyFrame(SeededFrame frame) {
        if (handLabelStack.size() == 0 || currentPosition == null
                || currentRotation == null){
            return;
        }

        Hand h = Hand.invalid();
        for (Hand hand : frame.hands()) {
            h = hand;
        }
        if (h instanceof SeededHand) {
            SeededHand sh = (SeededHand) h;


            Quaternion q = rotations.get(currentRotation);
            q.setBasis(sh);

            sh.setOrigin(vectors.get(currentPosition));
        }
        currentAnimationTime = (int) (System.currentTimeMillis() - lastSwitchTime);
    }

    @Override
    public boolean allowProcessing() {
        return true;
    }

    @Override
    public Frame newFrame() {
        if (handLabelStack.size() == 0){
//          App.out.println("Finished seeding hands after " + (System.currentTimeMillis() - startSeededTime) + "secs "  + SeededController.getSeededController().now());
            return Frame.invalid();
        }
        Frame f = SeededController.newFrame();
//        ArrayList<SeededHand> hs = new ArrayList<SeededHand>();
//        hs.add(hands.get(handLabelStack.get(0)));
//        float modifier = Math.max(1f, currentAnimationTime / animationTime);


        Hand hand = hands.get(currentHand);

        if (hand != null) {
            f = HandFactory.injectHandIntoFrame(f, hand);
        }

        return f;
    }

    @Override
    public String status() {
        return handLabelStack.size() + " hands";
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

        App.out.println(seededTime + " " + timings.get(currentHandIndex) +
                "\n");

        if (seededTime > timings.get(currentHandIndex)) {
            do {
                if (handLabelStack.size() > 0) {
                    currentHand = handLabelStack.get(currentHandIndex);
                    currentPosition = positionLabelStack.get
                            (currentHandIndex);
                    currentRotation = rotationLabelStack.get
                            (currentHandIndex);
                    tpgh.changeGesture();
                    if (timings.size() > currentHandIndex+1) {
                        animationTime = (int) (timings.get(currentHandIndex++)
                                - timings.get(currentHandIndex));
                    } else {
                        animationTime = 30;
                    }
                    lastSwitchTime = time;

                    currentHandIndex++;
                }
            } while (currentHand == null || currentPosition == null || currentRotation == null);
        }

        currentAnimationTime = (int) (time - lastSwitchTime);
        if (animationTime <= 0){
            animationTime = 1;
        }

        tpgh.tick(time);

        lastUpdate = time;
    }

    public long lastTick(){
        return lastUpdate;
    }

    @Override
    public void cleanUp() {

    }
}
