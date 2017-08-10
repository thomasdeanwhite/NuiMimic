package com.sheffield.leapmotion.frame.generators.gestures;

import com.leapmotion.leap.*;
import com.sheffield.leapmotion.controller.mocks.SeededCircleGesture;
import com.sheffield.leapmotion.controller.mocks.SeededGesture;
import com.sheffield.leapmotion.frame.analyzer.machinelearning.ngram.NGram;

import java.util.HashMap;

public class SequenceGestureHandler extends RandomGestureHandler {

    private HashMap<String, SeededCircleGesture> circleGestures;
    private String nextGesture;
    private String nextCircleGesture;

    public void setNextGesture(String nextGesture) {
        this.nextGesture = nextGesture;
    }

    public void setNextCircleGesture(String nextCircleGesture) {
        this.nextCircleGesture = nextCircleGesture;
    }

    public SequenceGestureHandler(HashMap<String, SeededCircleGesture> circleGestures){
        this.circleGestures = circleGestures;

    }

    @Override
    public String getNextGesture() {
        return nextGesture;
    }

    @Override
    public Gesture setupGesture(Gesture.Type gestureType, Frame frame, int gestureId, int count, Controller controller) {
        if (gestureType.equals(Gesture.Type.TYPE_CIRCLE)) { //setup circle

            SeededGesture sg = new SeededGesture(gestureType, gestureState, frame, gestureDuration, gestureId);

            SeededCircleGesture scg = circleGestures.get(nextCircleGesture).copy();

            sg.setCircleGesture(scg);
            scg.setGesture(sg);
            return sg;
        }
        return super.setupGesture(gestureType, frame, gestureId, count, controller);
    }

    public HashMap<String, SeededCircleGesture> getCircleGestures() {
        return circleGestures;
    }
}
