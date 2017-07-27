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
    private final static int GESTURE_CHANGE_TIME = 300000;

	private String currentGesture;

    private NGramFrameGenerator frameSelector;

    private GestureHandler gestureHandler;

    public File generateFile(String filename){
        return FileHandler.generateTestingOutputFile(filename);
    }

	public SingleModelFrameGenerator() {
		String gesture = Properties.INPUT[0];

        try {
            NGramFrameGenerator ngfs = new NGramFrameGenerator(gesture);

            frameSelector = ngfs;

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

    @Override
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
        return frameSelector.handleFrame(frame, controller);
    }

    @Override
    public void setGestureOutputFile(File f) {

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
