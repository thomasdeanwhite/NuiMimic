package com.sheffield.leapmotion.frameselectors;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.GestureList;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.FileHandler;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.controller.gestures.GestureHandler;
import com.sheffield.leapmotion.controller.gestures.NGramGestureHandler;
import com.sheffield.leapmotion.framemodifier.FrameModifier;
import com.sheffield.leapmotion.mocks.SeededFrame;

import java.io.File;
import java.util.HashMap;
import java.util.Random;

public class StaticDistanceFrameSelector extends FrameSelector implements FrameModifier, GestureHandler {

	private Random r = new Random();

    private long lastGestureChange = 0;
    private final static int GESTURE_CHANGE_TIME = 10000;

	private HashMap<String, FrameModifier> frameModifiers;
	private String currentGesture;

    private HashMap<String, FrameSelector> frameSelectors;

    private HashMap<String, GestureHandler> gestureHandlers;

    private boolean changeGestures = false;

    private long lastPositionChange = 0;
    private final int POSITION_LOCATE_TIME = 500;
    private final int POSITION_CHANGE_TIME = 4000;

    public File generateFile(String filename){
        return FileHandler.generateTestingOutputFile(filename);
    }

	public StaticDistanceFrameSelector() {
		String[] gestures = Properties.INPUT;

        if (gestures.length > 1){
            changeGestures = true;
            currentGesture = gestures[r.nextInt(gestures.length)];
        } else {
            currentGesture = gestures[0];
        }
        frameModifiers = new HashMap<String, FrameModifier>();
        frameSelectors = new HashMap<String, FrameSelector>();
        gestureHandlers = new HashMap<String, GestureHandler>();

        long testIndex = Properties.CURRENT_RUN;

		for (String s : gestures){
            try {
                App.out.println(s);
                File pFile = generateFile("hand_positions-" + testIndex);
                pFile.createNewFile();
                File rFile = generateFile("hand_rotations-" + testIndex);
                rFile.createNewFile();
                NGramFrameSelector ngfs = new NGramFrameSelector(s);
                ngfs.setOutputFiles(pFile, rFile);
                frameModifiers.put(s, ngfs);
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
		frameModifiers.get(currentGesture).modifyFrame(frame);
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

        if (frameModifiers.get(currentGesture).lastTick() < time) {
            frameModifiers.get(currentGesture).tick(time);
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
