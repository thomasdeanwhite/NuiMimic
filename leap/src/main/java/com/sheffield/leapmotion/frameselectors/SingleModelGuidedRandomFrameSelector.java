package com.sheffield.leapmotion.frameselectors;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.GestureList;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.FileHandler;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.controller.gestures.GestureHandler;
import com.sheffield.leapmotion.controller.gestures.NGramGestureHandler;
import com.sheffield.leapmotion.controller.gestures.RandomGestureHandler;
import com.sheffield.leapmotion.framemodifier.FrameModifier;
import com.sheffield.leapmotion.mocks.SeededFrame;

import java.io.File;
import java.util.HashMap;
import java.util.Random;

public class SingleModelGuidedRandomFrameSelector extends FrameSelector implements FrameModifier, GestureHandler {

	private Random r = new Random();

    private long lastGestureChange = 0;
    private final static int GESTURE_CHANGE_TIME = 10000;

	private HashMap<String, FrameModifier> frameModifiers;
	private String currentGesture;

    private HashMap<String, FrameSelector> frameSelectors;

    private HashMap<String, GestureHandler> gestureHandlers;

    private RandomTemplateFrameSelector randomFrameSelector;
    private RandomGestureHandler randomGestureHandler;

    private Random random = new Random();

    private boolean changeGestures = false;

    private long lastPositionChange = 0;
    private final int POSITION_LOCATE_TIME = 500;
    private final int POSITION_CHANGE_TIME = 4000;

    boolean useModel;

    public File generateFile(String filename){
        return FileHandler.generateTestingOutputFile(filename);
    }

	public SingleModelGuidedRandomFrameSelector() {
		String[] gestures = Properties.INPUT;

        randomFrameSelector = new RandomTemplateFrameSelector(gestures[0]);
        randomGestureHandler = new RandomGestureHandler();

        useModel = false;

        if (gestures.length > 1){
            changeGestures = true;
            currentGesture = gestures[r.nextInt(gestures.length)];
        } else {
            currentGesture = gestures[0];
        }
        frameModifiers = new HashMap<String, FrameModifier>();
        frameSelectors = new HashMap<String, FrameSelector>();
        gestureHandlers = new HashMap<String, GestureHandler>();

        int testIndex = Properties.CURRENT_RUN;

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
        if (useModel) {
            long time = System.currentTimeMillis();
            if (time - lastGestureChange > GESTURE_CHANGE_TIME) {
                String[] gestures = Properties.INPUT;
                currentGesture = gestures[r.nextInt(gestures.length)];
                lastGestureChange = System.currentTimeMillis();
                useModel = false;
            }

            return frameSelectors.get(currentGesture).newFrame();
        } else {
            long time = System.currentTimeMillis();
            long timePassed = (time - lastGestureChange);
            if (random.nextInt((int)timePassed) < GESTURE_CHANGE_TIME){
                lastGestureChange = time;
            }
            return randomFrameSelector.newFrame();
        }

	}

    @Override
    public String status() {
        return null;
    }

    public void modifyFrame(SeededFrame frame) {
        if (useModel) {
            frameModifiers.get(currentGesture).modifyFrame(frame);
        } else {
            randomFrameSelector.modifyFrame(frame);
        }
	}

    @Override
    public GestureList handleFrame(Frame frame) {
        if (useModel) {
            return gestureHandlers.get(currentGesture).handleFrame(frame);
        } else {
            return randomGestureHandler.handleFrame(frame);
        }
    }

    private long lastUpdate = 0;
    @Override
    public void tick(long time) {
        lastUpdate = time;
    }

    public long lastTick(){
        return lastUpdate;
    }

    @Override
    public void cleanUp() {

    }
}
