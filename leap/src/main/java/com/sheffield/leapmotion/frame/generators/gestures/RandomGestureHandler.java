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

        int counter = 0;
        for (Gesture.Type gestureType : gestureTypes) {
            if (gestureType == Gesture.Type.TYPE_INVALID)
                return gl;

            gestureCount++;

            gl.addGesture(setupGesture(gestureType, frame, gestureId, counter++));
        }
        return gl;
    }

    private Gesture.Type[] nextGestures = null;

    public Gesture setupGesture(Gesture.Type gestureType, Frame frame,
                                int gestureId, int count) {
        Gesture g = new SeededGesture(gestureType, gestureState, frame,
                gestureDuration, gestureId);

        Pointable frontMost = g.pointables().frontmost();
        Pointable lastFrontMost = lastFrame.pointables().frontmost();

        if (count > 0){
            frontMost = g.pointables().get(count);
            lastFrontMost = lastFrame.pointables().get(count);                gestureCount = 0;
        }


        if (gestureType == Gesture.Type.TYPE_CIRCLE) {
            scg = new SeededCircleGesture(g);
            if (cumalitiveGesturePositions.size() <= count) {
                cumalitiveGesturePositions.add(lastFrontMost.stabilizedTipPosition());
            }
            //cumalitiveGesturePositions.add(count+1, cumalitiveGesturePositions.get(count).plus      (frontMost.stabilizedTipPosition()));
            //cumalitiveGesturePositions.remove(count);
            Vector center = cumalitiveGesturePositions.get(count);//.divide(gestureCount + 1);


            //Vector diff = center.minus(g.pointables().frontmost().stabilizedTipPosition());

            scg.setCenter(center);

            Vector gradient = (center.minus(
                    frontMost.stabilizedTipPosition()));
            scg.setRadius(
                    gradient.magnitude() + Properties.GESTURE_CIRCLE_RADIUS);
            gradient = gradient.normalized();
            scg.setNormal(new Vector(gradient.getY(), -gradient.getX(),
                    gradient.getZ()));
            scg.setProgress(gestureDuration / 1000f);
            scg.setPointable(frontMost);

            ((SeededGesture) g).setCircleGesture(scg);

        } else if (gestureType == Gesture.Type.TYPE_SWIPE) {
            Pointable p = frontMost;
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
        if (gestureTypes == null){
            String[] stringNext = nextGestures();
            nextGestures = new Gesture.Type[stringNext.length];

            for (int i = 0; i < stringNext.length; i++){
                nextGestures[i] = Gesture.Type.valueOf(stringNext[i]);
            }
        }

            if (gestureState == null || gestureTypes == null ||
                    gestureState
                            == Gesture.State.STATE_STOP) {
                gestureState = Gesture.State.STATE_START;
                //currentGesture = analyzer.getDataAnalyzer().next();
                gestureStart = time-3;
                gestureTypes = nextGestures;
                nextGestures = null;
                gestureTimeLimit = Properties.GESTURE_TIME_LIMIT;
                gestureCount = 0;
                cumalitiveGesturePositions.clear();
            } else {
                    if (gestureState == Gesture.State.STATE_UPDATE) {

                        String[] stringNext = nextGestures();

                        boolean continueGesture = true;

                        for (String s : stringNext) {
                            Gesture.Type newGesture;
                            try {
                                newGesture = Gesture.Type.valueOf(s);

                                boolean found = false;

                                for (Gesture.Type currentGestures : gestureTypes) {
                                    if (currentGestures.equals(newGesture)) {
                                        found = true;
                                    }
                                }

                                if (!found) {
                                    continueGesture = false;
                                }
                            } catch (Exception e) {

                            }

                        }

                        if (continueGesture) {
                            int newDuration = (int) (time - gestureStart);
                            gestureTimeLimit = newDuration + Properties.GESTURE_TIME_LIMIT;
                            gestureDuration = newDuration;
                        } else if (gestureDuration > gestureTimeLimit) {
                            nextGestures = new Gesture.Type[stringNext.length];

                            for (int i = 0; i < stringNext.length; i++){
                                nextGestures[i] = Gesture.Type.valueOf(stringNext[i]);
                            }
                            gestureState = Gesture.State.STATE_STOP;
                            gestureTimeLimit = Properties.GESTURE_TIME_LIMIT;
                        } else {
                            nextGestures = null;
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
                        ngLog.element = "";
                        for (Gesture.Type gt : gestureTypes) {
                            ngLog.element += gt.toString() + ",";
                        }
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

    private String[] nextGestures(){
        return getNextGesture().split("\\+");
    }
}
