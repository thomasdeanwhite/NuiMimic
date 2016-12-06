package com.sheffield.leapmotion.controller.gestures;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.Gesture.State;
import com.leapmotion.leap.Gesture.Type;
import com.leapmotion.leap.GestureList;
import com.leapmotion.leap.Vector;

import java.util.Random;

public class NoneGestureHandler implements GestureHandler {

	private static final Type[] TYPES = new Type[]{Type.TYPE_CIRCLE, Type.TYPE_INVALID, Type.TYPE_KEY_TAP, Type.TYPE_SCREEN_TAP, Type.TYPE_SWIPE};
	protected static final Random random = new Random();


	protected State gestureState;
	protected Type gestureType;
	protected long gestureStart;
	protected int gestureDuration;
	protected int gestureId = 0;
	protected Vector cumalitiveGesturePositions = Vector.zero();
	protected int gestureCount = 0;

	//33 ms gesture duration (30 fps)
	public static final int GESTURE_TIME_LIMIT = 33;
	
	@Override
	public GestureList handleFrame(Frame frame) {
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
		if (gestureState == null || gestureType == null || gestureState == State.STATE_STOP){
			gestureState = State.STATE_START;
			//TODO: Implement GestureSelector here!
			gestureType = Gesture.Type.valueOf(getNextGesture());
			gestureStart = System.currentTimeMillis()-3;
			//default duration should be > 0 according to docs
			gestureDuration = 3;
			gestureId++;
			cumalitiveGesturePositions = Vector.zero();
			gestureCount = 0;
		} else {
			if (gestureState == State.STATE_UPDATE){
				long chance = random.nextInt(gestureDuration);
				
				if (chance > GESTURE_TIME_LIMIT){
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
