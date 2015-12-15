package com.sheffield.instrumenter.framemodifier;

import com.sheffield.leapmotion.mocks.SeededFrame;

public interface FrameModifier {

	public void modifyFrame(SeededFrame frame);
	
}
