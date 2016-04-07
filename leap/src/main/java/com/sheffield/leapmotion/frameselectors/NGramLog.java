package com.sheffield.leapmotion.frameselectors;

public class NGramLog {
	public String element;
	public int timeSeeded;

	public NGramLog() {
	}

	public String toString(){
		return element + ":" + timeSeeded + "\n";
	}
}