package com.sheffield.leapmotion.frame.generators;

import com.google.gson.Gson;
import com.leapmotion.leap.*;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.controller.mocks.SeededCircleGesture;
import com.sheffield.leapmotion.frame.analyzer.machinelearning.ngram.NGram;
import com.sheffield.leapmotion.frame.analyzer.machinelearning.ngram.NGramModel;
import com.sheffield.leapmotion.frame.generators.gestures.NGramGestureHandler;
import com.sheffield.leapmotion.util.FileHandler;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.frame.util.Quaternion;
import com.sheffield.leapmotion.controller.mocks.SeededHand;

import java.io.File;
import java.util.HashMap;

public class NGramFrameGenerator extends SequenceFrameGenerator {

    protected NGram jointNgram;
    protected NGram positionNgram;
    protected NGram rotationNgram;
    protected NGram stabilisedNgram;
    protected NGram gestureNGram;
    protected NGram circleNGram;

    protected String nextSequenceCircle = "";
    protected String nextSequenceGesture = "";
    private String currentSequenceStab = "";

    public void setGestureOutputFile(File file) {

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

            ngString = rawFile + "/gesture_circle_ngram";

            NGram gcNgram = gson.fromJson(FileHandler.readFile(new File
                    (ngString)), NGram.class);

            gcNgram.calculateProbabilities();

            setup(jointNgram, positionNgram, rotationNgram, gestureNgram,
                    stabNgram, gcNgram);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace(App.out);
            System.exit(0);
        }

    }

    public NGramFrameGenerator(NGram jointNgram, NGram positionNgram, NGram
            rotationNgram, NGram gestureNGram, NGram tipsNgram, NGram circleNGram,
                               HashMap<String, SeededHand> joints, HashMap<String, Vector> positions, HashMap<String, Quaternion> rotations,
                               HashMap<String, Vector[]> stabilisedTips, HashMap<String, SeededCircleGesture> seededCircles) {
        super(joints, positions, rotations, stabilisedTips, seededCircles);
        setup(jointNgram, positionNgram, rotationNgram, gestureNGram, tipsNgram, circleNGram);
    }

    public void merge(NGramFrameGenerator ngfs) {
        jointNgram.merge(ngfs.jointNgram);
        positionNgram.merge(ngfs.positionNgram);
        rotationNgram.merge(ngfs.rotationNgram);
        stabilisedNgram.merge(ngfs.stabilisedNgram);
        gestureNGram.merge(ngfs.gestureNGram);
        circleNGram.merge(circleNGram);

    }

    private void setup(NGram jointNgram, NGram positionNgram, NGram
            rotationNgram, NGram gestureNGram, NGram stabilisedNgram, NGram circleNGram) {
        App.out.println("* Setting up NGramModel Frame Selection");

        this.jointNgram = jointNgram;
        this.jointNgram.calculateProbabilities();

        this.positionNgram = positionNgram;
        this.positionNgram.calculateProbabilities();

        this.rotationNgram = rotationNgram;
        this.rotationNgram.calculateProbabilities();

        this.stabilisedNgram = stabilisedNgram;
        this.stabilisedNgram.calculateProbabilities();

        this.gestureNGram = gestureNGram;
        this.gestureNGram.calculateProbabilities();

        this.circleNGram = circleNGram;
        this.circleNGram.calculateProbabilities();
    }

    @Override
    public String getName() {
        return "NGram Frame Generation";
    }

    protected long lastUpdate = 0;

    @Override
    public void tick(long time) {
        super.tick(time);

        //ngGestureHandler.tick(time);

    }

    private String currentSequence = "";
    private String currentSequencePosition = "";
    private String currentSequenceRotation = "";

    public static String getLastLabel(String s) {
        if (s == null){
            return null;
        }
        int delimSubstring = s.lastIndexOf(NGramModel.DELIMITER) + 1;
        if (delimSubstring > 0) {
            s = s.substring(delimSubstring);
        }

        return s;
    }

    public String nextSequenceJoints() {
        currentSequence = jointNgram.babbleNext(currentSequence);

        if (currentSequence == null) {
            currentSequence = jointNgram.babbleNext("");
        }

        return getLastLabel(currentSequence);
    }

    public String nextSequencePosition() {
        currentSequencePosition = positionNgram.babbleNext(currentSequencePosition);

        if (currentSequencePosition == null) {
            currentSequencePosition = positionNgram.babbleNext("");
        }

        return getLastLabel(currentSequencePosition);
    }

    public String nextSequenceRotation() {
        currentSequenceRotation = rotationNgram.babbleNext(currentSequenceRotation);

        if (currentSequenceRotation == null) {
            currentSequenceRotation = rotationNgram.babbleNext("");
        }

        return getLastLabel(currentSequenceRotation);
    }

    @Override
    public String nextSequenceGesture() {
        nextSequenceGesture = gestureNGram.babbleNext(nextSequenceGesture);

        if (nextSequenceGesture == null || nextSequenceGesture.equals("null")){
            nextSequenceGesture = gestureNGram.babbleNext("");
        }

        if (nextSequenceGesture == null  || nextSequenceGesture.equals("null")){
            nextSequenceGesture = "TYPE_INVALID";
        }

        String gest = getLastLabel(nextSequenceGesture);
        if (!gest.contains("TYPE_CIRCLE")){
            nextSequenceCircle = "";
        }


        return gest;
    }



    @Override
    public String nextSequenceCircleGesture() {
        nextSequenceCircle = circleNGram.babbleNext(nextSequenceCircle);

        if (nextSequenceCircle == null){
            nextSequenceCircle = circleNGram.babbleNext("");
        }

        return getLastLabel(nextSequenceCircle);
    }

    @Override
    public String nextSequenceStabilisedTips() {
        currentSequenceStab = stabilisedNgram.babbleNext
                (currentSequenceStab);

        if (currentSequenceStab == null) {
            currentSequenceStab = stabilisedNgram.babbleNext
                    ("");
        }

        return getLastLabel(currentSequenceStab);
    }

    @Override
    public void cleanUp() {

    }

}
