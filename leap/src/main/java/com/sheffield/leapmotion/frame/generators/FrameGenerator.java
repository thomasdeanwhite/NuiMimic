package com.sheffield.leapmotion.frame.generators;

import com.leapmotion.leap.Frame;
import com.sheffield.leapmotion.App;
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

	public boolean hasNextFrame(){
		return true;
	}

	public float getProgress(){
		return App.getApp().getProgress();
	}

	public abstract String getName();
}
