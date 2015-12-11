package com.sheffield.leapmotion.tester.controller.gestures;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.GestureList;
import com.sheffield.leapmotion.mocks.SeededGesture;
import com.sheffield.leapmotion.mocks.SeededGestureList;

public class RandomGestureHandler extends NoneGestureHandler {
	
	@Override
	public GestureList handleFrame(Frame frame) {
		frame = clearFrame(frame);
		
		SeededGestureList gl = new SeededGestureList();

		advanceGestures();
		
		Gesture g = new SeededGesture(gestureType, gestureState, frame, gestureDuration, gestureId);
		
		gl.addGesture(g);
		return gl;
	}
	
}
