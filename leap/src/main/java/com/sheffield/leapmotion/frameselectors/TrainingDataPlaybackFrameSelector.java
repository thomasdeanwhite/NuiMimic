package com.sheffield.leapmotion.frameselectors;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.FileHandler;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.controller.SeededController;
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
public class TrainingDataPlaybackFrameSelector extends FrameSelector implements FrameModifier {

    //long lastSwitchTime = 0;
    //int currentAnimationTime = 0;
    int currentLabel = 0;

    private HashMap<String, SeededHand> hands;
    private HashMap<String, Vector> positions;
    private HashMap<String, Vector[]> rotations;

    private ArrayList<String> handLabelStack;
    private ArrayList<String> positionabelStack;
    private ArrayList<String> rotationLabelStack;


    public TrainingDataPlaybackFrameSelector (String filename){
        try {
            App.out.println("* Setting up NGram Frame Selection");
            //lastSwitchTime = System.currentTimeMillis();
            //currentAnimationTime = Properties.SWITCH_TIME;
            handLabelStack = new ArrayList<String>();
            String clusterFile = Properties.DIRECTORY + "/" + filename + ".joint_position_data";
            hands = new HashMap<String, SeededHand>();

            currentLabel = 0;

            String contents = FileHandler.readFile(new File(clusterFile));
            String[] lines = contents.split("\n");
            for (String line : lines) {
                Frame f = SeededController.newFrame();
                SeededHand hand = HandFactory.createHand(line, f);

                hands.put(hand.getUniqueId(), hand);
                // order.add(hand.getUniqueId());

                HandFactory.injectHandIntoFrame(f, hand);

            }

            String sequenceFile = Properties.DIRECTORY + "/" + filename + ".joint_position_ngram";
            String sequenceInfo = FileHandler.readFile(new File(sequenceFile));
            String[] seqData = sequenceInfo.split(",");

            for (String s : seqData){
                handLabelStack.add(s);
            }

        } catch (IOException e){
            e.printStackTrace(App.out);
        }
    }
    @Override
    public void modifyFrame(SeededFrame frame) {

    }

    @Override
    public Frame newFrame() {
        Frame f = SeededController.newFrame();
        f = HandFactory.injectHandIntoFrame(f, hands.get(handLabelStack.remove(0)));
        return f;
    }
}
