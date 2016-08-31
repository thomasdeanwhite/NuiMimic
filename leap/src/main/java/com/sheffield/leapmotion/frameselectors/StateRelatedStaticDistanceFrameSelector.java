package com.sheffield.leapmotion.frameselectors;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.GestureList;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.FileHandler;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.analyzer.ProbabilityTracker;
import com.sheffield.leapmotion.controller.gestures.GestureHandler;
import com.sheffield.leapmotion.controller.gestures.NGramGestureHandler;
import com.sheffield.leapmotion.framemodifier.FrameModifier;
import com.sheffield.leapmotion.mocks.SeededFrame;
import com.sheffield.leapmotion.output.StateComparator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class StateRelatedStaticDistanceFrameSelector extends FrameSelector implements FrameModifier, GestureHandler {

	private Random r = new Random();
	
	private static final boolean WRITE_LOGS_TO_FILE = true;

    private long lastGestureChange = 0;
    private final static int GESTURE_CHANGE_TIME = 10000;

	private HashMap<String, FrameModifier> frameModifiers;
	private String currentGesture;

    private HashMap<String, FrameSelector> frameSelectors;

    private HashMap<String, GestureHandler> gestureHandlers;

    private boolean changeGestures = false;

    private long lastPositionChange = 0;
    private final int POSITION_LOCATE_TIME = 500;
    private final int POSITION_CHANGE_TIME = 4000;

    public static final String[] STATE_MODELS = {".state.hand_position_data",
    ".state.hand_rotation_data", ".state.joint_position_data", ".state.gesture_data"};


    public File generateFile(String filename){
        return FileHandler.generateTestingOutputFile(filename);
    }

	public StateRelatedStaticDistanceFrameSelector() {
		String[] gestures = Properties.GESTURE_FILES;
        frameModifiers = new HashMap<String, FrameModifier>();
        frameSelectors = new HashMap<String, FrameSelector>();
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
                NGramFrameSelector ngfs = new NGramFrameSelector(s);
                int testIndex = Properties.CURRENT_RUN;
                File pFile = generateFile("hand_positions-" + testIndex);
                pFile.createNewFile();
                File rFile = generateFile("hand_rotations-" + testIndex);
                rFile.createNewFile();
                ngfs.setOutputFiles(pFile, rFile);
                ngfs.addPositionProbabilityListener(positionPbt);
                ngfs.addRotationProbabilityListener(rotationPbt);
                frameModifiers.put(s, ngfs);

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
        if (changeGestures && time - lastGestureChange > GESTURE_CHANGE_TIME){
            String[] gestures = Properties.GESTURE_FILES;
            currentGesture = gestures[r.nextInt(gestures.length)];
            lastGestureChange = System.currentTimeMillis();
        }

        return frameSelectors.get(currentGesture).newFrame();

	}

    @Override
    public String status() {
        return "State: " + StateComparator.getCurrentState();
    }

    public void modifyFrame(SeededFrame frame) {
		frameModifiers.get(currentGesture).modifyFrame(frame);
	}

    @Override
    public GestureList handleFrame(Frame frame) {
        return gestureHandlers.get(currentGesture).handleFrame(frame);
    }
}
