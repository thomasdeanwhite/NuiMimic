package com.sheffield.leapmotion.frameselectors;

import com.sheffield.leapmotion.output.DctStateComparator;

public class NGramLog {
	public String element;
	public int timeSeeded;
	public int state;

	public NGramLog() {
	}

	public String toString(){
		return element + ":" + timeSeeded + ":" + DctStateComparator.getCurrentState() + "\n";
	}
}