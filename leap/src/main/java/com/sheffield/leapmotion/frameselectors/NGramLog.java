package com.sheffield.leapmotion.frameselectors;

import com.google.gson.Gson;
import com.sheffield.leapmotion.output.StateComparator;
import com.sheffield.leapmotion.output.TestingStateComparator;

public class NGramLog {
	public String element;
	public int timeSeeded;
	public int state;
	private static Gson GSON = new Gson();

	public NGramLog() {
	}

	public String toString(){

		return element + ":" + timeSeeded + ":" + (TestingStateComparator
				.getCurrentState() >= 0 ? GSON.toJson(TestingStateComparator.getState(TestingStateComparator.getCurrentState())) : "[]") + "\n";
	}
}