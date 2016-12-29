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
            String sequenceFile = Properties.DIRECTORY + "/" + filename +
                    ".raw_sequence.gesture_type_data";
            gestureLabels = new ArrayList<String>();

            String gestureData = FileHandler.readFile(new File(sequenceFile));

            String[] gestureInfo = gestureData.split(" ");

            for (String s : gestureInfo) {
                gestureLabels.add(s);
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace(App.out);
        }
    }

	public String getNextGesture() {
		return gestureLabels.get(currentGesture);
	}

	public void changeGesture(){
		currentGesture++;
	}
	
}
