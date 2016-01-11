package com.sheffield.leapmotion.controller.gestures;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.GestureList;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.mocks.SeededCircleGesture;
import com.sheffield.leapmotion.mocks.SeededGesture;
import com.sheffield.leapmotion.mocks.SeededGestureList;

public class RandomGestureHandler extends NoneGestureHandler {

	private SeededCircleGesture scg;

	@Override
	public GestureList handleFrame(Frame frame) {
		frame = clearFrame(frame);
		
		SeededGestureList gl = new SeededGestureList();

		advanceGestures();
		gestureCount++;
		
		Gesture g = new SeededGesture(gestureType, gestureState, frame, gestureDuration, gestureId);

		if (gestureType == Gesture.Type.TYPE_CIRCLE){
			scg = new SeededCircleGesture(g);
			cumalitiveGesturePositions = cumalitiveGesturePositions.plus(g.pointables().frontmost().stabilizedTipPosition());
			Vector center = cumalitiveGesturePositions.divide(gestureCount);

			SeededCircleGesture scg = new SeededCircleGesture(g);

			scg.setCenter(center);

			Vector gradient = (center.minus(g.pointables().frontmost().stabilizedTipPosition()));
			scg.setRadius(gradient.magnitude());
			gradient = gradient.normalized();
			scg.setNormal(new Vector(gradient.getY(), -gradient.getX(), gradient.getZ()));
			scg.setProgress(gestureDuration / 1000f);
			scg.setPointable(g.pointables().frontmost());

			((SeededGesture) g).setCircleGesture(scg);

		}
		
		gl.addGesture(g);
		return gl;
	}
	
}
