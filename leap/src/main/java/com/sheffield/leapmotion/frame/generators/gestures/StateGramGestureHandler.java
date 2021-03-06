package com.sheffield.leapmotion.frame.generators.gestures;

import com.sheffield.leapmotion.frame.analyzer.machinelearning.ngram.NGram;
import com.sheffield.leapmotion.frame.generators.NGramFrameGenerator;
import com.sheffield.leapmotion.output.StateComparator;

import java.io.File;
import java.util.HashMap;

public class StateGramGestureHandler extends RandomGestureHandler {

	private HashMap<Integer, NGram> ngrams;

	private File outputFile;

	private String currentGesture = "";

	public void setGestureOutputFile(File f){
		outputFile = f;
		super.setGestureOutputFile(f);
	}


	public StateGramGestureHandler(HashMap<Integer, NGram> ngs) {
		ngrams = ngs;
	}


	@Override
	public String getNextGesture() {
		int currentState = StateComparator.getCurrentState();

		if (ngrams.containsKey(currentState)) {
			currentGesture = ngrams.get(currentState).babbleNext(currentGesture);
		} else {
			currentGesture = ngrams.get(-1).babbleNext(currentGesture);
		}

		return NGramFrameGenerator.getLastLabel(currentGesture);
	}
}
