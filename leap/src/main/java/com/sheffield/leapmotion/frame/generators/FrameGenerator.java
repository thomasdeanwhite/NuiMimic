package com.sheffield.leapmotion.frame.generators;

import com.leapmotion.leap.Frame;
import com.sheffield.leapmotion.util.Tickable;
import com.sheffield.leapmotion.controller.mocks.SeededFrame;
import com.sheffield.output.Csv;

public abstract class FrameGenerator implements Tickable {
	public abstract Frame newFrame();

	public abstract String status();

	public abstract void cleanUp();

	public abstract Csv getCsv();

	public abstract void modifyFrame(SeededFrame frame);

	public abstract boolean allowProcessing();
}