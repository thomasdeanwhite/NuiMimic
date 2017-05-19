package com.sheffield.leapmotion.frame.generators.gestures;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.GestureList;
import com.sheffield.leapmotion.util.Tickable;

import java.io.File;

public interface GestureHandler extends Tickable {
	GestureList handleFrame(Frame frame, Controller controller);

	void setGestureOutputFile(File f);

}
