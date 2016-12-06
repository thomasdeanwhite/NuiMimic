package com.sheffield.leapmotion.frameselectors;

import com.leapmotion.leap.Frame;
import com.sheffield.leapmotion.framemodifier.FrameModifier;
import com.sheffield.leapmotion.mocks.SeededFrame;
import com.sheffield.output.Csv;

public class EmptyFrameSelector extends FrameSelector implements FrameModifier {
	public Csv getCsv() {
		return new Csv();
	}
	public EmptyFrameSelector() {
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
