package com.sheffield.leapmotion.frame.generators.gestures;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.GestureList;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.util.FileHandler;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.frame.analyzer.AnalyzerApp;
import com.sheffield.leapmotion.controller.mocks.SeededGestureList;

import java.io.File;
import java.util.ArrayList;

public class ReconstructiveGestureHandler extends RandomGestureHandler {

	private ArrayList<String> gestureLabels;
	private int currentGesture = 0;

	public ReconstructiveGestureHandler(String filename) {
		//super(filename);
		try {
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
//
//	@Override
//	public void advanceGestures(long time){
//
//		if (currentGesture == null){
//			return;
//		}
//
//		if (gestureState == null || gestureTypes.size() == 0 || gestureState == Gesture.State.STATE_STOP){
//			gestureState = Gesture.State.STATE_START;
//
//			gestureTypes.clear();
//			cumalitiveGesturePositions = Vector.zero();
//			gestureCount = 0;
//			String[] gestures = currentGesture.split("\\+");
//			gestureDuration = 3;
//			gestureStart = time-gestureDuration;
//			for (String s : gestures){
//				if (s != null && !s.equalsIgnoreCase("null")){
//					gestureTypes.add(Gesture.Type.valueOf(s));
//
//					gestureId++;
//				}
//			}
//		} else {
//			if (gestureState == Gesture.State.STATE_UPDATE) {
//				if (changed){
//					changed = false;
//					gestureState = Gesture.State.STATE_STOP;
//				}
//
//			} else {
//				gestureState = Gesture.State.STATE_UPDATE;
//			}
//			//update times
//			gestureDuration = (int) (System.currentTimeMillis() - gestureStart);
//		}
//	}
//
//	@Override
//	public GestureList handleFrame(Frame frame) {
//		lastFrame = frame;
//		frame = clearFrame(frame);
//		SeededGestureList gl = new SeededGestureList();
//
//		if (currentGesture == null || currentGesture.equals("TYPE_INVALID")){
//			return gl;
//		}
//
//		gestureCount++;
//		if (gestureTypes.size() <= 0)
//			return gl;
//		int counter = gestureTypes.size();
//		for (Gesture.Type gt : gestureTypes) {
//			if (!gt.equals(Gesture.Type.TYPE_INVALID)) {
//				gl.addGesture(setupGesture(gt, frame, gestureId - (--counter)));
//			}
//		}
//		return gl;
//	}
//
//	public void changeGesture(long time){
//		String newGesture = gestureLabels.remove(0);
//		if (!newGesture.equals(currentGesture)){
//			currentGesture = newGesture;
//			changed = true;
//		}
//	}

	public String getNextGesture() {
		return gestureLabels.get(currentGesture);
	}

	public void changeGesture(){
		currentGesture++;
	}
	
}
