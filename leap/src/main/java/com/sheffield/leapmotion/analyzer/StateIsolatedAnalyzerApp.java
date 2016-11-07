package com.sheffield.leapmotion.analyzer;

import com.sheffield.instrumenter.FileHandler;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.output.StateComparator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class StateIsolatedAnalyzerApp extends AnalyzerApp {

	public static final int PROGRESS_BARS = 20;
	public static final int SEQUENCE_LENGTH = 1000;

	private boolean logBase = false;

	private String contents;
	private HashMap<Integer, DataAnalyzer> dataAnalyzers;
	private AnalyzerApp failsafe;

	private Integer currentState = -1;

	private HashMap<Integer, ArrayList<String>> probabilities;

	public static long framesBeingProcessed = 0;

	public void setLogBase(boolean logBase){
		this.logBase = logBase;
	}

	public DataAnalyzer getDataAnalyzer() {
		if (dataAnalyzers.containsKey(StateComparator.getCurrentState())){
			return dataAnalyzers.get(StateComparator.getCurrentState());
		}
		return failsafe.getDataAnalyzer();
	}

	public StateIsolatedAnalyzerApp(String file, String failsafeFile) {
		//App.out.println("* Loading Sequence Data from " + file);
		try {
			contents = FileHandler.readFile(new File(file));

			failsafe = new AnalyzerApp(failsafeFile);

			probabilities = new HashMap<Integer, ArrayList<String>>();

			String[] ps = contents.split("\n");

			int currentState = -1;

			HashMap<String, Integer[]> stateCache = new HashMap<String, Integer[]>();

			for (int i = 0; i < ps.length; i += 3) {
				if (ps[i].contains("state")){
					Integer[] state = null;

					String stateInfo = ps[i].split(":")[1];
					if (stateCache.containsKey(stateInfo)){
						state = stateCache.get(stateInfo);
					} else {
						String[] stateData = stateInfo.split(",");
						state = new Integer[stateData.length];

						for (int s = 0; s < state.length; s++) {
							state[i] = Integer.parseInt(stateData[i]);
						}
						stateCache.put(stateInfo, state);
					}
				} else {
					if (currentState == -1){
						continue;
					}
					String conts = ps[i].trim() + ":" + ps[i + 1].trim();
					if (!probabilities.containsKey(currentState)){
						probabilities.put(currentState, new ArrayList<String>
								());
					}
					probabilities.get(currentState).add(conts);
				}
				// System.out.print("\r" + conts);
			}

			//App.out.println("\t*! Sequence Loading Done (" + file + ")");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void analyze() {
		failsafe.analyze();
		try {
			for (Integer i : probabilities.keySet()) {
				if (!dataAnalyzers.containsKey(i)) {
					dataAnalyzers.put(i, new ProbabilityDataAnalyzer());
					dataAnalyzers.get(i).analyze(probabilities.get(i), logBase);
				}
			}

		} catch (Exception e){
			e.printStackTrace(App.out);
		}
	}

	public void output() {
		getDataAnalyzer().output("processed-data");
	}

	public void addProbabilityListener(ProbabilityListener pbl){
		getDataAnalyzer().addProbabilityListener(pbl);
	}

}
