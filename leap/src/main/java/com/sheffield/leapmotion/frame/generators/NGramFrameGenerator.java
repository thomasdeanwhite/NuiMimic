package com.sheffield.leapmotion.frame.generators;

import com.google.gson.Gson;
import com.leapmotion.leap.*;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.frame.analyzer.machinelearning.ngram.NGram;
import com.sheffield.leapmotion.frame.analyzer.machinelearning.ngram.NGramModel;
import com.sheffield.leapmotion.frame.generators.gestures.GestureHandler;
import com.sheffield.leapmotion.frame.generators.gestures.NGramGestureHandler;
import com.sheffield.leapmotion.util.FileHandler;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.frame.util.Quaternion;
import com.sheffield.leapmotion.controller.mocks.SeededHand;

import java.io.File;
import java.util.HashMap;

public class NGramFrameGenerator extends SequenceFrameGenerator implements GestureHandler {

    protected NGram jointNgram;
    protected NGram positionNgram;
    protected NGram rotationNgram;
    protected NGram stabilisedNgram;

    protected NGramGestureHandler ngGestureHandler;
    private String currentSequenceStab = "";

    public void setGestureOutputFile(File file) {
        ngGestureHandler.setGestureOutputFile(file);
    }

    public NGramFrameGenerator(String filename) {
        super(filename);
        try {
            String rawFile = Properties.DIRECTORY + "/" + filename + "/processed";

            String sequenceFile = rawFile;

            if (!Properties.SINGLE_DATA_POOL) {
                sequenceFile += "/joint_position_ngram";
            } else {
                sequenceFile += "/hand_joints_ngram";
            }

            String ngString = FileHandler.readFile(new File(sequenceFile));

            Gson gson = new Gson();
            NGram jointNgram = gson.fromJson(ngString, NGram.class);

            jointNgram.calculateProbabilities();

            ngString = rawFile + "/hand_position_ngram";

            NGram positionNgram = gson.fromJson(FileHandler.readFile(new File(ngString)), NGram.class);

            positionNgram.calculateProbabilities();

            ngString = rawFile + "/hand_rotation_ngram";

            NGram rotationNgram = gson.fromJson(FileHandler.readFile(new File(ngString)), NGram.class);

            rotationNgram.calculateProbabilities();


            ngString = rawFile + "/gesture_type_ngram";

            NGram gestureNgram = gson.fromJson(FileHandler.readFile(new File(ngString)), NGram.class);

            gestureNgram.calculateProbabilities();


            ngString = rawFile + "/stabilised_tip_ngram";

            NGram stabNgram = gson.fromJson(FileHandler.readFile(new File
                    (ngString)), NGram.class);

            stabNgram.calculateProbabilities();

            setup(jointNgram, positionNgram, rotationNgram, gestureNgram,
                    stabNgram);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace(App.out);
            System.exit(0);
        }

    }

    public NGramFrameGenerator(NGram jointNgram, NGram positionNgram, NGram
            rotationNgram, NGram gestureNGram, NGram tipsNgram,
                               HashMap<String, SeededHand> joints, HashMap<String, Vector> positions, HashMap<String, Quaternion> rotations,
                               HashMap<String, Vector[]> stabilisedTips) {
        super(joints, positions, rotations, stabilisedTips);
        setup(jointNgram, positionNgram, rotationNgram, gestureNGram, tipsNgram);
    }

    public void merge(NGramFrameGenerator ngfs) {
        jointNgram.merge(ngfs.jointNgram);
        positionNgram.merge(ngfs.positionNgram);
        rotationNgram.merge(ngfs.rotationNgram);
        ngGestureHandler.merge(ngfs.ngGestureHandler);

    }

    private void setup(NGram jointNgram, NGram positionNgram, NGram
            rotationNgram, NGram gestureNGram, NGram stabilisedNgram) {
        App.out.println("* Setting up NGramModel Frame Selection");

        this.jointNgram = jointNgram;
        this.jointNgram.calculateProbabilities();

        this.positionNgram = positionNgram;
        this.positionNgram.calculateProbabilities();

        this.rotationNgram = rotationNgram;
        this.rotationNgram.calculateProbabilities();

        this.stabilisedNgram = stabilisedNgram;
        this.stabilisedNgram.calculateProbabilities();

        this.ngGestureHandler = new NGramGestureHandler(gestureNGram);
    }

    @Override
    public String getName() {
        return "NGram Frame Generation";
    }

    protected long lastUpdate = 0;

    @Override
    public void tick(long time) {
        super.tick(time);

        ngGestureHandler.tick(time);

    }

    private String currentSequence = "";
    private String currentSequencePosition = "";
    private String currentSequenceRotation = "";

    public static String getLastLabel(String s) {
        int delimSubstring = s.lastIndexOf(NGramModel.DELIMITER) + 1;
        if (delimSubstring > 0) {
            s = s.substring(delimSubstring);
        }

        return s;
    }

    public String nextSequenceJoints() {
        currentSequence = jointNgram.babbleNext(currentSequence);

        if (currentSequence == null) {
            throw new DataSparsityException("Data is too sparse for input");
        }

        return getLastLabel(currentSequence);
    }

    public String nextSequencePosition() {
        currentSequencePosition = jointNgram.babbleNext(currentSequencePosition);

        if (currentSequencePosition == null) {
            throw new DataSparsityException("Data is too sparse for input");
        }

        return getLastLabel(currentSequencePosition);
    }

    public String nextSequenceRotation() {
        currentSequenceRotation = jointNgram.babbleNext(currentSequenceRotation);

        if (currentSequenceRotation == null) {
            throw new DataSparsityException("Data is too sparse for input");
        }

        return getLastLabel(currentSequenceRotation);
    }

    @Override
    public String nextSequenceGesture() {
        return "TYPE_INVALID";
    }

    @Override
    public String nextSequenceStabilisedTips() {
        currentSequenceStab = stabilisedNgram.babbleNext
                (currentSequenceStab);

        if (currentSequenceStab == null) {
            throw new DataSparsityException("Data is too sparse for input");
        }

        return getLastLabel(currentSequenceStab);
    }

    @Override
    public void cleanUp() {

    }

    @Override
    public GestureList handleFrame(Frame frame, Controller controller) {
        return ngGestureHandler.handleFrame(frame, controller);
    }

}
