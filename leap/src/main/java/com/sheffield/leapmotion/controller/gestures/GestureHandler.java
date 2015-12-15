package com.sheffield.leapmotion.controller.gestures;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.GestureList;

public interface GestureHandler {
	public GestureList handleFrame(Frame frame);
}
