package com.sheffield.leapmotion.frame.generators.gestures;

import com.google.gson.Gson;
import com.leapmotion.leap.Gesture;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.frame.analyzer.AnalyzerApp;
import com.sheffield.leapmotion.frame.analyzer.ProbabilityListener;
import com.sheffield.leapmotion.frame.analyzer.machinelearning.ngram.NGram;
import com.sheffield.leapmotion.frame.analyzer.machinelearning.ngram.NGramModel;
import com.sheffield.leapmotion.frame.generators.NGramFrameGenerator;
import com.sheffield.leapmotion.util.FileHandler;

import java.io.File;
import java.util.ArrayList;

public class NGramGestureHandler extends RandomGestureHandler {

	private NGram ngram;
	
	private File outputFile;

	private String currentGesture = "";
	
	public void setOutputFile(File f){
		outputFile = f;
		super.setOutputFile(f);
	}


	public NGramGestureHandler(NGram ng) {

		ngram = ng;

	}

	public NGramGestureHandler(String filename) {
		try {

			String sequenceFile = Properties.DIRECTORY  + "/" + filename + "/processed/gesture_type_ngram";
			Gson gson = new Gson();

			ngram = gson.fromJson(FileHandler.readFile(new File(sequenceFile)), NGram.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace(App.out);
		}

	}


	@Override
	public String getNextGesture() {
		currentGesture = ngram.babbleNext(currentGesture);
		return NGramFrameGenerator.getLastLabel(currentGesture);
	}
}
