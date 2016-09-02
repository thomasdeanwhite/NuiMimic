package com.sheffield.leapmotion.frameselectors;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.framemodifier.FrameModifier;
import com.sheffield.leapmotion.mocks.SeededFrame;
import com.sheffield.leapmotion.mocks.SeededHand;

import java.util.HashMap;
import java.util.Random;

public class RandomDistanceFrameSelector extends FrameSelector implements FrameModifier {

	private Random r = new Random();

    private long lastGestureChange = 0;
    private final static int GESTURE_CHANGE_TIME = 10000;

	private HashMap<String, FrameModifier> frameModifiers;
	private String currentGesture;

	private HashMap<String, FrameSelector> frameSelectors;

    private long lastPositionChange = 0;
    private final int POSITION_LOCATE_TIME = 500;
    private final int POSITION_CHANGE_TIME = 4000;

    private float xLimit = 200;
    private float yLimit = 200;
    private float zLimit = 200;

    private float lastX = 0;
    private float lastY = 0;
    private float lastZ = 0;

    private float nextX = 0;
    private float nextY = 0;
    private float nextZ = 0;

	public RandomDistanceFrameSelector() {
		String[] gestures = Properties.INPUT;
        frameModifiers = new HashMap<String, FrameModifier>();
        frameSelectors = new HashMap<String, FrameSelector>();
		for (String s : gestures){
            try {
                NGramFrameSelector ngfs = new NGramFrameSelector(s);
                frameModifiers.put(s, ngfs);
                frameSelectors.put(s, ngfs);
            } catch (Exception e){
                e.printStackTrace(App.out);
            }
		}
		currentGesture = gestures[r.nextInt(gestures.length)];
	}

	@Override
	public Frame newFrame() {
        long time = System.currentTimeMillis();
        if (time - lastPositionChange > POSITION_CHANGE_TIME){
            lastX = nextX;
            lastY = nextY;
            lastZ = nextZ;
            nextX = r.nextInt((int) (xLimit*2))-xLimit;
            nextY = r.nextInt((int) (yLimit*2))-yLimit;
            nextZ = r.nextInt((int) (zLimit*2))-zLimit;
            lastPositionChange = time;
        }
        if (time - lastGestureChange > GESTURE_CHANGE_TIME){
            String[] gestures = Properties.INPUT;
            currentGesture = gestures[r.nextInt(gestures.length)];
            lastGestureChange = System.currentTimeMillis();
        }

        return frameSelectors.get(currentGesture).newFrame();

	}

    @Override
    public String status() {
        return null;
    }

    public void modifyFrame(SeededFrame frame) {
		frameModifiers.get(currentGesture).modifyFrame(frame);
        long time = System.currentTimeMillis();
        float modifier = Math.min(1, (time - lastPositionChange) / (float)POSITION_LOCATE_TIME);
        Hand h = frame.hands().iterator().next();
        if (h instanceof SeededHand){
            SeededHand sh = (SeededHand) h;
            Vector lastPosition = new Vector(lastX, lastY, lastZ);
            Vector newPosition = new Vector(nextX, nextY, nextZ);
            sh.setOrigin(sh.basis().getOrigin().plus(lastPosition.plus(newPosition.minus(lastPosition).times(modifier))));
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
