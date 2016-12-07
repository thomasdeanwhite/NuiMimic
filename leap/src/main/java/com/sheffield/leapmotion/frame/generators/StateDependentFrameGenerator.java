package com.sheffield.leapmotion.frame.generators;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.GestureList;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.util.FileHandler;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.frame.analyzer.ProbabilityTracker;
import com.sheffield.leapmotion.frame.generators.gestures.GestureHandler;
import com.sheffield.leapmotion.frame.generators.gestures.NGramGestureHandler;
import com.sheffield.leapmotion.controller.mocks.SeededFrame;
import com.sheffield.leapmotion.output.StateComparator;
import com.sheffield.output.Csv;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class StateDependentFrameGenerator extends FrameGenerator implements GestureHandler {
    @Override
    public Csv getCsv() {
        return new Csv();
    }
	private Random r = new Random();
	
	private static final boolean WRITE_LOGS_TO_FILE = true;

    private long lastGestureChange = 0;
    private final static int GESTURE_CHANGE_TIME = 10000;

	private String currentGesture;

    private HashMap<String, FrameGenerator> frameSelectors;

    private HashMap<String, GestureHandler> gestureHandlers;

    private boolean changeGestures = false;

    public static final String[] STATE_MODELS = {".state.hand_position_data",
    ".state.hand_rotation_data", ".state.joint_position_data", ".state.gesture_data"};


    public File generateFile(String filename){
        return FileHandler.generateTestingOutputFile(filename);
    }

	public StateDependentFrameGenerator() {
		String[] gestures = Properties.INPUT;
        frameSelectors = new HashMap<String, FrameGenerator>();
        gestureHandlers = new HashMap<String, GestureHandler>();

        if (gestures.length > 1){
            changeGestures = true;
            currentGesture = gestures[r.nextInt(gestures.length)];
        } else {
            currentGesture = gestures[0];
        }

        HashMap<String, Integer[]> stateCache = new HashMap<String, Integer[]>();
		for (String s : gestures){
            ArrayList<HashMap<Integer, HashMap<String, Float>>> stateModels = new ArrayList<HashMap<Integer, HashMap<String, Float>>>(STATE_MODELS.length);
            ArrayList<HashMap<Integer, Integer>> totalModels = new ArrayList<HashMap<Integer, Integer>>(STATE_MODELS.length);
            for (int f = 0; f < STATE_MODELS.length; f++) {
                HashMap<Integer, HashMap<String, Float>> states = new HashMap<Integer, HashMap<String, Float>>();
                HashMap<Integer, Integer> totals = new HashMap<Integer, Integer>();
                String sequenceFile = Properties.DIRECTORY + "/" + s + STATE_MODELS[f];
                try {
                    String contents = FileHandler.readFile(new File(sequenceFile));
                    String[] lines = contents.split("\n");
                    for (String line : lines) {
                        line = line.trim();
                        if (line.length() == 0){
                            continue;
                        }
                        String[] stateInfo = line.split(":");
                        Integer[] state = null;
                        if (stateCache.containsKey(stateInfo[0])){
                            state = stateCache.get(stateInfo[0]);
                        } else {
                            String[] stateData = stateInfo[0].split(",");
                            state = new Integer[stateData.length];

                            for (int i = 0; i < state.length; i++) {
                                state[i] = Integer.parseInt(stateData[i]);
                            }
                            stateCache.put(stateInfo[0], state);
                        }
                        String[] handData = null;
                        try {
                            handData = stateInfo[1].split(",");
                        } catch (ArrayIndexOutOfBoundsException e){
                            continue;
                        }


                        HashMap<String, Float> stateProbabilities = new HashMap<String, Float>();


                        int total = 0;

                        int stateNumber = StateComparator.addState(state);

                        for (int i = 0; i < handData.length; i++) {
                            String handString = handData[i].trim();
                            if (handString.equalsIgnoreCase("null")){
                                continue;
                            }
                            if (handString.length() > 0) {
                                String[] hand = handString.split("#");
                                String h = hand[0];
                                int freq = Integer.parseInt(hand[1]);
                                stateProbabilities.put(h, (float) freq);
                                total += freq;
                            }
                        }

                        if (!totals.containsKey(stateNumber)){
                            totals.put(stateNumber, 0);
                        }
                        totals.put(stateNumber, totals.get(stateNumber) + total);


                        for (String st : stateProbabilities.keySet()) {
                            float freq = stateProbabilities.get(st);
                            stateProbabilities.put(st, freq / total);
                        }

                        if (!states.containsKey(stateNumber)){
                            states.put(stateNumber, new HashMap<String, Float>());
                        }
                        states.get(stateNumber).putAll(stateProbabilities);

                    }
                    stateModels.add(f, states);
                    totalModels.add(f, totals);

                } catch (Exception e) {
                    e.printStackTrace(App.out);
                }
            }
            try {
                App.out.println("- Registered " + stateModels.get(0).size() + " states.");
                ProbabilityTracker positionPbt = new ProbabilityTracker(stateModels.get(0), totalModels.get(0));
                ProbabilityTracker rotationPbt = new ProbabilityTracker(stateModels.get(1), totalModels.get(1));
                NGramFrameGenerator ngfs = new NGramFrameGenerator(s);
                long testIndex = Properties.CURRENT_RUN;
                File pFile = generateFile("hand_positions-" + testIndex);
                pFile.createNewFile();
                File rFile = generateFile("hand_rotations-" + testIndex);
                rFile.createNewFile();
                ngfs.setOutputFiles(pFile, rFile);
                ngfs.addPositionProbabilityListener(positionPbt);
                ngfs.addRotationProbabilityListener(rotationPbt);

                ngfs.addProbabilityListener(new ProbabilityTracker(stateModels.get(2), totalModels.get(2)));
                File jFile = generateFile("joint_positions-" + testIndex);
                jFile.createNewFile();
                ngfs.setOutputFile(jFile);
                frameSelectors.put(s, ngfs);
                NGramGestureHandler nggh = new NGramGestureHandler(s);
                
                
                ProbabilityTracker gesturePbt = new ProbabilityTracker(stateModels.get(3), totalModels.get(3));
                nggh.addProbabilityListener(gesturePbt);
                File gFile = generateFile("gestures-" + testIndex);
                gFile.createNewFile();
                nggh.setOutputFile(gFile);
                gestureHandlers.put(s, nggh);
            } catch (Exception e){
                e.printStackTrace(App.out);
            }
		}
	}

	@Override
	public Frame newFrame() {
        long time = System.currentTimeMillis();

        return frameSelectors.get(currentGesture).newFrame();

	}

    @Override
    public String status() {
        return "State: " + StateComparator.getCurrentState();
    }

    public void modifyFrame(SeededFrame frame) {
		frameSelectors.get(currentGesture).modifyFrame(frame);
	}

    @Override
    public boolean allowProcessing() {
        return true;
    }

    @Override
    public GestureList handleFrame(Frame frame) {
        return gestureHandlers.get(currentGesture).handleFrame(frame);
    }

    private long lastUpdate = 0;
    @Override
    public void tick(long time) {
        lastUpdate = time;

        if (time - lastGestureChange > GESTURE_CHANGE_TIME){
            String[] gestures = Properties.INPUT;
            currentGesture = gestures[r.nextInt(gestures.length)];
            lastGestureChange = time;
        }

        if (gestureHandlers.get(currentGesture).lastTick() < time){
            gestureHandlers.get(currentGesture).tick(time);
        }
        if (frameSelectors.get(currentGesture).lastTick() < time){
            frameSelectors.get(currentGesture).tick(time);
        }
    }

    public long lastTick(){
        return lastUpdate;
    }

    @Override
    public void cleanUp() {

    }
}
