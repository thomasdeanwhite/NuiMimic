package com.sheffield.leapmotion.tester.frameselectors;

import com.leapmotion.leap.Frame;
import com.sheffield.leapmotion.mocks.SeededFrame;
import com.sheffield.leapmotion.tester.framemodifier.FrameModifier;

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
