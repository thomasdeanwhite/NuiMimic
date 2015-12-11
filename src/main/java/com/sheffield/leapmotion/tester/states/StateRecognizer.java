package com.sheffield.leapmotion.tester.states;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import com.sheffield.leapmotion.tester.App;
import com.sheffield.leapmotion.tester.analysis.ClassAnalyzer;

public abstract class StateRecognizer {

	public static final float SIMILARITY_THRESHOLD = 0.4f;

	public abstract int recognizeState();
	
	public abstract boolean isProcessing();

}
