package com.sheffield.leapmotion.frameselectors;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.GestureList;
import com.sheffield.instrumenter.Properties;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.controller.gestures.GestureHandler;
import com.sheffield.leapmotion.controller.gestures.NGramGestureHandler;
import com.sheffield.leapmotion.framemodifier.FrameModifier;
import com.sheffield.leapmotion.framemodifier.NGramFrameModifier;
import com.sheffield.leapmotion.mocks.SeededFrame;

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

	public StaticDistanceFrameSelector() {
		String[] gestures = Properties.GESTURE_FILES;

        if (gestures.length > 1){
            changeGestures = true;
            currentGesture = gestures[r.nextInt(gestures.length)];
        } else {
            currentGesture = gestures[0];
        }
        frameModifiers = new HashMap<String, FrameModifier>();
        frameSelectors = new HashMap<String, FrameSelector>();
        gestureHandlers = new HashMap<String, GestureHandler>();
		for (String s : gestures){
            try {
                App.out.println(s);
                frameModifiers.put(s, new NGramFrameModifier(s));
                frameSelectors.put(s, new NGramFrameSelector(s));
                gestureHandlers.put(s, new NGramGestureHandler(s));
            } catch (Exception e){
                e.printStackTrace(App.out);
            }
		}

	}

	@Override
	public Frame newFrame() {
        long time = System.currentTimeMillis();
        if (time - lastGestureChange > GESTURE_CHANGE_TIME){
            String[] gestures = Properties.GESTURE_FILES;
            currentGesture = gestures[r.nextInt(gestures.length)];
            lastGestureChange = System.currentTimeMillis();
        }

        return frameSelectors.get(currentGesture).newFrame();

	}

	public void modifyFrame(SeededFrame frame) {
		frameModifiers.get(currentGesture).modifyFrame(frame);
	}

    @Override
    public GestureList handleFrame(Frame frame) {
        return gestureHandlers.get(currentGesture).handleFrame(frame);
    }
}
