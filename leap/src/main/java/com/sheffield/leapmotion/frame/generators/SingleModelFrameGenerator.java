package com.sheffield.leapmotion.frame.generators;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.GestureList;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.util.FileHandler;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.frame.generators.gestures.GestureHandler;
import com.sheffield.leapmotion.controller.mocks.SeededFrame;
import com.sheffield.output.Csv;

import java.io.File;
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

    private FrameGenerator frameSelector;

    private GestureHandler gestureHandler;

    private boolean changeGestures = false;

    private long lastPositionChange = 0;
    private final int POSITION_LOCATE_TIME = 500;
    private final int POSITION_CHANGE_TIME = 4000;

    public File generateFile(String filename){
        return FileHandler.generateTestingOutputFile(filename);
    }

	public SingleModelFrameGenerator() {
		String gesture = Properties.INPUT[0];

        long testIndex = Properties.CURRENT_RUN;

        try {
            File pFile = generateFile("hand_positions-" + testIndex);
            pFile.createNewFile();
            File rFile = generateFile("hand_rotations-" + testIndex);
            rFile.createNewFile();
            NGramFrameGenerator ngfs = new NGramFrameGenerator(gesture);
            ngfs.setOutputFiles(pFile, rFile);
            File jFile = generateFile("joint_positions-" + testIndex);
            jFile.createNewFile();
            ngfs.setOutputJointsFile(jFile);
            frameSelector = ngfs;
            File gFile = generateFile("gestures-" + testIndex);
            gFile.createNewFile();

            ngfs.setGestureOutputFile(gFile);
            gestureHandler = ngfs;
        } catch (Exception e){
            e.printStackTrace(App.out);
        }

	}

	@Override
	public Frame newFrame() {
        return frameSelector.newFrame();

	}

    @Override
    public String status() {
        return null;
    }

    public void modifyFrame(SeededFrame frame) {
		frameSelector.modifyFrame(frame);
	}

    @Override
    public boolean allowProcessing() {
        return true;
    }

    @Override
    public String getName() {
        return "NGram Model";
    }

    @Override
    public GestureList handleFrame(Frame frame, Controller controller) {
        return gestureHandler.handleFrame(frame, controller);
    }

    @Override
    public void setGestureOutputFile(File f) {
        gestureHandler.setGestureOutputFile(f);
    }

    private long lastUpdate = 0;

    @Override
    public void tick(long time) {
        try {
            lastUpdate = time;


            if (time - lastGestureChange > GESTURE_CHANGE_TIME && Properties.INPUT.length > 1) {
                String[] gestures = Properties.INPUT;
                currentGesture = gestures[r.nextInt(gestures.length)];
                lastGestureChange = time;
            }

            if (frameSelector.lastTick() < time) {
                frameSelector.tick(time);
            }
        } catch (Throwable t){
            t.printStackTrace(App.out);
        }
    }

    public long lastTick(){
        return lastUpdate;
    }

    @Override
    public void cleanUp() {

    }
}
