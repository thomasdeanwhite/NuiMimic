package com.sheffield.leapmotion.listeners;

import com.leapmotion.leap.Frame;

public interface FrameSwitchListener {
	public void onFrameSwitch(Frame lastFrame, Frame nextFrame);
}
