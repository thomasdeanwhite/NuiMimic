package com.sheffield.leapmotion.frame.generators;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.GestureList;
import com.sheffield.leapmotion.controller.mocks.SeededFrame;
import com.sheffield.leapmotion.controller.mocks.SeededGestureList;
import com.scythe.output.Csv;

public class EmptyFrameGenerator extends FrameGenerator {

	@Override
	public GestureList handleFrame(Frame frame, Controller controller) {
		return new SeededGestureList();
	}

	public Csv getCsv() {
		return new Csv();
	}

	private Frame currentFrame;

	public EmptyFrameGenerator() {
		currentFrame = null;
	}

	@Override
	public Frame newFrame() {
        return currentFrame;
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

		SeededFrame sf = new SeededFrame(Frame.invalid());
		sf.setId(time);

		currentFrame = sf;
	}

	public long lastTick(){
		return lastUpdate;
	}

	@Override
	public void cleanUp() {

	}
}
