package com.sheffield.leapmotion.frameselectors;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.GestureList;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Pointable;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.BezierHelper;
import com.sheffield.leapmotion.FileHandler;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.Quaternion;
import com.sheffield.leapmotion.QuaternionHelper;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.controller.gestures.GestureHandler;
import com.sheffield.leapmotion.framemodifier.FrameModifier;
import com.sheffield.leapmotion.mocks.HandFactory;
import com.sheffield.leapmotion.mocks.SeededCircleGesture;
import com.sheffield.leapmotion.mocks.SeededFrame;
import com.sheffield.leapmotion.mocks.SeededGesture;
import com.sheffield.leapmotion.mocks.SeededGestureList;
import com.sheffield.leapmotion.mocks.SeededHand;
import com.sheffield.leapmotion.mocks.SeededSwipeGesture;
import com.sheffield.leapmotion.output.TestingStateComparator;
import com.sheffield.output.Csv;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class RegressiveFrameSelector extends FrameSelector implements FrameModifier, GestureHandler {

    private HashMap<String, SeededHand> hands;
    private ArrayList<SeededHand> seededHands;
    private ArrayList<String> seededLabels;
    private ArrayList<NGramLog> failures;
    private ArrayList<NGramLog> success;

    private ClusterPlayback[] clusterPlaybacks;

    private int currentAnimationTime = 0;
    private long lastSwitchTime = 0;
    private SeededHand lastHand;
    private String lastLabel;

    private HashMap<String, Vector> vectors;
    private HashMap<String, Quaternion> rotations;

    private Vector lastPosition;
    private String lastPositionLabel;
    private ArrayList<Vector> seededPositions = new ArrayList<Vector>();
    private ArrayList<String> positionLabels = new ArrayList<String>();

    private Quaternion lastRotation;
    private String lastRotationLabel;
    private ArrayList<Quaternion> seededRotations = new ArrayList<Quaternion>();
    private ArrayList<String> rotationLabels = new ArrayList<String>();
    private int currentModTime = 0;


    public RegressiveFrameSelector(String filename, ArrayList<NGramLog>[] ngLogs) {
        failures = new ArrayList<NGramLog>();
        success = new ArrayList<NGramLog>();
        try {
            clusterPlaybacks = new ClusterPlayback[ngLogs.length];
            for (int i = 0; i < ngLogs.length; i++) {
                clusterPlaybacks[i] = new ClusterPlayback(ngLogs[i]);
            }
            App.out.print("* Regressive Frame Selection");
            lastSwitchTime = System.currentTimeMillis();

            hands = new HashMap<String, SeededHand>();

            seededHands = new ArrayList<SeededHand>();
            seededLabels = new ArrayList<String>();
            String clusterFile = Properties.DIRECTORY + "/" + filename + ".joint_position_data";
            String contents = FileHandler.readFile(new File(clusterFile));
            String[] lines = contents.split("\n");
            for (String line : lines) {
                Frame f = SeededController.newFrame();
                SeededHand hand = HandFactory.createHand(line, f);

                hands.put(hand.getUniqueId(), hand);
                // order.add(hand.getUniqueId());

                HandFactory.injectHandIntoFrame(f, hand);

            }



            String positionFile = Properties.DIRECTORY + "/" + filename + ".hand_position_data";
            lastSwitchTime = System.currentTimeMillis();
            currentAnimationTime = 0;
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

            String sequenceFile = Properties.DIRECTORY + "/" + filename + ".hand_position_ngram";

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

                //App.out.println(vect[0] + ": " + q);

            }

            sequenceFile = Properties.DIRECTORY + "/" + filename + ".hand_rotation_ngram";
        } catch (IOException e) {
            e.printStackTrace(App.out);
        }

        App.out.println("\r* Regressive Setup Complete!");

    }

    private NGramLog lastHandN = null;

    @Override
    public Frame newFrame() {

        if (lastSwitchTime == 0){
            lastSwitchTime = System.currentTimeMillis();
        }

        if (lastHandN == null){
            lastHandN = clusterPlaybacks[0].getCurrentNGramLog(0);
        }

        NGramLog hand = clusterPlaybacks[0].getCurrentNGramLog(currentAnimationTime);
        String[] handLog = hand.element.split(",");

        if (lastHand == null){
            lastLabel = handLog[0];
            lastHand = hands.get(lastLabel);
        }
        while (seededHands.size() < handLog.length){
            if (!seededHands.contains(lastHand)){
                seededHands.clear();
                seededHands.add(0, lastHand);
                seededLabels.clear();
                seededLabels.add(lastLabel);
            } else {
                String label = handLog[seededHands.size()];
                Hand h = hands.get(label);
                if (h != null && h instanceof SeededHand) {
                    seededHands.add((SeededHand) h);
                    seededLabels.add(label);
                }
            }
        }
//		if (nextHand == null) {
//			nextHand = hands.get(analyzer.getDataAnalyzer().next());
//		}

        if (lastHandN != hand) {
            // load next frame
            currentAnimationTime = 0;
            lastHand = seededHands.get(seededHands.size() - 1);
            lastLabel = seededLabels.get(seededLabels.size() - 1);

            seededHands.clear();
            seededLabels.clear();

            lastSwitchTime = System.currentTimeMillis();

            if (seededPositions.size() > 0 && seededRotations.size() > 0) {


                lastPosition = seededPositions.get(seededPositions.size() - 1);
                lastPositionLabel = positionLabels.get(positionLabels.size() - 1);
                lastRotation = seededRotations.get(seededRotations.size() - 1);
                lastRotationLabel = rotationLabels.get(rotationLabels.size() - 1);

                seededPositions.clear();
                seededRotations.clear();
                positionLabels.clear();
                rotationLabels.clear();
            }

            int state = TestingStateComparator.getCurrentState();
            if (state != hand.state){
                failures.add(hand);
            } else {
                success.add(hand);
            }

           // App.out.print((success.size() / (float)(success.size() + failures.size())) + " success rate ");

            lastSwitchTime = System.currentTimeMillis();
            lastHandN = hand;
        }
        currentAnimationTime = (int) (System.currentTimeMillis() - lastSwitchTime);
        Frame f = SeededController.newFrame();
//        float modifier = Math.min(1, currentAnimationTime / (float) hand
//                .timeSeeded);
        Hand newHand = lastHand;//.fadeHand(seededHands, modifier);
        f = HandFactory.injectHandIntoFrame(f, newHand);

        lastHandN = hand;

        return f;
    }

    @Override
    public String status() {
        return "[" + (Math.round(100f * (success.size() / (float)(success.size() + failures.size())))/100f) + " matched]";
    }

    private long lastUpdate = 0;
    @Override
    public void tick(long time) {
        lastUpdate = time;
    }

    public long lastTick(){
        return lastUpdate;
    }

    private NGramLog lastPosN = null;
    private long lastPosSwitchTime = 0;

    @Override
    public void modifyFrame(SeededFrame frame) {

        if (lastPosSwitchTime == 0){
            lastPosSwitchTime = System.currentTimeMillis();
        }

        if (lastPosN == null){
            lastPosN =  clusterPlaybacks[1].getCurrentNGramLog(0);
        }


        NGramLog posLog = clusterPlaybacks[1].getCurrentNGramLog(currentModTime);

        if (posLog != lastPosN){
            currentModTime = 0;
            lastPosSwitchTime = System.currentTimeMillis();
        }

        String[] posG = posLog.element.split(",");
        String[] rotG = clusterPlaybacks[2].getCurrentNGramLog(currentModTime).element.split(",");
        if (lastPosition == null) {
            lastPositionLabel = posG[0];
            if (lastPositionLabel != null && !lastPositionLabel.equals("null")) {
                lastPosition = vectors.get(lastPositionLabel);
            }
        }

        if (lastRotation == null) {
            lastRotationLabel = rotG[0];
            if (lastRotationLabel != null && !lastRotationLabel.equals("null")) {
                lastRotation = rotations.get(lastRotationLabel);
            }
        }


        while (seededPositions.size() < posG.length) {
            if (seededPositions.contains(lastPosition)) {
                Vector position = null;
                String pLabel = null;
                while (position == null) {
                    pLabel = posG[seededPositions.size()];

                    if (pLabel != null) {
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

        while (seededRotations.size() < rotG.length) {
            if (seededRotations.contains(lastRotation)) {
                Quaternion rotation = null;
                String rLabel = null;
                while (rotation == null) {
                    rLabel = rotG[seededRotations.size()];

                    if (rLabel != null) {
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

        Hand h = Hand.invalid();
        for (Hand hand : frame.hands()) {
            h = hand;
        }
        if (h instanceof SeededHand) {
            float modifier = currentModTime / (float) posLog.timeSeeded;
            SeededHand sh = (SeededHand) h;

            Quaternion q = seededRotations.get(0);//QuaternionHelper
                    // .fadeQuaternions(seededRotations, modifier);

            q.setBasis(sh);
            sh.setOrigin(seededPositions.get(0));//BezierHelper.bezier
                    //(seededPositions, modifier));
        }
        //currentAnimationTime = (int) (System.currentTimeMillis() - lastSwitchTime);

        currentModTime = (int) (System.currentTimeMillis() - lastPosSwitchTime);

        lastPosN = posLog;
    }

    private long lastGestureSwitch = 0;

    private NGramLog lastGesture = null;

    @Override
    public GestureList handleFrame(Frame frame) {
        SeededGestureList gl = new SeededGestureList();

        if (lastGestureSwitch == 0){
            lastGestureSwitch = System.currentTimeMillis();
        }

        if (lastGesture == null){
            lastGesture = clusterPlaybacks[3].getCurrentNGramLog(0);
            gestureType = Gesture.Type.valueOf(lastGesture.element);
        }

        NGramLog gesture = clusterPlaybacks[3].getCurrentNGramLog(gestureDuration);

        if (gesture != lastGesture){
            //a switch has occurred
            lastGestureSwitch = System.currentTimeMillis();
            cumalitiveGesturePositions = Vector.zero();
            gestureCount = 0;
            gestureDuration = 3;
            gestureStart = System.currentTimeMillis()-gestureDuration;
            gestureType = Gesture.Type.valueOf(gesture.element);
            gestureState = Gesture.State.STATE_START;
        }

        gestureDuration = (int)(System.currentTimeMillis() - lastGestureSwitch);

        lastGesture = gesture;

        if (clusterPlaybacks[3].willExpire(gestureDuration)){
            gestureState = Gesture.State.STATE_STOP;
        } else {
            gestureState = Gesture.State.STATE_UPDATE;
        }

        if (gestureType == Gesture.Type.TYPE_INVALID)
            return gl;

        gestureCount++;

        gl.addGesture(setupGesture(gestureType, frame, gestureId));



        return gl;
    }

    private SeededCircleGesture scg;
    private SeededSwipeGesture ssg;
    private static final Gesture.Type[] TYPES = new Gesture.Type[]{Gesture.Type.TYPE_CIRCLE, Gesture.Type.TYPE_INVALID, Gesture.Type.TYPE_KEY_TAP, Gesture.Type.TYPE_SCREEN_TAP, Gesture.Type.TYPE_SWIPE};
    protected static final Random random = new Random();

    protected Gesture.State gestureState;
    protected Gesture.Type gestureType;
    protected long gestureStart;
    protected int gestureDuration;
    protected int gestureId = 0;
    protected Vector cumalitiveGesturePositions = Vector.zero();
    protected int gestureCount = 0;

    public Gesture setupGesture(Gesture.Type gestureType, Frame frame, int gestureId){
        Gesture g = new SeededGesture(gestureType, gestureState, frame, gestureDuration, gestureId);

        if (gestureType == Gesture.Type.TYPE_CIRCLE){
            scg = new SeededCircleGesture(g);
            cumalitiveGesturePositions = cumalitiveGesturePositions.plus(g.pointables().frontmost().stabilizedTipPosition());
            Vector center = cumalitiveGesturePositions.divide(gestureCount);

            SeededCircleGesture scg = new SeededCircleGesture(g);

            scg.setCenter(center);

            Vector gradient = (center.minus(g.pointables().frontmost().stabilizedTipPosition()));
            scg.setRadius(gradient.magnitude());
            gradient = gradient.normalized();
            scg.setNormal(new Vector(gradient.getY(), -gradient.getX(), gradient.getZ()));
            scg.setProgress(gestureDuration / 1000f);
            scg.setPointable(g.pointables().frontmost());

            ((SeededGesture) g).setCircleGesture(scg);

        } else if (gestureType == Gesture.Type.TYPE_SWIPE){
            Pointable p = g.pointables().frontmost();
            Vector position = p.hand().palmPosition();
            Vector startPosition;
            if (ssg != null){
                startPosition = ssg.startPosition();
            } else {
                startPosition = position;
            }
            Vector direction = position.minus(startPosition);
            float speed = startPosition.distanceTo(position)/ (float)gestureDuration;

            ssg = new SeededSwipeGesture(g, startPosition, position, direction, speed, p);
            ((SeededGesture) g).setSwipeGesture(ssg);
        }
        return g;
    }

    @Override
    public void cleanUp() {

    }

    @Override
    public Csv getCsv() {
        Csv csv = new Csv();
        csv.add("correctStates", "" + (Math.round(100f * (success.size() / (float)(success.size() + failures.size())))/100f));
        return csv;
    }
}
