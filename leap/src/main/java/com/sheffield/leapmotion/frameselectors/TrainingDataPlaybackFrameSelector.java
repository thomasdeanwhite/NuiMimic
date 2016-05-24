package com.sheffield.leapmotion.frameselectors;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.GestureList;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.BezierHelper;
import com.sheffield.leapmotion.FileHandler;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.Quaternion;
import com.sheffield.leapmotion.QuaternionHelper;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.controller.gestures.GestureHandler;
import com.sheffield.leapmotion.controller.gestures.TrainingPlaybackGestureHandler;
import com.sheffield.leapmotion.framemodifier.FrameModifier;
import com.sheffield.leapmotion.mocks.HandFactory;
import com.sheffield.leapmotion.mocks.SeededFrame;
import com.sheffield.leapmotion.mocks.SeededHand;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by thoma on 11/05/2016.
 */
public class TrainingDataPlaybackFrameSelector extends FrameSelector implements FrameModifier, GestureHandler {

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

    private TrainingPlaybackGestureHandler tpgh;


    public TrainingDataPlaybackFrameSelector (String filename){
        try {
            tpgh = new TrainingPlaybackGestureHandler(filename);
            App.out.println("* Setting up NGram Frame Selection");
            lastSwitchTime = System.currentTimeMillis();
            currentAnimationTime = 0;
            handLabelStack = new ArrayList<String>();
            positionLabelStack = new ArrayList<String>();
            rotationLabelStack = new ArrayList<String>();
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
            String[] seqData = sequenceInfo.split(",");

            for (String s : seqData){
                handLabelStack.add(s);
            }

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
            seqData = sequenceInfo.split(",");

            for (String s : seqData){
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

                rotations.put(vect[0], q);

            }

            sequenceFile = Properties.DIRECTORY + "/" + filename + ".raw_sequence.hand_rotation_data";
            sequenceInfo = FileHandler.readFile(new File(sequenceFile));
            seqData = sequenceInfo.split(",");

            for (String s : seqData){
                rotationLabelStack.add(s);
            }

        } catch (IOException e){
            e.printStackTrace(App.out);
        }
    }

    @Override
    public void modifyFrame(SeededFrame frame) {
        Hand h = Hand.invalid();
        for (Hand hand : frame.hands()) {
            h = hand;
        }
        if (h instanceof SeededHand) {
            SeededHand sh = (SeededHand) h;

            Quaternion lastRotation = rotations.get(currentRotation);

            float modifier = currentAnimationTime / (float)Properties.SWITCH_TIME;

            Quaternion q = QuaternionHelper.fadeQuaternions(lastRotation, rotations.get(rotationLabelStack.get(0)), modifier);

            //Vector[] rotationVectors = q.toMatrix();




            //sh.setBasis(rotationVectors[0], rotationVectors[1], rotationVectors[2]);
//            sh.setRotation(q);

            Vector axis = q.getAxis();
            float angle = q.getAngle();

            sh.setRotation(axis, angle);
//
            ArrayList<Vector> seededPositions = new ArrayList<Vector>();

            seededPositions.add(vectors.get(currentPosition));
            seededPositions.add(vectors.get(positionLabelStack.get(0)));

            sh.setOrigin(BezierHelper.bezier(seededPositions, modifier));
        }
    }

    @Override
    public Frame newFrame() {
        long time = System.currentTimeMillis();
        if (currentHand == null || currentPosition == null || currentRotation == null||  currentAnimationTime > Properties.SWITCH_TIME){

            currentHand = handLabelStack.remove(0);
            currentPosition = positionLabelStack.remove(0);
            currentRotation = rotationLabelStack.remove(0);
            tpgh.changeGesture();

            currentAnimationTime = 0;
            lastSwitchTime = time;
        } else {
            currentAnimationTime = (int) (time - lastSwitchTime);
        }
        Frame f = SeededController.newFrame();
        ArrayList<SeededHand> hs = new ArrayList<SeededHand>();
        hs.add((SeededHand)  hands.get(handLabelStack.get(0)));
        Hand hand = hands.get(currentHand);
        float modifier = currentAnimationTime / (float)Properties.SWITCH_TIME;
        f = HandFactory.injectHandIntoFrame(f, ((SeededHand)hand).fadeHand(hs, modifier));
        return f;
    }

    @Override
    public GestureList handleFrame(Frame frame) {
        return tpgh.handleFrame(frame);
    }
}
