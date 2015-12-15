package com.sheffield.leapmotion.controller.gestures;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture.State;
import com.leapmotion.leap.Gesture.Type;
import com.leapmotion.leap.GestureList;

import java.util.Random;

public class NoneGestureHandler implements GestureHandler {

	private static final Type[] TYPES = new Type[]{Type.TYPE_CIRCLE};//, Type.TYPE_KEY_TAP, Type.TYPE_SCREEN_TAP, Type.TYPE_SWIPE};
	private static final Random random = new Random();
	
	protected State gestureState;
	protected Type gestureType;
	protected long gestureStart;
	protected int gestureDuration;
	protected int gestureId = 0;
	
	//gesture time limit (3 seconds)[in nanoseconds]
	private static final int GESTURE_TIME_LIMIT = 3 * 1000;
	
	@Override
	public GestureList handleFrame(Frame frame) {
		return new GestureList();
	}
	
	public Frame clearFrame(Frame frame){
//		// TODO Auto-generated method stub
//		GestureList gl = frame.gestures();
//		Iterator<Gesture> i = gl.iterator();
//		while (i.hasNext()){
//			i.next();
//			i.remove();
//		}
		return frame;
	}
	
	public Type randomType(){
		int index = random.nextInt(TYPES.length);
		return TYPES[index];
	}
	
	public void advanceGestures(){
		if (gestureState == null || gestureType == null || gestureState == State.STATE_STOP){
			gestureState = State.STATE_START;
			//TODO: Implement GestureSelector here!
			gestureType = randomType();
			gestureStart = System.currentTimeMillis()-3;
			//default duration should be > 0 according to docs
			gestureDuration = 3;
			gestureId++;
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
	
	
	
}
