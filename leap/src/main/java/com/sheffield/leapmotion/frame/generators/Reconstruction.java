package com.sheffield.leapmotion.frame.generators;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.GestureList;

/**
 * Created by thomas on 07/03/17.
 */
public interface Reconstruction {

    int getClusters();

    void setFrame(int index);

    GestureList handleFrame(Frame frame, Controller controller);
}
