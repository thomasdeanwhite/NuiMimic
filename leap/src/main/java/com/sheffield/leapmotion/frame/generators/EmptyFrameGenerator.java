package com.sheffield.leapmotion.frame.generators;

import com.leapmotion.leap.Frame;
import com.sheffield.leapmotion.controller.mocks.SeededFrame;
import com.sheffield.output.Csv;

public class EmptyFrameGenerator extends FrameGenerator {
	public Csv getCsv() {
		return new Csv();
	}
	public EmptyFrameGenerator() {
	}

	@Override
	public Frame newFrame() {
        return new SeededFrame(Frame.invalid());

	}

	@Override
	public String status() {
		return null;
	}

	public void modifyFrame(SeededFrame frame) {

	}

	@Override
	public boolean allowProcessing() {
		return true;
	}

	@Override
	public String getName() {
		return "Empty Frames";
	}

	private long lastUpdate = 0;
	@Override
	public void tick(long time) {
		lastUpdate = time;
	}

	public long lastTick(){
		return lastUpdate;
	}

	@Override
	public void cleanUp() {

	}
}
