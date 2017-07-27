package com.sheffield.leapmotion.frame.generators.gestures;

import com.leapmotion.leap.*;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.controller.mocks.*;
import com.sheffield.leapmotion.util.FileHandler;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.frame.playback.NGramLog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RandomGestureHandler extends NoneGestureHandler {

    private SeededCircleGesture scg;
    private SeededSwipeGesture ssg;
    protected Frame lastFrame;
    protected int gestureTimeLimit = Properties.GESTURE_TIME_LIMIT;

    private ArrayList<NGramLog> logs = new ArrayList<NGramLog>();


    @Override
    public GestureList handleFrame(Frame frame, Controller controller) {
        lastFrame = frame;
        frame = clearFrame(frame);

        SeededGestureList gl = new SeededGestureList();

        int counter = 0;

        if (gestureTypes == null || gestureTypes.length == 0){
            return new SeededGestureList();
        }

        for (int i = 0; i < gestureTypes.length; i++) {
            Gesture.Type gestureType = gestureTypes[i];
            Finger.Type fingerType = fingerTypes[i];
            if (gestureType == Gesture.Type.TYPE_INVALID)
                continue;

            gestureCount++;

            Gesture g = setupGesture(gestureType, frame, gestureId, counter++, controller);

            gl.addGesture(g);

            if (fingerType != null){

                Finger ft = Finger.invalid();

                for (Finger f : frame.fingers()){
                    if (f.type().equals(fingerType)){
                        ft = f;
                        break;
                    }
                }

                ((SeededGesture)g).addPointable((SeededFinger) ft);

                if (((SeededGesture)g).getCircleGesture() != null){
                    ((SeededCircleGesture)((SeededGesture)g).getCircleGesture()).setProgress(gestureDuration/1000f);
                    ((SeededCircleGesture)((SeededGesture)g).getCircleGesture()).setPointable(ft);
                }

            } else {
                Finger f = Finger.invalid();

                SeededGesture sg = ((SeededGesture) g);
                if (sg.getCircleGesture() != null){
                    f = (Finger) sg.getCircleGesture().pointable();
                }

                if (f != null) {
                    ((SeededGesture) g).addPointable((SeededFinger) f);
                }

                if (((SeededGesture)g).getCircleGesture() != null){
                    ((SeededCircleGesture)((SeededGesture)g).getCircleGesture()).setProgress(gestureDuration/1000f);

                }
            }
        }
        return gl;
    }

    private Gesture.Type[] nextGestures = null;
    private Finger.Type[] nextFingers = null;

    public Gesture setupGesture(Gesture.Type gestureType, Frame frame,
                                int gestureId, int count, Controller controller) {
        Gesture g = new SeededGesture(gestureType, gestureState, frame,
                gestureDuration, gestureId);

        Pointable frontMost = g.pointables().frontmost();
        Pointable lastFrontMost = lastFrame.pointables().frontmost();

        if (count > 0) {
            frontMost = g.pointables().get(count);
            gestureCount = 0;
        }


        if (gestureType == Gesture.Type.TYPE_CIRCLE) {
            scg = new SeededCircleGesture(g);
            if (cumalitiveGesturePositions.size() <= count) {
                Vector center = Vector.zero();
                int counter = 0;
                for (int i = 1; i < Properties.GESTURE_CIRCLE_FRAMES; i++) {
                    Frame f = controller.frame(i);

                    if (!f.isValid()) {
                        break;
                    }

                    if (count > 0) {
                        frontMost = g.pointables().get(count);
                        center = center.plus(f.pointables().get(count).stabilizedTipPosition());
                    } else {
                        center = center.plus(f.pointables().frontmost().stabilizedTipPosition());
                    }
                    counter++;
                }

                center = center.plus(frontMost.stabilizedTipPosition());
                counter++;

                center = center.divide(counter);
                cumalitiveGesturePositions.add(center);
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
        String[] nextGest = nextGestures();
        if (gestureTypes == null) {
            String[] stringNextRaw = nextGest;
            ArrayList<Gesture.Type> stringNext = new ArrayList<>();
            ArrayList<Finger.Type> fingersNext = new ArrayList<>();

            for (String s : stringNextRaw) {
                if (s != null && !s.equals("null")) {
                    try {
                        String[] gest = s.split(">>");

                        stringNext.add(Gesture.Type.valueOf(gest[0]));
                        fingersNext.add(Finger.Type.valueOf(gest[1]));
                    } catch (Exception e) {
                        // The exception is either null or not a valid gesture
                        if (fingersNext.size() < stringNext.size()){
                            fingersNext.add(null);
                        }
                    }
                }
            }

            nextGestures = new Gesture.Type[stringNext.size()];
            nextFingers = new Finger.Type[fingersNext.size()];

            for (int i = 0; i < stringNext.size(); i++) {
                nextGestures[i] = stringNext.get(i);
                nextFingers[i] = fingersNext.get(i);
            }

            gestureTypes = nextGestures;
            fingerTypes = nextFingers;
        }

        if (gestureState == null || gestureTypes == null ||
                gestureState
                        == Gesture.State.STATE_STOP) {
            gestureState = Gesture.State.STATE_START;
            //currentGesture = analyzer.getDataAnalyzer().next();
            gestureStart = time - 3;
            gestureTypes = nextGestures;
            fingerTypes = nextFingers;
            nextGestures = null;
            nextFingers = null;
            gestureTimeLimit = Properties.GESTURE_TIME_LIMIT;
            gestureCount = 0;
            cumalitiveGesturePositions.clear();
        } else {

            gestureTypes = nextGestures;
            fingerTypes = nextFingers;

            if (gestureState == Gesture.State.STATE_UPDATE && gestureTypes != null && gestureTypes.length > 0) {

                String[] stringNext = nextGest;

                boolean continueGesture = true;

                for (String s : stringNext) {
                    Gesture.Type newGesture;
                    try {

                        String gest[] = s.split(">>");

                        newGesture = Gesture.Type.valueOf(gest[0]);

                        boolean found = false;

                        for (Gesture.Type currentGestures : gestureTypes) {
                            if (currentGestures.equals(newGesture) && currentGestures != Gesture.Type.TYPE_INVALID) {
                                found = true;
                            }
                        }

                        if (!found) {
                            continueGesture = false;
                        }
                    } catch (Exception e) {
                        App.out.println(e);
                    }

                }

                if (continueGesture) {
                    int newDuration = (int) (time - gestureStart);
                    gestureTimeLimit = newDuration + Properties.GESTURE_TIME_LIMIT;
                    gestureDuration = newDuration;
                } else if (gestureDuration > gestureTimeLimit) {
                    nextGestures = new Gesture.Type[stringNext.length];
                    nextFingers = new Finger.Type[stringNext.length];

                    for (int i = 0; i < stringNext.length; i++) {
                        String gest[] = stringNext[i].split(">>");
                        nextGestures[i] = Gesture.Type.valueOf(gest[0]);
                        if (gest.length > 1) {
                            nextFingers[i] = Finger.Type.valueOf(gest[1]);
                        } else {
                            nextFingers[i] = null;
                        }
                    }
                    gestureState = Gesture.State.STATE_STOP;
                    gestureTimeLimit = Properties.GESTURE_TIME_LIMIT;
                }

            } else {
                gestureState = Gesture.State.STATE_UPDATE;
            }

            int newDuration = (int) (time - gestureStart);
            gestureDuration = newDuration;

            if (gestureState == Gesture.State.STATE_STOP) {
                NGramLog ngLog = new NGramLog();
                ngLog.element = "";
                for (Gesture.Type gt : gestureTypes) {
                    ngLog.element += gt.toString() + ",";
                }
                ngLog.timeSeeded = gestureDuration;
                logs.add(ngLog);
                if (outputFile != null) {
                    try {
                        if (!outputFile.exists()) {
                            if (!outputFile.getParentFile().exists()) {
                                outputFile.getParentFile().mkdirs();
                            }
                            outputFile.createNewFile();
                        }
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

    private String[] nextGestures() {
        return getNextGesture().split("\\+");
    }
}
