package com.sheffield.leapmotion.frame.generators.gestures;

import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.util.FileHandler;

import java.io.File;
import java.util.ArrayList;

public class ReconstructiveGestureHandler extends RandomGestureHandler {

	private ArrayList<String> gestureLabels;
	private int currentGesture = 0;

	public ReconstructiveGestureHandler(String filename) {
        //super(filename);
        try {
            String sequenceFile = Properties.DIRECTORY + "/" + filename +
                    "/processed/gesture_type_data.raw_sequence";
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

	public void changeGesture(int num){
		currentGesture=num;
	}
	
}
