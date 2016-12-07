package com.sheffield.leapmotion.frame.generators.gestures;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.GestureList;
import com.sheffield.leapmotion.util.Tickable;

public interface GestureHandler extends Tickable {
	public GestureList handleFrame(Frame frame);

}
