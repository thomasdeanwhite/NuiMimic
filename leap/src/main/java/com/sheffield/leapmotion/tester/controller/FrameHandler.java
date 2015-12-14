package com.sheffield.leapmotion.tester.controller;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.GestureList;
import com.sheffield.leapmotion.mocks.SeededFrame;
import com.sheffield.leapmotion.tester.App;
import com.sheffield.leapmotion.tester.Properties;
import com.sheffield.leapmotion.tester.controller.gestures.GestureHandler;
import com.sheffield.leapmotion.tester.framemodifier.FrameModifier;
import com.sheffield.leapmotion.tester.frameselectors.*;
import com.sheffield.leapmotion.tester.listeners.FrameSwitchListener;

import java.util.ArrayList;

public class FrameHandler {
    private FrameSelector frameSelector;
    private ArrayList<FrameSwitchListener> frameSwitchListeners;
    private ArrayList<Frame> frames;
    private GestureHandler gestureHandler;
    private ArrayList<FrameModifier> frameModifiers;

    public FrameHandler() {
        frameSwitchListeners = new ArrayList<FrameSwitchListener>();
        frameModifiers = new ArrayList<FrameModifier>();
        frames = new ArrayList<Frame>();

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
            case STATIC_DISTANCE:
                frameSelector = new StaticDistanceFrameSelector();
                break;
            case BRANCH_DISTANCE_AVERAGE:
                frameSelector = new BranchDistanceAverageFrameSelector();
                break;
            case N_GRAM:
                frameSelector = new NGramFrameSelector("");
                break;
            case EMPTY:
                frameSelector = new EmptyFrameSelector();
                break;
            default:
                break;
        }

        // addFrameModifier(new RandomFrameModifier());
        if (frameSelector instanceof FrameModifier) {
            addFrameModifier((FrameModifier) frameSelector);
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
        if (i < frames.size()) {
            frame = frames.get(i);
        } else {
            frame = Frame.invalid();
        }
        if (frame instanceof SeededFrame) {
            GestureList gl = null;
            if (gestureHandler != null) {
                gl = gestureHandler.handleFrame(frame);
            }
            if (gl == null) {
                gl = frame.gestures();
            }


            SeededFrame sf = (SeededFrame) frame;
            if (!sf.isGestureSet()) {
                sf.setGestures(gl);
            }
            for (FrameModifier fm : frameModifiers) {
                fm.modifyFrame((SeededFrame) frame);
            }
        }

        return frame;

    }

    public void loadNewFrame() {
        Frame frame = frameSelector.newFrame();
        if (!(frameSelector instanceof UserPlaybackFrameSelector)) {
            frame = new SeededFrame(frame);
        }
        frames.add(0, frame);
        for (FrameSwitchListener fsl : frameSwitchListeners) {
            fsl.onFrameSwitch(getFrame(), frame);
        }

        while (frames.size() > Properties.MAX_LOADED_FRAMES) {
            frames.remove(frames.size() - 1);
        }

    }
}
