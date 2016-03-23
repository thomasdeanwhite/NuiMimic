package com.sheffield.leapmotion.analyzer;

import java.util.ArrayList;

public interface DataAnalyzer {
	public void analyze(ArrayList<String> frames);

	public void output(String directory);

	public String next();

	void addProbabilityListener(ProbabilityListener pbl);
}
