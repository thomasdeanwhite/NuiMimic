package com.sheffield.instrumenter.frameselectors;

import com.leapmotion.leap.Frame;
import com.sheffield.instrumenter.framemodifier.FrameModifier;
import com.sheffield.leapmotion.mocks.SeededFrame;

public class EmptyFrameSelector extends FrameSelector implements FrameModifier {

	public EmptyFrameSelector() {
	}

	@Override
	public Frame newFrame() {
        return new SeededFrame(Frame.invalid());

	}

	public void modifyFrame(SeededFrame frame) {

	}
}
