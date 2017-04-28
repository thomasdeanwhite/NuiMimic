package com.sheffield.leapmotion.controller;

import com.google.gson.JsonSyntaxException;
import com.leapmotion.leap.*;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.controller.mocks.SeededFinger;
import com.sheffield.leapmotion.controller.mocks.SeededHand;
import com.sheffield.leapmotion.frame.generators.*;
import com.sheffield.leapmotion.frame.util.BezierHelper;
import com.sheffield.leapmotion.util.FileHandler;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.util.ProgressBar;
import com.sheffield.leapmotion.util.Serializer;
import com.sheffield.leapmotion.util.Tickable;
import com.sheffield.leapmotion.frame.generators.gestures.GestureHandler;
import com.sheffield.leapmotion.frame.generators.gestures.RandomGestureHandler;
import com.sheffield.leapmotion.frame.generators.FrameGenerator;
import com.sheffield.leapmotion.frame.playback.NGramLog;
import com.sheffield.leapmotion.controller.listeners.FrameSwitchListener;
import com.sheffield.leapmotion.controller.mocks.SeededFrame;
import com.sheffield.leapmotion.output.TestingStateComparator;
import com.sheffield.output.Csv;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class FrameHandler implements Tickable {
    private FrameGenerator frameGenerator;
    private ArrayList<FrameSwitchListener> frameSwitchListeners;
    private LinkedList<Frame> frames;
    private GestureHandler gestureHandler;

    private boolean initialised = false;

    private long firstFrameTimestamp = Long.MIN_VALUE;

    private FrameSeedingRunnableQueue
            frameSeedingQueue = new FrameSeedingRunnableQueue();

    private int framesGenerated = 0;

    public String status() {
        return frameGenerator == null ? "Initialising" : frameGenerator.status();
    }

    public FrameHandler copy(){
        FrameHandler fh = new FrameHandler();

        //fh.frameGenerator = frameGenerator;
        fh.frameSwitchListeners = new ArrayList<>();

        for (FrameSwitchListener fsl : frameSwitchListeners){
            fh.frameSwitchListeners.add(fsl);
        }

        fh.frames = new LinkedList<Frame>();

        for (Frame f : frames){
            fh.frames.add(f);
        }

        return fh;
    }

    public FrameHandler() {

    }

    public void init(Controller seededController) {
        assert (!initialised);

        String playback = Properties.DIRECTORY + "/" + Properties.INPUT[0] + "/raw_frame_data.bin";

        LineIterator lineIterator = null;
        try {
            lineIterator = FileUtils.lineIterator(new File(playback));
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (lineIterator.hasNext() && SeededFrame.originalFrame == null){
            try {
                Frame f = Serializer
                        .sequenceFromJson(lineIterator.nextLine());


                for (Hand h : f.hands()){
                    if (h.isValid() && h.isRight()){
                        SeededFrame.originalFrame = f;
                        break;
                    }
                }
            } catch (JsonSyntaxException | IllegalArgumentException e){

            }

        }

        lineIterator.close();


        frameSwitchListeners = new ArrayList<FrameSwitchListener>();
        frames = new LinkedList<Frame>();
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
                    frameGenerator = new VectorQuantizedFrameGenerator(
                            Properties.INPUT[0]);
                    break;
                case STATE_ISOLATED:
                    App.out.println(
                            "- Redirecting to StateDependent Frame Generation");
                    Properties.FRAME_SELECTION_STRATEGY = Properties
                            .FrameSelectionStrategy.STATE_DEPENDENT;
                case STATE_DEPENDENT:
                    StateIsolatedFrameGenerator
                            sifs = new StateIsolatedFrameGenerator(
                            Properties.INPUT[0]);
                    long testIndex = Properties.CURRENT_RUN;
                    File g = FileHandler
                            .generateTestingOutputFile("gestures-" + testIndex);
                    g.createNewFile();
                    sifs.setGestureOutputFile(g);

                    File p = FileHandler.generateTestingOutputFile(
                            "joint_positions-" + testIndex);
                    p.createNewFile();
                    sifs.setOutputFile(p);

                    File hp = FileHandler.generateTestingOutputFile(
                            "hand_positions-" + testIndex);
                    hp.createNewFile();

                    File hr = FileHandler.generateTestingOutputFile(
                            "hand_rotations-" + testIndex);
                    hr.createNewFile();
                    sifs.setOutputFiles(hp, hr);

                    frameGenerator = sifs;
                    break;
                case RECONSTRUCTION:
                    frameGenerator = new ReconstructiveFrameGenerator(
                            Properties.INPUT[0]);
                    break;
                case RAW_RECONSTRUCTION:
                    frameGenerator = new RawReconstructiveFrameGenerator(
                            Properties.INPUT[0]);
                    break;

                case REGRESSION:
                    Properties.TESTING_OUTPUT = "testing_regression";
                    ArrayList<NGramLog>[] logs = (ArrayList<NGramLog>[]) Array
                            .newInstance(ArrayList.class, 4);
                    String[] files = {Properties.INPUT[1], Properties.INPUT[2],
                            Properties.INPUT[3], Properties.INPUT[4]};

                    HashMap<NGramLog, String> handStateMap = new HashMap
                            <NGramLog, String>();

                    int lastState = -1;
                    HashMap<Integer, ArrayList<Integer>> stateMapping = new
                            HashMap
                                    <Integer, ArrayList<Integer>>();

                    for (int i = 0; i < files.length; i++) {
                        logs[i] = new ArrayList<NGramLog>();
                        String[] data = FileHandler.readFile(new File(files[i]))
                                .split("\n");
                        for (String s : data) {
                            s = s.trim();
                            if (s.length() <= 0) {
                                continue;
                            }

                            String[] d = s.split(":");
                            NGramLog log = new NGramLog();
                            //trailing ,
                            log.element = d[0];
                            if (d[0].contains(",")) {
                                log.element = log.element
                                        .substring(0, d[0].length() - 1);
                            }
                            log.timeSeeded = Integer.parseInt(d[1]);
                            String currentState = d[2];
                            currentState = currentState
                                    .substring(1, d[2].length() - 1);

                            handStateMap.put(log, currentState.replace(",",
                                    ";"));

                            if (currentState.trim().length() == 0) {
                                continue;
                            }

                            String[] stateArr = currentState.split(",");
                            Integer[] cState = new Integer[stateArr.length];

                            for (int z = 0; z < stateArr.length; z++) {
                                try {
                                    cState[z] = Integer.parseInt(stateArr[z]);
                                } catch (NumberFormatException e) {
                                    App.out.println(
                                            files[i] + ": State[" + z + "/" +
                                                    stateArr.length + "]: " +
                                                    stateArr[z]);
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
                case USER_PLAYBACK:
                    frameGenerator = new UserPlaybackFrameGenerator(new EmptyFrameGenerator(), seededController);
                    break;
                default:
                    frameGenerator = new EmptyFrameGenerator();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace(App.out);
            //TODO: Alert user an error has occurred
            App.getApp().throwableThrown(e);
            System.exit(-1);
        }

        if (frameGenerator instanceof GestureHandler) { //frameGenerator
            // instanceof ReconstructiveFrameGenerator || frameGenerator
            // instanceof RegressionFrameGenerator){ //
            setGestureHandler((GestureHandler) frameGenerator);
        } else {
            RandomGestureHandler rgh = new RandomGestureHandler();
            File f = FileHandler.generateTestingOutputFile("gestures-" +
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

        if (Properties.PLAYBACK_FILE != null && !(frameGenerator instanceof UserPlaybackFrameGenerator)) {
            FrameGenerator backupFs = frameGenerator;
            frameGenerator =
                    new UserPlaybackFrameGenerator(backupFs, seededController);
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

        if (frame == null) {
            frame = Frame.invalid();
        }

        return frame;
    }

    public void loadNewFrame(long time) {

        if (frameGenerator == null || frameSeedingQueue.size() > Properties.MAX_LOADED_FRAMES){
            return;
        }

        Frame frame = frameGenerator.newFrame();
        if (frame == null) {
            return;
        }
        if (frameGenerator instanceof EmptyFrameGenerator) {
            final Frame last = getFrame(1);
            final Frame next = frame;
            for (FrameSwitchListener fsl : frameSwitchListeners) {
                final FrameSwitchListener fl = fsl;
                fl.onFrameSwitch(last, next);
            }
            return;
        } else if (!(frameGenerator instanceof UserPlaybackFrameGenerator)) {
            if (!(frame instanceof SeededFrame)) {
                frame = new SeededFrame(frame);
            }
            SeededFrame sf = (SeededFrame) frame;

            GestureList gl = null;
            frameGenerator.modifyFrame(sf);
            if (gestureHandler == null) {
                gestureHandler = new RandomGestureHandler();
            }


            if (!(frameGenerator instanceof Reconstruction)) {
                sf = (SeededFrame) finalizeFrame(sf, time);
            }


            gl = gestureHandler
                    .handleFrame(frame, SeededController.getController());
            if (gl == null) {
                gl = frame.gestures();
            }


            sf.setGestures(gl);


            frame = sf;

            //sf.interactionBox();
        }
        if (frames.contains(frame)) {
            return;
        }


        if (frame.timestamp() == -1 && frame instanceof SeededFrame){
            ((SeededFrame)frame).setTimestamp(time*1000);
        }

        if (firstFrameTimestamp == Long.MIN_VALUE) {
            firstFrameTimestamp = frame.timestamp();
        }

        frames.add(0, frame);
        while (frames.size() > Properties.MAX_LOADED_FRAMES) {
            frames.remove(frames.size() - 1);
        }
        final Frame last = getFrame(1);
        final Frame next = frame;

        assert (next != null && !next.equals(last));

        framesGenerated++;

        for (int i = 0; i < frameSwitchListeners.size(); i++) {
            FrameSwitchListener fl = frameSwitchListeners.get(i);

//            if (fl.equals(SeededController.CONTROLLER)){
//                fl = ((SeededController)fl).copy();
//            }

            FrameSeedingRunnable r = new FrameSeedingRunnable(fl, next, last,
                    (frame.timestamp() - firstFrameTimestamp)/1000);

            if (Properties.SINGLE_THREAD) {
                frameSeedingQueue.addFrameSeedTask(r);
            } else {
                new Thread(r).start();
            }
        }

    }

    private static long minimumFrame = 0;

    private Frame finalizeFrame(SeededFrame frame, long time) {


        minimumFrame++;

        if (frame.timestamp() == 0) {
            frame.setTimestamp(time);
        }

        long timeStamp = frame.timestamp();
        long timeElapsed = 0;
        int count = 0;

        // get frames seeded in last second:
        //     1000000 -> 1 second in microseconds
        while (timeElapsed < 1000000 && frames.size() > count) {
            if (frames.get(count).isValid() &&
                    frames.get(count).hands().count() > 0) {
                timeElapsed = timeStamp - frames.get(count++).timestamp();
            } else {
                frames.remove(count);
            }
        }

        count -= 2; //take 2 for the comparison betweeen current and main frame

        if (count < 0) {
            return frame;
        }

        FingerList fl = frame.hands().get(0).fingers();

        final int TIP_SAMPLES = 200;
        int increment = count / TIP_SAMPLES;


        // if increment == 0 less than TIP_SAMPLES frames has loaded. We can still use those loaded to calculate a stabilised tip position.
        if (increment == 0){
            increment = 1;
        }

        if (frames.size() <= increment){
            return frame;
        }

        for (Finger f : fl) {
            Frame firstFrame = frames.get(increment);

            Vector firstTip = ((SeededFinger)f).rawTipPosition();
            Vector secondTip = ((SeededFinger)firstFrame.fingers().fingerType(f.type()).get(0))
                    .rawTipPosition();

            Vector tipMovement = firstTip.minus(secondTip);


            ArrayList<Vector> tips = new ArrayList<>();
            tips.add(firstTip);

            Vector stabTipMovement = tipMovement.plus(
                    frames.get(increment).fingers().fingerType(f.type()).get(0).tipVelocity()
            );

            Vector stabilizedTip = firstTip;


            Vector stabilisedThreshold = new Vector(10f, 10f, 10f);
            Vector totalChange = new Vector(Math.abs(tipMovement.getX()),
                            Math.abs(tipMovement.getY()),
                            Math.abs(tipMovement.getZ()));

            int counter = 0;
            for (int i = increment; i < count; i += increment) {

                SeededFinger f1 =
                        (SeededFinger) frames.get(i).fingers().fingerType(f.type()).get(0);

                tips.add(f1.rawTipPosition());

                SeededFinger f2 = (SeededFinger) frames.get(i + increment).fingers().fingerType(f.type())
                        .get(0);

                Vector tipChange = f1.rawTipPosition().minus(f2.rawTipPosition());

                totalChange = totalChange.plus(new Vector(
                        Math.abs(tipChange.getX()),
                        Math.abs(tipChange.getY()),
                        Math.abs(tipChange.getZ())
                ));

                tipMovement = tipMovement
                        .plus(tipChange);

                if (stabilisedThreshold.getX() < totalChange.getX()){
                    stabilizedTip.setX(f1.rawTipPosition().getX());
                    stabilisedThreshold.setX(Float.MAX_VALUE);
                }

                if (stabilisedThreshold.getY() < totalChange.getY()){
                    stabilizedTip.setY(f1.rawTipPosition().getY());
                    stabilisedThreshold.setY(Float.MAX_VALUE);
                }

                if (stabilisedThreshold.getZ() < totalChange.getZ()){
                    stabilizedTip.setZ(f1.rawTipPosition().getZ());
                    stabilisedThreshold.setZ(Float.MAX_VALUE);
                }


                counter++;

            }

            ((SeededFinger) f).setTipVelocity(tipMovement);



//            float mod = 1f;
//            float magSqr = stabTipMovement.magnitudeSquared();
//            float mag = (float) Math.pow(magSqr/2000f, 10);
//            if (mag > 0){
//                mod = Math.min(1f, Math.max(0, (512-mag)/1024));
//            }
//
//            int frameNum = (int)(mod * counter) + 1;
//
//            if (frameNum <= minimumFrame){
//                minimumFrame = frameNum;
//            } else {
//                mod = frameNum / (float)counter;
//            }
//
//            ((SeededFinger) f).setStabilizedTipPosition(BezierHelper.bezier(tips, mod));
            ((SeededFinger) f).setStabilizedTipPosition(stabilizedTip);


            ((SeededFinger) f).normalize();

        }

        Vector palmVelocity = frame.hand(0).palmPosition().minus(frames.get(increment).hand(0).palmPosition());

        for (int i = increment; i < count; i += increment) {
            Hand h1 = frames.get(i).hand(0);
            Hand h2 = frames.get(i + increment).hand(0);

            palmVelocity = palmVelocity.plus(h1.palmPosition().minus(h2.palmPosition()));

        }

        ((SeededHand)frame.hand(0)).setPalmVelocity(palmVelocity);

        return frame;
    }

    private long lastUpdate = 0;

    @Override
    public void tick(long time) {
        lastUpdate = time;

        if (gestureHandler == null || frameGenerator == null){
            return;
        }

        if (gestureHandler.lastTick() < time) {
            gestureHandler.tick(time);
        }


        if (frameGenerator.lastTick() < time) {
            frameGenerator.tick(time);
        }
    }

    public long lastTick() {
        return lastUpdate;
    }

    public void cleanUp() {
        frameGenerator.cleanUp();
    }

    public Csv getCsv() {
        Csv csv = frameGenerator.getCsv();
        Csv newCsv = new Csv();

        newCsv.add("framesGenerated", framesGenerated + "");
        newCsv.add("discardedFrames", frameSeedingQueue.discardedFrames() + "");
        newCsv.merge(csv);
        newCsv.finalize();

        return newCsv;
    }

    public boolean allowProcessing() {
        if (frameGenerator != null) {
            return //framesGenerated % Properties.SEEDED_BEFORE_PROCESSING == 0 &&
                    frameGenerator
                            .allowProcessing();
        }

        return false;
    }

    public boolean hasNextFrame() {
        return !initialised || frameGenerator.hasNextFrame();
    }

    public float getProgress() {
        if (!initialised) {
            return 0f;
        }
        return frameGenerator.getProgress();
    }

    public String getTechnique() {
        return frameGenerator.getName();
    }
}
