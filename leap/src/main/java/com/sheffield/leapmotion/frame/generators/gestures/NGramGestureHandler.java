package com.sheffield.leapmotion.frame.generators.gestures;

import com.leapmotion.leap.Gesture;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.frame.analyzer.AnalyzerApp;
import com.sheffield.leapmotion.frame.analyzer.ProbabilityListener;

import java.io.File;
import java.util.ArrayList;

public class NGramGestureHandler extends RandomGestureHandler {

	private AnalyzerApp analyzer;
	private ArrayList<Gesture.Type> gestureTypes;
	private String currentGesture;
	
	private File outputFile;
	
	public void setOutputFile(File f){
		outputFile = f;
		super.setOutputFile(f);
	}

	public void setAnalyzer(AnalyzerApp an){
		analyzer = an;
	}
	
	public void addProbabilityListener(ProbabilityListener pbl){
		analyzer.addProbabilityListener(pbl);
	}

	public NGramGestureHandler(AnalyzerApp an) {
		gestureTypes = new ArrayList<Gesture.Type>();
		setAnalyzer(an);
		analyzer.analyze();
	}

	public NGramGestureHandler(String filename) {
		try {
			gestureTypes = new ArrayList<Gesture.Type>();
			String sequenceFile = Properties.DIRECTORY  + "/" + filename + ".gesture_type_ngram";
			analyzer = new AnalyzerApp(sequenceFile);
			//analyzer.setLogBase(true);
			analyzer.analyze();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace(App.out);
		}

	}

//	public void advanceGestures(long time){
//		super.advanceGestures(time);
//		String nextGesture = analyzer.getDataAnalyzer().next();
//
//		while (nextGesture == null || nextGesture.equals("NULL")){
//			nextGesture = analyzer.getDataAnalyzer().next();
//		}
//
//		if (gestureState == null || gestureState == Gesture.State.STATE_STOP){
//			currentGesture = nextGesture;
//			gestureState = Gesture.State.STATE_START;
//			//currentGesture = analyzer.getDataAnalyzer().next();
//
//			gestureTypes.clear();
//			cumalitiveGesturePositions = Vector.zero();
//			gestureCount = 0;
//			cumalitiveGesturePositions = Vector.zero();
//			String[] gestures = currentGesture.split("\\+");
//			gestureDuration = 3;
//			gestureStart = System.currentTimeMillis()-gestureDuration;
//			for (String s : gestures){
//				if (s != null && !s.equalsIgnoreCase("null") && !s.equals("TYPE_INVALID")){
//					gestureTypes.add(Gesture.Type.valueOf(s));
//
//					gestureId++;
//				}
//			}
//			gestureCount = 0;
//		} else {
//			if (gestureState == Gesture.State.STATE_UPDATE) {
//
//
//				if (gestureDuration > GESTURE_TIME_LIMIT && !nextGesture.equals(currentGesture)) {
//					gestureState = Gesture.State.STATE_STOP;
//					if (outputFile != null){
//						NGramLog nLog = new NGramLog();
//						String gestures = "";
//						for (Gesture.Type gt : gestureTypes) {
//							gestures += gt + ",";
//						}
//
//						if (gestures.length() == 0){
//							gestures = "TYPE_INVALID,";
//						}
//						gestures = gestures.substring(0, gestures.length()-1);
//						nLog.element = gestures;
//						nLog.timeSeeded = gestureDuration;
//						try {
//							FileHandler.appendToFile(outputFile, nLog.toString());
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					}
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
//		gestureCount++;
//		if (gestureTypes.size() <= 0)
//			return gl;
//		int counter = gestureTypes.size();
//		for (Gesture.Type gt : gestureTypes) {
//			if (gt == Gesture.Type.TYPE_INVALID){
//				break;
//			}
//			gl.addGesture(setupGesture(gt, frame, gestureId-(--counter)));
//		}
//		return gl;
//	}
//
//	private long lastUpdate = 0;
//	@Override
//	public void tick(long time) {
//		lastUpdate = time;
//		if (lastFrame == null){
//			lastFrame = SeededController.newFrame();
//		}
//		advanceGestures(time);
//	}
//
//	public long lastTick(){
//		return lastUpdate;
//	}

	@Override
	public String getNextGesture() {
		return analyzer.getDataAnalyzer().next().split("\\+")[0];
	}
}
