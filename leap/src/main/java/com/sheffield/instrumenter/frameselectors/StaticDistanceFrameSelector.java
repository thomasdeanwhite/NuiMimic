package com.sheffield.instrumenter.frameselectors;

import com.leapmotion.leap.Frame;
import com.sheffield.instrumenter.App;
import com.sheffield.instrumenter.Properties;
import com.sheffield.instrumenter.framemodifier.FrameModifier;
import com.sheffield.instrumenter.framemodifier.NGramFrameModifier;
import com.sheffield.leapmotion.mocks.SeededFrame;

import java.util.HashMap;
import java.util.Random;

public class StaticDistanceFrameSelector extends FrameSelector implements FrameModifier {

	private Random r = new Random();

    private long lastGestureChange = 0;
    private final static int GESTURE_CHANGE_TIME = 10000;

	private HashMap<String, FrameModifier> frameModifiers;
	private String currentGesture;

	private HashMap<String, FrameSelector> frameSelectors;

    private long lastPositionChange = 0;
    private final int POSITION_LOCATE_TIME = 500;
    private final int POSITION_CHANGE_TIME = 4000;

	public StaticDistanceFrameSelector() {
		String[] gestures = Properties.GESTURE_FILES;
        frameModifiers = new HashMap<String, FrameModifier>();
        frameSelectors = new HashMap<String, FrameSelector>();
		for (String s : gestures){
            try {
                frameModifiers.put(s, new NGramFrameModifier(s));
                frameSelectors.put(s, new NGramFrameSelector(s));
            } catch (Exception e){
                e.printStackTrace(App.out);
            }
		}
		currentGesture = gestures[r.nextInt(gestures.length)];
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
        long time = System.currentTimeMillis();
	}
}
