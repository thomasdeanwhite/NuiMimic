package com.sheffield.leapmotion.frameselectors;

import com.leapmotion.leap.Frame;
import com.sheffield.leapmotion.Tickable;
import com.sheffield.output.Csv;

public abstract class FrameSelector implements Tickable {
	public abstract Frame newFrame();

	public abstract String status();

	public abstract void cleanUp();

	public abstract Csv getCsv();
}
