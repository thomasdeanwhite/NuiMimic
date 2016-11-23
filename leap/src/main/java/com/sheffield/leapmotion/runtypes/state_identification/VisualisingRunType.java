package com.sheffield.leapmotion.runtypes.state_identification;

import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.output.TrainingDataVisualiser;
import com.sheffield.leapmotion.runtypes.RunType;

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
