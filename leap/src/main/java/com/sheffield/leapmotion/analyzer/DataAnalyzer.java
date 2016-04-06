package com.sheffield.leapmotion.analyzer;

import java.util.ArrayList;

public interface DataAnalyzer {
	void analyze(ArrayList<String> frames, boolean logBase);

	void output(String directory);

	String next();

	void addProbabilityListener(ProbabilityListener pbl);
}
