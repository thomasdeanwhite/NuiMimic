package com.sheffield.leapmotion.controller.gestures;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.GestureList;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.FileHandler;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.analyzer.AnalyzerApp;
import com.sheffield.leapmotion.mocks.SeededGestureList;

import java.io.File;
import java.util.ArrayList;

public class ReconstructiveGestureHandler extends RandomGestureHandler {

	private AnalyzerApp analyzer;
	private ArrayList<Gesture.Type> gestureTypes;
	private String currentGesture;

	private ArrayList<String> gestureLabels;

	private boolean changed = true;

	public ReconstructiveGestureHandler(String filename) {
		//super(filename);
		try {
			gestureTypes = new ArrayList<Gesture.Type>();
			String sequenceFile = Properties.DIRECTORY  + "/" + filename + ".raw_sequence.gesture_type_data";
			gestureLabels = new ArrayList<String>();

			String gestureData = FileHandler.readFile(new File(sequenceFile));

			String[] gestureInfo = gestureData.split(" ");

			for (String s : gestureInfo){
				gestureLabels.add(s);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace(App.out);
		}

	}

	public void advanceGestures(){
		if (currentGesture == null || currentGesture == "TYPE_INVALID"){
			return;
		}
		if (gestureState == null || gestureTypes.size() == 0 || gestureState == Gesture.State.STATE_STOP){
			gestureState = Gesture.State.STATE_START;

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
				if (changed){
					changed = false;
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
			if (!gt.equals(Gesture.Type.TYPE_INVALID)) {
				gl.addGesture(setupGesture(gt, frame, gestureId - (--counter)));
			}
		}
		return gl;
	}

	public void changeGesture(){
		String newGesture = gestureLabels.remove(0);
		if (!newGesture.equals(currentGesture)){
			currentGesture = newGesture;
			changed = true;
		}
	}
	
}
