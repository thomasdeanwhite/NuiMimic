package com.sheffield.leapmotion.controller;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.GestureList;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.FileHandler;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.controller.gestures.GestureHandler;
import com.sheffield.leapmotion.controller.gestures.RandomGestureHandler;
import com.sheffield.leapmotion.framemodifier.FrameModifier;
import com.sheffield.leapmotion.frameselectors.AdaptiveRandomDistanceFrameSelector;
import com.sheffield.leapmotion.frameselectors.ClusterPlaybackFrameSelector;
import com.sheffield.leapmotion.frameselectors.EmptyFrameSelector;
import com.sheffield.leapmotion.frameselectors.EuclideanFrameSelector;
import com.sheffield.leapmotion.frameselectors.FrameSelector;
import com.sheffield.leapmotion.frameselectors.NGramLog;
import com.sheffield.leapmotion.frameselectors.RandomDistanceFrameSelector;
import com.sheffield.leapmotion.frameselectors.RandomFrameSelector;
import com.sheffield.leapmotion.frameselectors.RandomTemplateFrameSelector;
import com.sheffield.leapmotion.frameselectors.SingleModelGuidedRandomFrameSelector;
import com.sheffield.leapmotion.frameselectors.StateRelatedStaticDistanceFrameSelector;
import com.sheffield.leapmotion.frameselectors.StaticDistanceFrameSelector;
import com.sheffield.leapmotion.frameselectors.ReconstructiveFrameSelector;
import com.sheffield.leapmotion.frameselectors.UserPlaybackFrameSelector;
import com.sheffield.leapmotion.listeners.FrameSwitchListener;
import com.sheffield.leapmotion.mocks.SeededFrame;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class FrameHandler {
    private FrameSelector frameSelector;
    private ArrayList<FrameSwitchListener> frameSwitchListeners;
    private ArrayList<Frame> frames;
    private GestureHandler gestureHandler;
    private ArrayList<FrameModifier> frameModifiers;

    public String status(){
        return frameSelector.status();
    }

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
                    //frameSelector = new NGramFrameSelector("");
                    throw new UnsupportedOperationException("NGram is deprecated. Please use SINGLE_MODEL or STATE_DEPENDENT");
                case EMPTY:
                    frameSelector = new EmptyFrameSelector();
                    break;
                case VQ:
                    frameSelector = new RandomTemplateFrameSelector(Properties.GESTURE_FILES[0]);
                    break;
                case STATE_DEPENDENT:
                    frameSelector = new StateRelatedStaticDistanceFrameSelector();
                    break;
                case REPRODUCTION:
                    frameSelector = new ReconstructiveFrameSelector(Properties.GESTURE_FILES[0]);
                    break;
                case REGRESSION:
                    ArrayList<NGramLog>[] logs = (ArrayList<NGramLog>[])Array.newInstance(ArrayList.class, 4);
                    String[] files = {Properties.GESTURE_FILES[1], Properties.GESTURE_FILES[2], Properties.GESTURE_FILES[3], Properties.GESTURE_FILES[4]};
                    for (int i = 0; i < files.length; i++){
                        logs[i] = new ArrayList<NGramLog>();
                        String[] data = FileHandler.readFile(new File(files[i])).split("\n");
                        for (String s : data){
                            String[] d = s.split(":");
                            NGramLog log = new NGramLog();
                            //trailing ,
                            log.element = d[0].substring(0, d[0].length()-1);
                            log.timeSeeded = Integer.parseInt(d[1]);
                            log.state = Integer.parseInt(d[2]);
                            logs[i].add(log);

                        }
                    }
                    frameSelector = new ClusterPlaybackFrameSelector(Properties.GESTURE_FILES[0], logs);
                    break;
                case RANDOM_SINGLE_TOGGLE:
                    frameSelector = new SingleModelGuidedRandomFrameSelector();
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

        if (frameSelector instanceof ReconstructiveFrameSelector || frameSelector instanceof ClusterPlaybackFrameSelector){
            setGestureHandler((GestureHandler) frameSelector);
        } else {
            RandomGestureHandler rgh = new RandomGestureHandler();
            File f = FileHandler.generateTestingOutputFile("gestures-" + Properties.CURRENT_RUN);
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            rgh.setOutputFile(f);

            setGestureHandler(rgh);
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
