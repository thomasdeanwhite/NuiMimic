package com.sheffield.leapmotion.frame.generators.gestures;

import com.leapmotion.leap.*;
import com.leapmotion.leap.Gesture.State;
import com.leapmotion.leap.Gesture.Type;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.frame.playback.NGramLog;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class NoneGestureHandler implements GestureHandler {

	private static final Type[] TYPES = new Type[]{Type.TYPE_CIRCLE, Type.TYPE_INVALID, Type.TYPE_KEY_TAP, Type.TYPE_SCREEN_TAP, Type.TYPE_SWIPE};
	protected static final Random random = new Random();


	protected State gestureState;
	protected Type[] gestureTypes;
	protected long gestureStart;
	protected int gestureDuration;
	protected int gestureId = 0;
	protected ArrayList<Vector> cumalitiveGesturePositions = new ArrayList<Vector>();
	protected int gestureCount = 0;

	protected File outputFile;

	@Override
	public void setGestureOutputFile(File o) {
		outputFile = o;
	}
	
	@Override
	public GestureList handleFrame(Frame frame, Controller controller) {
		return new GestureList();
	}

	private long lastUpdate = 0;
	@Override
	public void tick(long time) {
		lastUpdate = time;
	}

	public long lastTick(){
		return lastUpdate;
	}

	public Frame clearFrame(Frame frame){

		// TODO Auto-generated method stub
		return frame;

	}
	
	public Type randomType(){
		int index = random.nextInt(TYPES.length);
		return TYPES[index];
	}
	
	public void advanceGestures(long time){
		if (gestureState == null || gestureTypes == null || gestureState == State.STATE_STOP){
			gestureState = State.STATE_START;
			//TODO: Implement GestureSelector here!
			gestureTypes = new Gesture.Type[]{Gesture.Type.valueOf(getNextGesture())};
			gestureStart = time-3;
			//default duration should be > 0 according to docs
			gestureDuration = 3;
			gestureId++;
			cumalitiveGesturePositions.clear();
			gestureCount = 0;
		} else {
			if (gestureState == State.STATE_UPDATE){
				long chance = random.nextInt(gestureDuration);

				if (chance > Properties.GESTURE_TIME_LIMIT){
					//new frame
					gestureState = State.STATE_STOP;
				}
			} else {
				gestureState = State.STATE_UPDATE;
			}
			//update times
			gestureDuration = (int) (System.currentTimeMillis() - gestureStart);
		}
	}

    public String getNextGesture(){
        return randomType().toString();
    }
	
	
	
}
