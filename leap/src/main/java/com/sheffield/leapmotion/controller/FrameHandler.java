package com.sheffield.leapmotion.controller;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.GestureList;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.frame.generators.*;
import com.sheffield.leapmotion.util.FileHandler;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.util.Tickable;
import com.sheffield.leapmotion.frame.generators.gestures.GestureHandler;
import com.sheffield.leapmotion.frame.generators.gestures.RandomGestureHandler;
import com.sheffield.leapmotion.frame.generators.FrameGenerator;
import com.sheffield.leapmotion.frame.playback.NGramLog;
import com.sheffield.leapmotion.controller.listeners.FrameSwitchListener;
import com.sheffield.leapmotion.controller.mocks.SeededFrame;
import com.sheffield.leapmotion.output.TestingStateComparator;
import com.sheffield.output.Csv;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class FrameHandler implements Tickable {
    private FrameGenerator frameGenerator;
    private ArrayList<FrameSwitchListener> frameSwitchListeners;
    private ArrayList<Frame> frames;
    private GestureHandler gestureHandler;

    private boolean initialised = false;

    private int framesGenerated = 0;

    public String status(){
        return frameGenerator.status();
    }

    public FrameHandler() {

    }

    public void init (Controller seededController){
        if (initialised){
            new Exception("init is being called again!").printStackTrace(App.out);
            return;
        }
        frameSwitchListeners = new ArrayList<FrameSwitchListener>();
        frames = new ArrayList<Frame>();
        try {
            switch (Properties.FRAME_SELECTION_STRATEGY) {
                case RANDOM:
                    frameGenerator = new RandomFrameGenerator();
                    break;
                case SINGLE_MODEL:
                    frameGenerator = new SingleModelFrameGenerator();
                    break;
                case EMPTY:
                    frameGenerator = new EmptyFrameGenerator();
                    break;
                case VQ:
                    frameGenerator = new VectorQuantizedFrameGenerator(Properties.INPUT[0]);
                    break;
                case STATE_ISOLATED:
                    App.out.println("- Redirecting to StateDependent Frame Generation");
                    Properties.FRAME_SELECTION_STRATEGY = Properties
                            .FrameSelectionStrategy.STATE_DEPENDENT;
                case STATE_DEPENDENT:
                    StateIsolatedFrameGenerator
                            sifs = new StateIsolatedFrameGenerator(Properties.INPUT[0]);
                    long testIndex = Properties.CURRENT_RUN;
                    File g = FileHandler.generateTestingOutputFile("gestures-" + testIndex);
                    g.createNewFile();
                    sifs.setGestureOutputFile(g);

                    File p = FileHandler.generateTestingOutputFile("joint_positions-" + testIndex);
                    p.createNewFile();
                    sifs.setOutputFile(p);

                    File hp = FileHandler.generateTestingOutputFile("hand_positions-" + testIndex);
                    hp.createNewFile();

                    File hr = FileHandler.generateTestingOutputFile("hand_rotations-" + testIndex);
                    hr.createNewFile();
                    sifs.setOutputFiles(hp, hr);

                    frameGenerator = sifs;
                    break;
                case RECONSTRUCTION:
                    frameGenerator = new ReconstructiveFrameGenerator(Properties.INPUT[0]);
                    break;
                case REGRESSION:
                    Properties.TESTING_OUTPUT = "testing_regression";
                    ArrayList<NGramLog>[] logs = (ArrayList<NGramLog>[])Array.newInstance(ArrayList.class, 4);
                    String[] files = {Properties.INPUT[1], Properties.INPUT[2], Properties.INPUT[3], Properties.INPUT[4]};

                    HashMap<NGramLog, String> handStateMap = new HashMap
                            <NGramLog, String>();

                    int lastState = -1;
                    HashMap<Integer, ArrayList<Integer>> stateMapping = new
                            HashMap
                            <Integer, ArrayList<Integer>>();

                    for (int i = 0; i < files.length; i++){
                        logs[i] = new ArrayList<NGramLog>();
                        String[] data = FileHandler.readFile(new File(files[i])).split("\n");
                        for (String s : data){
                            s = s.trim();
                            if (s.length() <= 0){
                                continue;
                            }

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

                            handStateMap.put(log, currentState.replace(",",
                                    ";"));

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

                            int newState = TestingStateComparator.addState
                                    (cState);

                            log.state = newState;

                            if (!stateMapping.containsKey(lastState)) {
                                stateMapping.put(lastState, new ArrayList
                                        <Integer>());
                            }
                            if (!stateMapping.get(lastState).contains
                                    (newState)) {
                                stateMapping.get(lastState).add(newState);
                            }
                            logs[i].add(log);

                            lastState = newState;

                        }
                    }
                    frameGenerator = new RegressionFrameGenerator(Properties
                            .INPUT[0], logs, handStateMap, stateMapping);
                    break;
                case MANUAL:
                    frameGenerator = null;
                    return;
                default:
                    frameGenerator = new EmptyFrameGenerator();
                    break;
            }
        } catch (Exception e){
            e.printStackTrace(App.out);
            //TODO: Alert user an error has occurred
            System.exit(-1);
        }

        if (frameGenerator instanceof GestureHandler){ //frameGenerator instanceof ReconstructiveFrameGenerator || frameGenerator instanceof RegressionFrameGenerator){
            setGestureHandler((GestureHandler) frameGenerator);
        } else {
            RandomGestureHandler rgh = new RandomGestureHandler();
            File f = FileHandler.generateTestingOutputFile( "gestures-" +
                    Properties.CURRENT_RUN);
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
            FrameGenerator backupFs = frameGenerator;
            frameGenerator = new UserPlaybackFrameGenerator(backupFs, seededController);
            output = "USER_PLAYBACK_FRAME_SELECTOR(" + output + ")";
        }

        App.out.println("- Using " + output + " for frame selection.");

        initialised = true;
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
        Frame frame = frameGenerator.newFrame();
        if (frame == null){
            return;
        }
        if (frameGenerator instanceof EmptyFrameGenerator){
            final Frame last = getFrame(1);
            final Frame next = frame;
            for (FrameSwitchListener fsl : frameSwitchListeners) {
                final FrameSwitchListener fl = fsl;
                fl.onFrameSwitch(last, next);
            }
            return;
        } else if (!(frameGenerator instanceof UserPlaybackFrameGenerator)) {
            if (!(frame instanceof  SeededFrame)) {
                frame = new SeededFrame(frame);
            }
            SeededFrame sf = (SeededFrame) frame;

            GestureList gl = null;
            frameGenerator.modifyFrame(sf);
            if (gestureHandler == null) {
                gestureHandler = new RandomGestureHandler();
            }
            gl = gestureHandler.handleFrame(frame);
            if (gl == null) {
                gl = frame.gestures();
            }


            sf.setGestures(gl);
        }
        if (frames.contains(frame)){
            return;
        }
        frames.add(0, frame);
        while (frames.size() > Properties.MAX_LOADED_FRAMES) {
            frames.remove(frames.size() - 1);
        }
        final Frame last = getFrame(1);
        final Frame next = frame;
        if (next != null) {

            framesGenerated++;

            for (int i = 0; i < frameSwitchListeners.size(); i++) {
                final FrameSwitchListener fl = frameSwitchListeners.get(i);

                fl.onFrameSwitch(last, next);
            }
        }
    }

    private long lastUpdate = 0;
    @Override
    public void tick(long time) {
        lastUpdate = time;
        if (gestureHandler.lastTick() < time) {
            gestureHandler.tick(time);
        }
        if (frameGenerator.lastTick() < time) {
            frameGenerator.tick(time);
        }
    }

    public long lastTick(){
        return lastUpdate;
    }

    public void cleanUp(){
        frameGenerator.cleanUp();
    }

    public Csv getCsv(){
        return frameGenerator.getCsv();
    }

    public boolean allowProcessing(){
        return framesGenerated % Properties.SEEDED_BEFORE_PROCESSING == 0 &&
        frameGenerator
                .allowProcessing();
    }

    public boolean hasNextFrame() {
        return !initialised || frameGenerator.hasNextFrame();
    }

    public float getProgress() {
        if (!initialised){
            return 0f;
        }
        return frameGenerator.getProgress();
    }
}
