package com.sheffield.leapmotion.runtypes;

import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.output.TrainingDataVisualiser;

/**
 * Created by thomas on 18/11/2016.
 */
public class VisualisingRunType implements RunType {
    @Override
    public int run() {
        TrainingDataVisualiser
                tdv = new TrainingDataVisualiser(Properties.INPUT[0]);
        return 0;
    }
}
