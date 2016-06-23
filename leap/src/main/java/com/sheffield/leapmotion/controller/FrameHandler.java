package com.sheffield.leapmotion.controller;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.GestureList;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.controller.gestures.GestureHandler;
import com.sheffield.leapmotion.controller.gestures.RandomGestureHandler;
import com.sheffield.leapmotion.framemodifier.FrameModifier;
import com.sheffield.leapmotion.frameselectors.*;
import com.sheffield.leapmotion.listeners.FrameSwitchListener;
import com.sheffield.leapmotion.mocks.SeededFrame;

import java.util.ArrayList;

public class FrameHandler {
    private FrameSelector frameSelector;
    private ArrayList<FrameSwitchListener> frameSwitchListeners;
    private ArrayList<Frame> frames;
    private GestureHandler gestureHandler;
    private ArrayList<FrameModifier> frameModifiers;

    public FrameHandler() {

    }

    public void init (){
        frameSwitchListeners = new ArrayList<FrameSwitchListener>();
        frameModifiers = new ArrayList<FrameModifier>();
        frames = new ArrayList<Frame>();
        try {
            switch (Properties.FRAME_SELECTION_STRATEGY) {
                case RANDOM:
                    frameSelector = new RandomFrameSelector();
                    break;
                case EUCLIDEAN:
                    frameSelector = new EuclideanFrameSelector();
                    break;
                case RANDOM_DISTANCE:
                    frameSelector = new RandomDistanceFrameSelector();
                    break;
                case ADAPTIVE_RANDOM_DISTANCE:
                    frameSelector = new AdaptiveRandomDistanceFrameSelector();
                    break;
                case SINGLE_MODEL:
                    frameSelector = new StaticDistanceFrameSelector();
                    break;
                case N_GRAM:
                    frameSelector = new NGramFrameSelector("");
                    break;
                case EMPTY:
                    frameSelector = new EmptyFrameSelector();
                    break;
                case VQ:
                    frameSelector = new RandomTemplateFrameSelector(Properties.GESTURE_FILES[0]);
                    break;
                case STATE_DEPENDANT:
                    frameSelector = new StateRelatedStaticDistanceFrameSelector();
                    break;
                case REPRODUCTION:
                    frameSelector = new TrainingDataPlaybackFrameSelector(Properties.GESTURE_FILES[0]);
                    break;
                default:
                    frameSelector = new RandomFrameSelector();
                    break;
            }
        } catch (Exception e){
            e.printStackTrace(App.out);
        }

        // addFrameModifier(new RandomFrameModifier());
        if (frameSelector instanceof FrameModifier) {
            addFrameModifier((FrameModifier) frameSelector);
        }

        if (frameSelector instanceof TrainingDataPlaybackFrameSelector){
            setGestureHandler((GestureHandler) frameSelector);
        } else {
            setGestureHandler(new RandomGestureHandler());
        }

        String output = Properties.FRAME_SELECTION_STRATEGY.toString();

        if (Properties.PLAYBACK_FILE != null) {
            FrameSelector backupFs = frameSelector;
            frameSelector = new UserPlaybackFrameSelector(backupFs);
            output = "USER_PLAYBACK_FRAME_SELECTOR(" + output + ")";
        }

        App.out.println("- Using " + output + " for frame selection.");
    }

    public void addFrameModifier(FrameModifier fm) {
        frameModifiers.add(fm);
    }

    public void setGestureHandler(GestureHandler gh) {
        gestureHandler = gh;
    }

    public void addFrameSwitchListener(FrameSwitchListener fsl) {
        if (!frameSwitchListeners.contains(fsl)) {
            frameSwitchListeners.add(fsl);
        }
    }

    public Frame getFrame() {
        return getFrame(0);
    }

    public Frame getFrame(int i) {
        Frame frame = null;
        if (i < frames.size() && i >= 0) {
            frame = frames.get(i);
        }

        if (frame == null){
            frame = Frame.invalid();
        }

        return frame;
    }

    public void loadNewFrame() {
        Frame frame = frameSelector.newFrame();
        if (frameSelector instanceof EmptyFrameSelector){
            final Frame last = getFrame(1);
            final Frame next = frame;
            for (FrameSwitchListener fsl : frameSwitchListeners) {
                final FrameSwitchListener fl = fsl;

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        fl.onFrameSwitch(last, next);
                    }
                }).start();
            }
            return;
//        }
//        if (frameSelector instanceof UserPlaybackFrameSelector) {
//            UserPlaybackFrameSelector upfs = (UserPlaybackFrameSelector) frameSelector;
//            if (upfs.finished()){
//                frameSelector = upfs.getBackupFrameSelector();
//                loadNewFrame();
//                return;
//            }
        } else {
            if (!(frame instanceof  SeededFrame)) {
                frame = new SeededFrame(frame);
            }
            SeededFrame sf = (SeededFrame) frame;

            //App.out.println(sf.toJson());
            GestureList gl = null;
            for (FrameModifier fm : frameModifiers) {
                fm.modifyFrame(sf);
            }
            if (gestureHandler == null) {
                gestureHandler = new RandomGestureHandler();
            }
            gl = gestureHandler.handleFrame(frame);
            if (gl == null) {
                gl = frame.gestures();
            }


            sf.setGestures(gl);
        }
        frames.add(0, frame);
        while (frames.size() > Properties.MAX_LOADED_FRAMES) {
            frames.remove(frames.size() - 1);
        }
        final Frame last = getFrame(1);
        final Frame next = frame;
        for (int i = 0; i < frameSwitchListeners.size(); i++) {
            final FrameSwitchListener fl = frameSwitchListeners.get(i);

            new Thread(new Runnable() {

                @Override
                public void run() {
                    fl.onFrameSwitch(last, next);
                }
            }).start();
        }
    }
}
