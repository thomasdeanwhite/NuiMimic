package com.sheffield.leapmotion.controller.listeners;

import com.leapmotion.leap.Frame;

public interface FrameSwitchListener {
	void onFrameSwitch(Frame lastFrame, Frame nextFrame);
}
