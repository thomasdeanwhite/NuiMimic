package com.sheffield.leapmotion.frame.analyzer;

import com.sheffield.instrumenter.FileHandler;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.output.StateComparator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class StateIsolatedAnalyzerApp extends AnalyzerApp {
	private boolean logBase = false;

	private String contents;
	private HashMap<Integer, DataAnalyzer> dataAnalyzers;
	private AnalyzerApp failsafe;

	public static int HITS = 0;
	public static int MISS = 0;

	public static float hitRatio(){
		if (HITS + MISS == 0){
			return 1f;
		}
		return (HITS / (float)(HITS + MISS));
	}

	private Integer currentState = -1;

	private HashMap<Integer, ArrayList<String>> probabilities;

	public static long framesBeingProcessed = 0;

	public void setLogBase(boolean logBase){
		this.logBase = logBase;
	}

	private String currentKey = null;

	private DataAnalyzer lastDa = null;

	public DataAnalyzer getDataAnalyzer() {

		if (lastDa != null){
			currentKey = lastDa.getCurrentKey();
		}

		if (dataAnalyzers.containsKey(StateComparator.getCurrentState())){
			DataAnalyzer da = dataAnalyzers.get(StateComparator.getCurrentState());
			if (currentKey == null || da.hasNext(currentKey)) {
				HITS++;
				lastDa = da;
				return da;
			}
		}
		MISS++;
		lastDa = failsafe.getDataAnalyzer();
		return failsafe.getDataAnalyzer();
	}

	public StateIsolatedAnalyzerApp(String file, String failsafeFile) {
		//App.out.println("* Loading Sequence Data from " + file);

		dataAnalyzers = new HashMap<Integer, DataAnalyzer>();
		try {
			contents = FileHandler.readFile(new File(file));

			failsafe = new AnalyzerApp(failsafeFile);

			probabilities = new HashMap<Integer, ArrayList<String>>();

			String[] ps = contents.split("\n");

			int currentState = -1;

			HashMap<String, Integer[]> stateCache = new HashMap<String, Integer[]>();

			for (int i = 0; i < ps.length-1; i ++) {
				if (ps[i].startsWith("state:")){
					Integer[] state = null;

					String stateInfo = ps[i].split(":")[1];
					if (stateCache.containsKey(stateInfo)){
						state = stateCache.get(stateInfo);
					} else {
						String[] stateData = stateInfo.split(",");
						state = new Integer[stateData.length];

						for (int s = 0; s < stateData.length; s++) {
							state[s] = Integer.parseInt(stateData[s].trim());
						}
						stateCache.put(stateInfo, state);
					}

					currentState = StateComparator.addState(state);


				} else if (ps[i].contains("cluster:")) {
					if (currentState == -1){
						continue;
					}
                    ps[i] = ps[i].split(":")[1].trim();
                    while(i < ps.length-1 && !ps[i].contains("state:")) {
						if (ps[i].trim().length() > 0 && !ps[i].contains("|")) {
                            String conts = ps[i].trim() + ":" + ps[i + 1].trim();
                            if (!probabilities.containsKey(currentState)) {
                                probabilities.put(currentState, new ArrayList<String>
                                        ());
                            }
                            probabilities.get(currentState).add(conts);
                        }
						i++;
					}
                    i--;
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
