package com.sheffield.leapmotion.frameselectors;

import com.google.gson.Gson;
import com.sheffield.leapmotion.output.StateComparator;

public class NGramLog {
	public String element;
	public int timeSeeded;
	public int state;
	private static Gson GSON = new Gson();

	public NGramLog() {
	}

	public String toString(){

		return element + ":" + timeSeeded + ":" + (StateComparator.getCurrentState() >= 0 ? GSON.toJson(StateComparator.getState(StateComparator.getCurrentState())) : "[]") + "\n";
	}
}