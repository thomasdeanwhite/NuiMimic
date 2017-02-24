package com.sheffield.leapmotion.frame.generators.gestures;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.GestureList;
import com.leapmotion.leap.Pointable;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.util.FileHandler;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.frame.playback.NGramLog;
import com.sheffield.leapmotion.controller.mocks.SeededCircleGesture;
import com.sheffield.leapmotion.controller.mocks.SeededGesture;
import com.sheffield.leapmotion.controller.mocks.SeededGestureList;
import com.sheffield.leapmotion.controller.mocks.SeededSwipeGesture;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RandomGestureHandler extends NoneGestureHandler {

    private SeededCircleGesture scg;
    private SeededSwipeGesture ssg;
    protected Frame lastFrame;
    protected int gestureTimeLimit = Properties.GESTURE_TIME_LIMIT;


    private File outputFile;
    private ArrayList<NGramLog> logs = new ArrayList<NGramLog>();

    public void setOutputFile(File o) {
        outputFile = o;
    }


    @Override
    public GestureList handleFrame(Frame frame) {
        lastFrame = frame;
        frame = clearFrame(frame);

        SeededGestureList gl = new SeededGestureList();

        if (gestureType == Gesture.Type.TYPE_INVALID)
            return gl;

        gestureCount++;

        gl.addGesture(setupGesture(gestureType, frame, gestureId));
        return gl;
    }

    private Gesture.Type nextGesture = Gesture.Type.TYPE_INVALID;

    public Gesture setupGesture(Gesture.Type gestureType, Frame frame,
                                int gestureId) {
        Gesture g = new SeededGesture(gestureType, gestureState, frame,
                gestureDuration, gestureId);


        if (gestureType == Gesture.Type.TYPE_CIRCLE) {
            scg = new SeededCircleGesture(g);
            if (cumalitiveGesturePositions.equals(Vector.zero())) {
                cumalitiveGesturePositions = lastFrame.pointables().frontmost()
                        .stabilizedTipPosition();
            }
            cumalitiveGesturePositions = cumalitiveGesturePositions
                    .plus(g.pointables().frontmost().stabilizedTipPosition());
            Vector center = cumalitiveGesturePositions.divide(gestureCount + 1);


            //Vector diff = center.minus(g.pointables().frontmost().stabilizedTipPosition());

            scg.setCenter(center);

            Vector gradient = (center.minus(
                    g.pointables().frontmost().stabilizedTipPosition()));
            scg.setRadius(
                    gradient.magnitude() + Properties.GESTURE_CIRCLE_RADIUS);
            gradient = gradient.normalized();
            scg.setNormal(new Vector(gradient.getY(), -gradient.getX(),
                    gradient.getZ()));
            scg.setProgress(gestureDuration / 1000f);
            scg.setPointable(g.pointables().frontmost());

            ((SeededGesture) g).setCircleGesture(scg);

        } else if (gestureType == Gesture.Type.TYPE_SWIPE) {
            Pointable p = g.pointables().frontmost();
            Vector position = p.hand().palmPosition();
            Vector startPosition;
            if (ssg != null) {
                startPosition = ssg.startPosition();
            } else {
                startPosition = position;
            }
            Vector direction = position.minus(startPosition);
            float speed = startPosition.distanceTo(position) / (float)
                    gestureDuration;

            ssg = new SeededSwipeGesture(g, startPosition, position, direction,
                    speed, p);
            ((SeededGesture) g).setSwipeGesture(ssg);
        }

        return g;
    }

    @Override
    public void advanceGestures(long time) {
        //super.advanceGestures(time);
        if (gestureState == null || gestureType == null ||
                gestureState
                        == Gesture.State.STATE_STOP || nextGesture != null) {
            gestureState = Gesture.State.STATE_START;
            //currentGesture = analyzer.getDataAnalyzer().next();

            gestureType = nextGesture;
            nextGesture = null;
            gestureTimeLimit = Properties.GESTURE_TIME_LIMIT;
        } else {
            if (gestureState == Gesture.State.STATE_UPDATE) {

                String[] nextSet = getNextGesture().split("\\+");
                Gesture.Type newType = Gesture.Type.TYPE_INVALID;

                for (String s : nextSet) {
                    Gesture.Type newGesture;
                    try {
                        newGesture = Gesture.Type.valueOf(s);

                        if (newGesture != Gesture.Type.TYPE_INVALID) {
                            newType = newGesture;
                            break;
                        }
                    } catch (Exception e) {

                    }
                }

                if (newType.equals(gestureType)) {
                    int newDuration = (int) (time - gestureStart);
                    gestureTimeLimit = newDuration + Properties.GESTURE_TIME_LIMIT;
                    gestureDuration = newDuration;
                } else if (gestureType.equals(Gesture.Type.TYPE_INVALID)
                        || !newType.equals(Gesture.Type.TYPE_INVALID)) {

                        cumalitiveGesturePositions = Vector.zero();
                        gestureCount = 0;
                        gestureDuration = 3;
                        gestureStart = time - gestureDuration;
                        nextGesture = newType;
                        gestureState = Gesture.State.STATE_STOP;
                        gestureTimeLimit = Properties.GESTURE_TIME_LIMIT;
                } else if (gestureDuration > gestureTimeLimit){
                    nextGesture = newType;
                    gestureState = Gesture.State.STATE_STOP;
                    gestureTimeLimit = Properties.GESTURE_TIME_LIMIT;
                } else {
                    nextGesture = null;
                }

            } else {
                gestureState = Gesture.State.STATE_UPDATE;
            }

//            if (gestureType == Gesture.Type.TYPE_INVALID){
//                return;
//            }
            //update times
            int newDuration = (int) (time - gestureStart);
            gestureDuration = newDuration;

            if (gestureState == Gesture.State.STATE_STOP) {
                NGramLog ngLog = new NGramLog();
                ngLog.element = gestureType.toString() + ",";
                ngLog.timeSeeded = (int) (gestureDuration);
                logs.add(ngLog);
                if (outputFile != null) {
                    try {
                        FileHandler.appendToFile(outputFile, ngLog.toString());
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace(App.out);
                    }
                }
            }
        }
    }

    private long lastUpdate = 0;

    @Override
    public void tick(long time) {
        if (lastFrame == null) {
            lastFrame = SeededController.newFrame();
        }
        lastUpdate = time;
        advanceGestures(time);
    }

    public long lastTick() {
        return lastUpdate;
    }

    @Override
    public String getNextGesture() {
        return super.getNextGesture();
    }
}
