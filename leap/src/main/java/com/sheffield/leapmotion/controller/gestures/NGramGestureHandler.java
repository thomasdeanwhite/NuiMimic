package com.sheffield.leapmotion.controller.gestures;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.GestureList;
import com.leapmotion.leap.Vector;
import com.sheffield.instrumenter.Properties;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.analyzer.AnalyzerApp;
import com.sheffield.leapmotion.mocks.SeededCircleGesture;
import com.sheffield.leapmotion.mocks.SeededGestureList;
import com.sheffield.leapmotion.mocks.SeededSwipeGesture;

import java.util.ArrayList;

public class NGramGestureHandler extends RandomGestureHandler {

	private SeededCircleGesture scg;
	private SeededSwipeGesture ssg;
	private AnalyzerApp analyzer;
	private ArrayList<Gesture.Type> gestureTypes;
	private String currentGesture;

	public NGramGestureHandler(String filename) {
		try {
			gestureTypes = new ArrayList<Gesture.Type>();
			String sequenceFile = Properties.DIRECTORY  + "/" + filename + ".gesture_type_ngram";
			analyzer = new AnalyzerApp(sequenceFile);
			analyzer.analyze();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace(App.out);
		}

	}

	public void advanceGestures(){
		if (gestureState == null || gestureTypes.size() == 0 || gestureState == Gesture.State.STATE_STOP){
			while (currentGesture == null || currentGesture.equals("NULL")){
				currentGesture = analyzer.getDataAnalyzer().next();
			}
			gestureState = Gesture.State.STATE_START;
			currentGesture = analyzer.getDataAnalyzer().next();
			gestureTypes.clear();
			cumalitiveGesturePositions = Vector.zero();
			gestureCount = 0;
			cumalitiveGesturePositions = Vector.zero();
			String[] gestures = currentGesture.split("\\+");
			gestureDuration = 3;
			gestureStart = System.currentTimeMillis()-gestureDuration;
			for (String s : gestures){
				if (s != null && !s.equalsIgnoreCase("null")){
					gestureTypes.add(Gesture.Type.valueOf(s));

					gestureId++;
				}
			}
			gestureCount = 0;
		} else {
			if (gestureState == Gesture.State.STATE_UPDATE) {
				long chance = random.nextInt(gestureDuration);


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

	@Override
	public GestureList handleFrame(Frame frame) {
		frame = clearFrame(frame);
		SeededGestureList gl = new SeededGestureList();

		advanceGestures();
		gestureCount++;
		if (gestureTypes.size() <= 0)
			return gl;
		int counter = gestureTypes.size();
		for (Gesture.Type gt : gestureTypes) {
			gl.addGesture(setupGesture(gt, frame, gestureId-(--counter)));
		}
		return gl;
	}
	
}
