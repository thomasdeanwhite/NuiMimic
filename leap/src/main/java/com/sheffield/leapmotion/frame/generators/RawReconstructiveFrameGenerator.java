package com.sheffield.leapmotion.frame.generators;

import com.leapmotion.leap.*;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.controller.mocks.SeededFrame;
import com.sheffield.leapmotion.frame.generators.gestures.GestureHandler;
import com.sheffield.leapmotion.frame.generators.gestures
        .ReconstructiveGestureHandler;
import com.sheffield.leapmotion.util.FileHandler;
import com.sheffield.leapmotion.util.ProgressBar;
import com.scythe.output.Csv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by thoma on 11/05/2016.
 */
public class RawReconstructiveFrameGenerator extends SequenceFrameGenerator
        implements GestureHandler, Reconstruction {

    @Override
    public Csv getCsv() {
        return new Csv();
    }

    private ArrayList<String> handLabelStack;
    private ArrayList<Long> timings;

    private long lastSwitchTime = 0;


    private ReconstructiveGestureHandler tpgh;

    private long startSeededTime = 0;
    private long seededTime = 0;

    private long lastUpdate = 0;

    private int currentHandIndex = 0;


    public RawReconstructiveFrameGenerator(String filename) throws IOException {
        super(getRawJoints(filename), getRawPositions(filename),
                getRawRotations(filename), getRawStabilisedTips(filename),
                new HashMap<>());
        tpgh = new ReconstructiveGestureHandler(filename);
        App.out.println("* Setting up Raw Reconstruction");
        handLabelStack = new ArrayList<String>();

        handLabelStack.add(0, null);



        String sequenceFile =
                Properties.DIRECTORY + "/" + filename + "/processed/" + Properties.CLUSTERS + "-" + Properties.N + "/"  +
                        (Properties.SINGLE_DATA_POOL ?
                                "hand_joints.raw_sequence" :
                                "joint_position.raw_sequence");
        String sequenceInfo = FileHandler.readFile(new File(sequenceFile));

        timings = new ArrayList<Long>();

        String[] tim = sequenceInfo.split("\n")[1].split(",");

        int counter = 0;

        for (String s : tim) {
            float progress = counter++ / (float) (tim.length - 1);
            if (Properties.SHOW_PROGRESS || (int) (progress * 100000f) % 25000 == 0) {
                App.out.print("\r" + ProgressBar.getProgressBar(21, progress) + "[2 of 4]");
            }
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

    }

    @Override
    public boolean allowProcessing() {
        return true;
    }

    @Override
    public Frame newFrame() {

        if (currentHandIndex >= timings.size() || handLabelStack.size() == 0) {

            return null;
        }

        SeededFrame f = (SeededFrame)super.newFrame();

        if (f != null){
            long seedTime = timings.get(currentHandIndex) * 1000;
            f.setTimestamp(seedTime);
            f.setId(seedTime);
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

    @Override
    public void setGestureOutputFile(File f) {
        tpgh.setGestureOutputFile(f);
    }

    public int size() {
        return handLabelStack.size();
    }

    @Override
    public void tick(long time) {
        lastUpdate = time;
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


        if (currentHandIndex + Properties.GESTURE_CIRCLE_FRAMES >=
                timings.size() || currentHandIndex >= timings.size()) {

            return;
        }

        super.tick(time);


        lastSwitchTime = seededTime - Properties.SWITCH_TIME;

        tpgh.changeGesture(
                currentHandIndex + Properties.GESTURE_CIRCLE_FRAMES);


        tpgh.tick(time);
    }

    @Override
    public String nextSequenceJoints() {
        return handLabelStack.get(currentHandIndex);
    }

    @Override
    public String nextSequencePosition() {
        return handLabelStack.get
                (currentHandIndex);
    }

    @Override
    public String nextSequenceRotation() {
        return handLabelStack.get
                (currentHandIndex);
    }

    @Override
    public String nextSequenceGesture() {
        return null;
    }

    @Override
    public String nextSequenceCircleGesture() {
        return null;
    }

    @Override
    public String nextSequenceStabilisedTips() {
        return handLabelStack.get(currentHandIndex);
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
        return joints.size();
    }

    @Override
    public void setFrame(int index) {
        currentHandIndex = index;
    }
}
