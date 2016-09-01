package com.sheffield.leapmotion.controller.gestures;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.GestureList;
import com.sheffield.leapmotion.Tickable;

public interface GestureHandler extends Tickable {
	public GestureList handleFrame(Frame frame);

}
