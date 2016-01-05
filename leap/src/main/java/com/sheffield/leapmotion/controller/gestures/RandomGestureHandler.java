package com.sheffield.leapmotion.controller.gestures;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.GestureList;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.mocks.SeededCircleGesture;
import com.sheffield.leapmotion.mocks.SeededGesture;
import com.sheffield.leapmotion.mocks.SeededGestureList;

public class RandomGestureHandler extends NoneGestureHandler {

	private SeededCircleGesture scg;

	@Override
	public GestureList handleFrame(Frame frame) {
        App.out.println("ENTERED");
		frame = clearFrame(frame);
		
		SeededGestureList gl = new SeededGestureList();

		advanceGestures();
		
		Gesture g = new SeededGesture(gestureType, gestureState, frame, gestureDuration, gestureId);

		gestures.add(g);

		if (gestureType == Gesture.Type.TYPE_CIRCLE){
			if (gestureState == Gesture.State.STATE_START){
				scg = new SeededCircleGesture(g);
			} else {
				scg.setGesture(g);
			}

			Vector center = new Vector();

			for (Gesture ge : gestures){
				center.plus(ge.pointables().frontmost().stabilizedTipPosition());
			}

			center.divide(gestures.size());

			SeededCircleGesture scg = new SeededCircleGesture(g);

			scg.setCenter(center);

			Vector gradient = (center.minus(g.pointables().frontmost().stabilizedTipPosition()));
			scg.setRadius(gradient.magnitude());
			gradient = gradient.normalized();
			scg.setNormal(new Vector(gradient.getY(), -gradient.getX(), gradient.getZ()));
			scg.setProgress(gestureDuration / (float) GESTURE_TIME_LIMIT);
			scg.setPointable(g.pointables().frontmost());

			((SeededGesture) g).setCircleGesture(scg);

		}
        App.out.println("EXIT");
		
		gl.addGesture(g);
		return gl;
	}
	
}
