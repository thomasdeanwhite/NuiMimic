package com.sheffield.leapmotion.frame.generators;

import com.leapmotion.leap.*;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.util.FileHandler;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.frame.generators.gestures
        .ReconstructiveGestureHandler;
import com.sheffield.leapmotion.controller.mocks.SeededFrame;
import com.scythe.output.Csv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by thoma on 11/05/2016.
 */
public class ReconstructiveFrameGenerator extends SequenceFrameGenerator implements Reconstruction {

    private ArrayList<String> stabLabelStack;

    @Override
    public Csv getCsv() {
        return new Csv();
    }

    int currentAnimationTime = 0;
    int currentLabel = 0;

    private ArrayList<String> handLabelStack;
    private ArrayList<String> positionLabelStack;
    private ArrayList<String> rotationLabelStack;

    private ArrayList<Long> timings;

    private long lastSwitchTime = 0;

    private ReconstructiveGestureHandler tpgh;


    public ReconstructiveFrameGenerator(String filename) {
        super(filename);

        App.out.println("* Setting up Reconstruction");

        try {
            tpgh = new ReconstructiveGestureHandler(filename);
            lastSwitchTime = 0;
            currentAnimationTime = 0;
            handLabelStack = new ArrayList<String>();
            positionLabelStack = new ArrayList<String>();
            rotationLabelStack = new ArrayList<String>();
            stabLabelStack = new ArrayList<String>();



            currentLabel = 0;

            String sequenceFile =
                    Properties.DIRECTORY + "/" + filename + "/processed/" + Properties.CLUSTERS + "-" + Properties.N + "/"  +
                            (Properties.SINGLE_DATA_POOL ?
                                    "hand_joints.raw_sequence" :
                                    "joint_position.raw_sequence");
            String sequenceInfo = FileHandler.readFile(new File(sequenceFile));
            String[] seqData = sequenceInfo.split("\n")[0].split(",");

            for (String s : seqData) {
                if (s.length() > 0)
                    handLabelStack.add(s);
            }

            timings = new ArrayList<Long>();
            //final ArrayList<Long> timings = new ArrayList<Long>();

            String[] tim = sequenceInfo.split("\n")[1].split(",");


            for (String s : tim) {
                if (s.length() > 0)
                    // x / 1000 microsec to millisec
                    timings.add(Long.parseLong(s.split("@")[0]));
            }

            final ArrayList<Integer> indices = new ArrayList<Integer>();

            final ArrayList<Long> tims = timings;

            long first = tims.get(0);

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


            sequenceFile = Properties.DIRECTORY + "/" + filename +
                    "/processed/" + Properties.CLUSTERS + "-" + Properties.N + "/hand_position.raw_sequence";
            sequenceInfo = FileHandler.readFile(new File(sequenceFile));
            seqData = sequenceInfo.split("\n")[0].split(",");

            for (String s : seqData) {
                if (s.length() > 0)
                    positionLabelStack.add(s);
            }

            sequenceFile = Properties.DIRECTORY + "/" + filename +
                    "/processed/" + Properties.CLUSTERS + "-" + Properties.N + "/hand_rotation.raw_sequence";
            sequenceInfo = FileHandler.readFile(new File(sequenceFile));
            seqData = sequenceInfo.split("\n")[0].split(",");

            for (String s : seqData) {
                if (s.length() > 0)
                    rotationLabelStack.add(s);
            }



            sequenceFile = Properties.DIRECTORY + "/" + filename +
                    "/processed/" + Properties.CLUSTERS + "-" + Properties.N + "/stabilised_tip.raw_sequence";
            sequenceInfo = FileHandler.readFile(new File(sequenceFile));
            seqData = sequenceInfo.split("\n")[0].split(",");

            for (String s : seqData) {
                if (s.length() > 0)
                    stabLabelStack.add(s);
            }

        } catch (IOException e) {
            e.printStackTrace(App.out);
        }
    }

    @Override
    public boolean allowProcessing() {
        return true;
    }


    @Override
    public Frame newFrame() {
        Frame frame = null;
        if (handLabelStack.size() > 0 && currentHandIndex < handLabelStack.size()) {
            frame = super.newFrame();

            if (frame != null) {
                ((SeededFrame) frame).setTimestamp(timings.get(currentHandIndex) * 1000);
                ((SeededFrame) frame).setId(timings.get(currentHandIndex) * 1000);
            }
        }

        currentHandIndex++;

        if (frame == null){
            frame = SeededController.newFrame();
        }

        return frame;
    }

    @Override
    public String status() {
        return (handLabelStack.size() - currentHandIndex) + " hands";
    }

    @Override
    public GestureList handleFrame(Frame frame, Controller controller) {
        return tpgh.handleFrame(frame, controller);
    }

    @Override
    public void setGestureOutputFile(File f) {
        tpgh.setGestureOutputFile(f);
    }

    public int size() {
        return handLabelStack.size();
    }


    int currentHandIndex = 0;

    @Override
    public void tick(long time) {
        if (handLabelStack.size() == 0 || currentHandIndex >= timings.size()) {
            return;
        }

        super.tick(time);

        if (lastSwitchTime == 0) {
            lastSwitchTime = time;
        }

        tpgh.changeGesture(currentHandIndex);

        tpgh.tick(time);
    }

    @Override
    public String nextSequenceJoints() {
        return handLabelStack.get(currentHandIndex);
    }

    @Override
    public String nextSequencePosition() {
        return positionLabelStack.get
                (currentHandIndex);
    }

    @Override
    public String nextSequenceRotation() {
        return rotationLabelStack.get
                (currentHandIndex);
    }

    @Override
    public String nextSequenceGesture() {
        return null;
    }

    @Override
    public String nextSequenceCircleGesture() {
        return "0";
    }

    @Override
    public String nextSequenceStabilisedTips() {
        return stabLabelStack.get(currentHandIndex);
    }

    @Override
    public boolean hasNextFrame() {
        return currentHandIndex < handLabelStack.size();
    }

    @Override
    public String getName() {
        return "Clustered Reconstruction";
    }

    @Override
    public void cleanUp() {

    }

    public int getClusters() {
        return joints.size();
    }

    @Override
    public void setFrame(int index) {
        currentHandIndex = index;
    }
}
