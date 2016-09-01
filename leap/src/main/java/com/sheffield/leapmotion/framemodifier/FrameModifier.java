package com.sheffield.leapmotion.framemodifier;

import com.sheffield.leapmotion.Tickable;
import com.sheffield.leapmotion.mocks.SeededFrame;

public interface FrameModifier extends Tickable {

	public void modifyFrame(SeededFrame frame);
	
}
