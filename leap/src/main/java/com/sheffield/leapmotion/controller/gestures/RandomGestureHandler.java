package com.sheffield.leapmotion.controller.gestures;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.GestureList;
import com.leapmotion.leap.Pointable;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.mocks.SeededCircleGesture;
import com.sheffield.leapmotion.mocks.SeededGesture;
import com.sheffield.leapmotion.mocks.SeededGestureList;
import com.sheffield.leapmotion.mocks.SeededSwipeGesture;

public class RandomGestureHandler extends NoneGestureHandler {

	private SeededCircleGesture scg;
	private SeededSwipeGesture ssg;


	@Override
	public GestureList handleFrame(Frame frame) {
		frame = clearFrame(frame);
		
		SeededGestureList gl = new SeededGestureList();

		advanceGestures();

		if (gestureType == Gesture.Type.TYPE_INVALID)
			return gl;

		gestureCount++;
		
		gl.addGesture(setupGesture(gestureType, frame, gestureId));
		return gl;
	}

	public Gesture setupGesture(Gesture.Type gestureType, Frame frame, int gestureId){
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

		} else if (gestureType == Gesture.Type.TYPE_SWIPE){
			Pointable p = g.pointables().frontmost();
			Vector position = p.hand().palmPosition();
			Vector startPosition;
			if (ssg != null){
				startPosition = ssg.startPosition();
			} else {
				startPosition = position;
			}
			Vector direction = position.minus(startPosition);
			float speed = startPosition.distanceTo(position)/ (float)gestureDuration;

			ssg = new SeededSwipeGesture(g, startPosition, position, direction, speed, p);
			((SeededGesture) g).setSwipeGesture(ssg);
		}
		return g;
	}

	public void advanceGestures(){
		super.advanceGestures();
		if (gestureState == null || gestureType == null || gestureState == Gesture.State.STATE_STOP){
			gestureState = Gesture.State.STATE_START;
			//currentGesture = analyzer.getDataAnalyzer().next();
			gestureType = randomType();

			cumalitiveGesturePositions = Vector.zero();
			gestureCount = 0;
			cumalitiveGesturePositions = Vector.zero();
			gestureDuration = 3;
			gestureStart = System.currentTimeMillis()-gestureDuration;
			gestureCount = 0;
		} else {
			long chance = random.nextInt(gestureDuration);

			if (gestureType == Gesture.Type.TYPE_INVALID){
				if (chance > GESTURE_TIME_LIMIT) {
					gestureState = Gesture.State.STATE_STOP;
				}
			}
			if (gestureState == Gesture.State.STATE_UPDATE) {

				if (chance > GESTURE_TIME_LIMIT) {
					gestureState = Gesture.State.STATE_STOP;
				}

			} else {
				gestureState = Gesture.State.STATE_UPDATE;
			}
			//update times
			gestureDuration = (int) (System.currentTimeMillis() - gestureStart);
		}
	}


}
