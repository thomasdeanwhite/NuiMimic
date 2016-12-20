package com.sheffield.leapmotion.frame.generators;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.GestureList;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.util.FileHandler;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.frame.generators.gestures.GestureHandler;
import com.sheffield.leapmotion.frame.generators.gestures.NGramGestureHandler;
import com.sheffield.leapmotion.controller.mocks.SeededFrame;
import com.sheffield.output.Csv;

import java.io.File;
import java.util.HashMap;
import java.util.Random;

public class SingleModelFrameGenerator extends FrameGenerator implements GestureHandler {
    @Override
    public Csv getCsv() {
        return new Csv();
    }
	private Random r = new Random();

    private long lastGestureChange = 0;
    private final static int GESTURE_CHANGE_TIME = 10000;

	private String currentGesture;

    private HashMap<String, FrameGenerator> frameSelectors;

    private HashMap<String, GestureHandler> gestureHandlers;

    private boolean changeGestures = false;

    private long lastPositionChange = 0;
    private final int POSITION_LOCATE_TIME = 500;
    private final int POSITION_CHANGE_TIME = 4000;

    public File generateFile(String filename){
        return FileHandler.generateTestingOutputFile(filename);
    }

	public SingleModelFrameGenerator() {
		String[] gestures = Properties.INPUT;

        if (gestures.length > 1){
            changeGestures = true;
            currentGesture = gestures[r.nextInt(gestures.length)];
        } else {
            currentGesture = gestures[0];
        }
        frameSelectors = new HashMap<String, FrameGenerator>();
        gestureHandlers = new HashMap<String, GestureHandler>();

        long testIndex = Properties.CURRENT_RUN;

		for (String s : gestures){
            try {
                File pFile = generateFile("hand_positions-" + testIndex);
                pFile.createNewFile();
                File rFile = generateFile("hand_rotations-" + testIndex);
                rFile.createNewFile();
                NGramFrameGenerator ngfs = new NGramFrameGenerator(s);
                ngfs.setOutputFiles(pFile, rFile);
                File jFile = generateFile("joint_positions-" + testIndex);
                jFile.createNewFile();
                ngfs.setOutputFile(jFile);
                frameSelectors.put(s, ngfs);
                File gFile = generateFile("gestures-" + testIndex);
                gFile.createNewFile();
                NGramGestureHandler nggh = new NGramGestureHandler(s);
                nggh.setOutputFile(gFile);
                gestureHandlers.put(s, nggh);
            } catch (Exception e){
                e.printStackTrace(App.out);
            }
		}

	}

	@Override
	public Frame newFrame() {
        return frameSelectors.get(currentGesture).newFrame();

	}

    @Override
    public String status() {
        return null;
    }

    public void modifyFrame(SeededFrame frame) {
		frameSelectors.get(currentGesture).modifyFrame(frame);
	}

    @Override
    public boolean allowProcessing() {
        return true;
    }

    @Override
    public GestureList handleFrame(Frame frame) {
        return gestureHandlers.get(currentGesture).handleFrame(frame);
    }

    private long lastUpdate = 0;
    @Override
    public void tick(long time) {
        lastUpdate = time;

        if (time - lastGestureChange > GESTURE_CHANGE_TIME){
            String[] gestures = Properties.INPUT;
            currentGesture = gestures[r.nextInt(gestures.length)];
            lastGestureChange = time;
        }

        if (gestureHandlers.get(currentGesture).lastTick() < time){
            gestureHandlers.get(currentGesture).tick(time);
        }
        if (frameSelectors.get(currentGesture).lastTick() < time){
            frameSelectors.get(currentGesture).tick(time);
        }
    }

    public long lastTick(){
        return lastUpdate;
    }

    @Override
    public void cleanUp() {

    }
}
