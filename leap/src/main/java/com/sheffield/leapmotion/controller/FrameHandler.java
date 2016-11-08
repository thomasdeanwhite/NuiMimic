package com.sheffield.leapmotion.controller;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.GestureList;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.FileHandler;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.Tickable;
import com.sheffield.leapmotion.controller.gestures.GestureHandler;
import com.sheffield.leapmotion.controller.gestures.RandomGestureHandler;
import com.sheffield.leapmotion.framemodifier.FrameModifier;
import com.sheffield.leapmotion.frameselectors.AdaptiveRandomDistanceFrameSelector;
import com.sheffield.leapmotion.frameselectors.EmptyFrameSelector;
import com.sheffield.leapmotion.frameselectors.EuclideanFrameSelector;
import com.sheffield.leapmotion.frameselectors.FrameSelector;
import com.sheffield.leapmotion.frameselectors.NGramLog;
import com.sheffield.leapmotion.frameselectors.RandomDistanceFrameSelector;
import com.sheffield.leapmotion.frameselectors.RandomFrameSelector;
import com.sheffield.leapmotion.frameselectors.RandomTemplateFrameSelector;
import com.sheffield.leapmotion.frameselectors.ReconstructiveFrameSelector;
import com.sheffield.leapmotion.frameselectors.RegressiveFrameSelector;
import com.sheffield.leapmotion.frameselectors.SingleModelGuidedRandomFrameSelector;
import com.sheffield.leapmotion.frameselectors.StateDependentFrameSelector;
import com.sheffield.leapmotion.frameselectors.StateIsolatedFrameSelector;
import com.sheffield.leapmotion.frameselectors.StaticDistanceFrameSelector;
import com.sheffield.leapmotion.frameselectors.UserPlaybackFrameSelector;
import com.sheffield.leapmotion.listeners.FrameSwitchListener;
import com.sheffield.leapmotion.mocks.SeededFrame;
import com.sheffield.leapmotion.output.StateComparator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class FrameHandler implements Tickable {
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
                    frameSelector = new RandomTemplateFrameSelector(Properties.INPUT[0]);
                    break;
                case STATE_DEPENDENT:
                    frameSelector = new StateDependentFrameSelector();
                    break;
                case STATE_ISOLATED:
                    frameSelector = new StateIsolatedFrameSelector(Properties.INPUT[0]);
                    break;
                case REPRODUCTION:
                    frameSelector = new ReconstructiveFrameSelector(Properties.INPUT[0]);
                    break;
                case REGRESSION:
                    ArrayList<NGramLog>[] logs = (ArrayList<NGramLog>[])Array.newInstance(ArrayList.class, 4);
                    String[] files = {Properties.INPUT[1], Properties.INPUT[2], Properties.INPUT[3], Properties.INPUT[4]};
                    for (int i = 0; i < files.length; i++){
                        logs[i] = new ArrayList<NGramLog>();
                        String[] data = FileHandler.readFile(new File(files[i])).split("\n");
                        for (String s : data){
                            String[] d = s.split(":");
                            NGramLog log = new NGramLog();
                            //trailing ,
                            log.element = d[0];
                            if (d[0].contains(",")) {
                                log.element = log.element.substring(0, d[0].length() - 1);
                            }
                            log.timeSeeded = Integer.parseInt(d[1]);
                            String currentState = d[2];
                            currentState = currentState.substring(1, d[2].length()-1);

                            if (currentState.trim().length() == 0){
                                continue;
                            }

                            String[] stateArr = currentState.split(",");
                            Integer[] cState = new Integer[stateArr.length];

                            for (int z = 0; z < stateArr.length; z++){
                                try {
                                    cState[z] = Integer.parseInt(stateArr[z]);
                                } catch (NumberFormatException e){
                                    App.out.println(files[i] + ": State[" + z + "/" + stateArr.length + "]: " + stateArr[z]);
                                }
                            }

                            int newState = StateComparator.addState(cState);

                            log.state = newState;
                            logs[i].add(log);

                        }
                    }
                    frameSelector = new RegressiveFrameSelector(Properties.INPUT[0], logs);
                    break;
                case RANDOM_SINGLE_TOGGLE:
                    frameSelector = new SingleModelGuidedRandomFrameSelector();
                    break;
                case MANUAL:
                    frameSelector = null;
                    return;
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

        if (frameSelector instanceof ReconstructiveFrameSelector || frameSelector instanceof RegressiveFrameSelector){
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

    public synchronized void loadNewFrame() {
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
        } else if (frameSelector instanceof UserPlaybackFrameSelector) {
            UserPlaybackFrameSelector upfs = (UserPlaybackFrameSelector) frameSelector;

            //frame = upfs.newFrame();

            if (upfs.finished()){
                frameSelector = upfs.getBackupFrameSelector();
                loadNewFrame();
                return;
            }
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

    private long lastUpdate = 0;
    @Override
    public void tick(long time) {
        lastUpdate = time;
        if (gestureHandler.lastTick() < time) {
            gestureHandler.tick(time);
        }
        if (frameSelector.lastTick() < time) {
            frameSelector.tick(time);
        }
        for (FrameModifier fm : frameModifiers){
            if (fm.lastTick() < time) {
                fm.tick(time);
            }
        }
    }

    public long lastTick(){
        return lastUpdate;
    }

    public void cleanUp(){
        frameSelector.cleanUp();
    }
}
