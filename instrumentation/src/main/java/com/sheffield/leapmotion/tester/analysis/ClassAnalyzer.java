package com.sheffield.leapmotion.tester.analysis;

import com.sheffield.leapmotion.sampler.FileHandler;
import com.sheffield.leapmotion.tester.Properties;
import com.sheffield.leapmotion.tester.listeners.StateChangeListener;
import com.sheffield.leapmotion.tester.states.EuclideanStateRecognizer;
import com.sheffield.leapmotion.tester.states.StateRecognizer;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ClassAnalyzer {

	private static ArrayList<String> branchesToCover;

	public static final int FRAMES_FOR_STATE_CHECK = (int) Properties.SWITCH_RATE;

	private static int framesForNextCheck = FRAMES_FOR_STATE_CHECK;

	public static final float DISTANCE_ADDITION = 50f;

	private static ArrayList<String> methodsExecuted;

	private static ArrayList<String> methodsTotal;

	private static ArrayList<String> methodsExecutedThisRun;

	private static ArrayList<String> branchesExecuted;

	private static ArrayList<String> distancesWaiting;

	private static ArrayList<String> branchesTotal;

	private static ArrayList<String> branchesExecutedThisRun;

	private static ArrayList<String> branchesDistance;

	private static HashMap<String, BranchType> branchTypes;

	private static HashMap<String, Integer> callFrequencies;

	private static HashMap<String, Float> branchDistance;
	private static int currentBranchDistance = 0;

	private static HashMap<String, Integer> branchesCalled;

	public static HashMap<String, Integer> lastBranchesCalled;

	private static StateRecognizer stateRecognizer;

	private static int currentState = 0;

	private static ArrayList<StateChangeListener> stateChangeListeners;
	public static boolean frameStateNew = false;

	static {
		branchesToCover = new ArrayList<String>();

		methodsExecuted = new ArrayList<String>();
		methodsTotal = new ArrayList<String>();
		methodsExecutedThisRun = new ArrayList<String>();

		branchesExecuted = new ArrayList<String>();
		branchesTotal = new ArrayList<String>();
		branchesExecutedThisRun = new ArrayList<String>();
		branchesDistance = new ArrayList<String>();

		branchTypes = new HashMap<String, BranchType>();
		branchDistance = new HashMap<String, Float>();
		distancesWaiting = new ArrayList<String>();

		callFrequencies = new HashMap<String, Integer>();

		branchesCalled = new HashMap<String, Integer>();
		lastBranchesCalled = new HashMap<String, Integer>();

		stateRecognizer = new EuclideanStateRecognizer();

		stateChangeListeners = new ArrayList<StateChangeListener>();
	}

	public static void addBranchToCover(String s) {
		branchesToCover.add(s);
	}

	public static void addStateChangeListener(StateChangeListener scl) {
		stateChangeListeners.add(scl);
	}

	public static String getNextBranchDistanceOrdered() {

		if (branchesToCover.size() > 0) {
			for (String s : branchesToCover) {
				// Do we have a branch that hasn't been covered?
				if (distancesWaiting.contains(s)) {
					return s;
				}
			}
		}
		if (branchesDistance.isEmpty()) {
			return null;
		}
		if (currentBranchDistance >= branchesDistance.size()) {
			currentBranchDistance = 0;
		}

		String newBranch = null;

		while (newBranch == null && currentBranchDistance < branchesDistance.size()) {
			newBranch = branchesDistance.get(currentBranchDistance++);
			if (branchesExecutedThisRun.contains(newBranch)) {
				newBranch = null;
			}
		}

		if (newBranch == null) {
			for (String branch : branchesDistance) {

				if (!branchesExecutedThisRun.contains(branchesDistance)) {
					currentBranchDistance = branchesDistance.indexOf(branch);

					return branch;
				}

			}
			// All branch distances have been covered.
			return null;
		}

		return newBranch;
	}

	@SuppressWarnings("unchecked")
	public static String getNextBranchDistanceCallFrequency() {
		if (branchesToCover.size() > 0) {
			for (String s : branchesToCover) {
				// Do we have a branch that hasn't been covered?
				if (!branchesCalled.containsKey(s)) {
					return s;
				}
			}
		}
		if (branchesDistance.isEmpty()) {
			return null;
		}

		Set s = callFrequencies.keySet();

		String[] branches = new String[s.size()];

		s.toArray(branches);

		Arrays.sort(branches, new Comparator() {

			@Override
			public int compare(Object o1, Object o2) {
				String first = (String) o1;
				String second = (String) o2;
				return callFrequencies.get(second) - callFrequencies.get(first);
			}

		});

		String newBranch = null;
		int index = 0;
		while (newBranch == null && index < branchesDistance.size()) {
			newBranch = branchesDistance.get(index++);
			if (branchesExecutedThisRun.contains(newBranch)) {
				newBranch = null;
			}
		}

		if (newBranch == null) {
			for (String branch : branchesDistance) {

				if (!branchesExecutedThisRun.contains(branchesDistance)) {
					currentBranchDistance = branchesDistance.indexOf(branch);

					return branch;
				}

			}
			// All branch distances have been covered.
			return null;
		}

		return newBranch;
	}

	public static void methodFound(String method) {
		methodsTotal.add(method);
	}

	public static void branchFound(String branch) {
		branchesTotal.add(branch);
	}

	public static BranchType getBranchType(String branch) {
		return branchTypes.get(branch);
	}

	public static void branchDistanceFound(String branch, BranchType type) {
		if (!branchTypes.containsKey(branch)) {
			branchTypes.put(branch, type);
		}
		if (!branchesDistance.contains(branch)) {
			branchesDistance.add(branch);
		}

	}

	public static synchronized void newFrame() {
		if (framesForNextCheck <= 0 && !stateRecognizer.isProcessing()) {
			frameStateNew = true;
			lastBranchesCalled = branchesCalled;
			branchesCalled = new HashMap<String, Integer>();

			int newState = stateRecognizer.recognizeState();

			if (newState != currentState) {
				for (StateChangeListener scl : stateChangeListeners) {
					scl.onStateChange(currentState, newState);
				}
				currentState = newState;
			}

			distancesWaiting.clear();

			lastBranchesCalled.clear();

			framesForNextCheck = FRAMES_FOR_STATE_CHECK + 1;
		} else {
			frameStateNew = false;
		}

		framesForNextCheck--;
	}

	public static void newRun() {
		branchesExecutedThisRun.clear();
		methodsExecutedThisRun.clear();
	}

	public static void methodExecuted(String method) {
		if (!methodsExecuted.contains(method)) {
			methodsExecuted.add(method);
		}
		if (!methodsExecutedThisRun.contains(method)) {
			methodsExecutedThisRun.add(method);
		}
	}

	public static synchronized void branchExecuted(String branch) {
		if (!branchesExecuted.contains(branch)) {
			branchesExecuted.add(branch);
		}
		if (!branchesExecutedThisRun.contains(branch)) {
			branchesExecutedThisRun.add(branch);
		}
		if (!branchesCalled.containsKey(branch)) {
			branchesCalled.put(branch, 1);
		} else {
			int i = branchesCalled.get(branch) + 1;
			branchesCalled.put(branch, i);
		}

		if (!lastBranchesCalled.containsKey(branch)) {
			lastBranchesCalled.put(branch, 1);
		} else {
			int i = lastBranchesCalled.get(branch) + 1;
			lastBranchesCalled.put(branch, i);
		}
	}

	public static void branchExecutedDistance(int i, int j, String branch) {
		if (!distancesWaiting.contains(branch)) {
			distancesWaiting.add(branch);
		}
		calculateBranchDistance(branch, i, j);
		// if (!branch.startsWith("org.jbox") &&
		// !branchesCalled.containsKey(branch))
		// App.out.println(branch + " int");
	}

	public static void branchExecutedDistance(float i, float j, String branch) {
		if (!distancesWaiting.contains(branch)) {
			distancesWaiting.add(branch);
		}
		calculateBranchDistance(branch, i, j);
		// if (branch.startsWith("org/jbox2d/collision/AABB") &&
		// !branchesCalled.containsKey(branch)) {
		// App.out.println(branch + " float " + getBranchType(branch));
		// }
	}

	public static void branchExecutedDistance(double i, double j, String branch) {
		if (!distancesWaiting.contains(branch)) {
			distancesWaiting.add(branch);
		}
		calculateBranchDistance(branch, (float) i, (float) j);

	}

	public static void branchExecutedDistance(long i, long j, String branch) {
		if (!distancesWaiting.contains(branch)) {
			distancesWaiting.add(branch);
		}
		calculateBranchDistance(branch, i, j);
	}

	public static void branchExecutedDistance(short i, short j, String branch) {
		if (!distancesWaiting.contains(branch)) {
			distancesWaiting.add(branch);
		}
		calculateBranchDistance(branch, i, j);

	}

	public static double methodCoverage() {
		return (double) methodsExecutedThisRun.size() / (double) methodsTotal.size();
	}

	public static double branchCoverage() {
		return (double) branchesExecutedThisRun.size() / (double) branchesTotal.size();
	}

	public static double calculateBranchDistance(String branch, float b1, float b2) {
		// if (callFrequencies.containsKey(branch)) {
		// callFrequencies.put(branch, callFrequencies.get(branch).intValue() +
		// 1);
		// } else {
		// callFrequencies.put(branch, 1);
		// }

		b1 += DISTANCE_ADDITION;
		b2 += DISTANCE_ADDITION;

		BranchType bt = branchTypes.get(branch);

		if (bt == null) {
			return 1d;
		}

		float bd = 0;

		switch (bt) {
		case BRANCH_E:
			bd = Math.abs(b1 - b2);
			break;
		case BRANCH_GE:
			bd = b1 - b2;
			break;
		case BRANCH_GT:
			bd = b1 - b2;
			break;
		case BRANCH_LE:
			bd = b2 - b1;
			break;
		case BRANCH_LT:
			bd = b2 - b1;
			break;
		}
		bd = Math.abs(bd / Float.MAX_VALUE);
		// bd = 1f - Math.min(1f, Math.max(0f, bd));
		bd = Math.min(1f, Math.max(0f, bd));
		bd = (float) Math.pow(bd, 0.005);
		branchDistance.put(branch, bd);

		return bd;

	}

	/**
	 * Returns distance between branch. 0 is a satisfied branch, 1 is as far
	 * away as possible.
	 *
	 * @param branch
	 * @return
	 */
	public static double getBranchDistance(String branch) {
		if (branchesCalled.containsKey(branch)) {
			return 0f;
		}
		if (!branchDistance.containsKey(branch)) {
			return 1;
		}
		return branchDistance.get(branch);
	}

	public static synchronized double averageBranchDistance() {
		// TODO: If branchesToCover.size() > 0, only use branches to cover in
		// average
		float branchDistance = 0f;
		int i = 0;
		String[] keys = new String[branchesCalled.keySet().size()];
		branchesCalled.keySet().toArray(keys);
		for (i = 0; i < keys.length; i++) {
			branchDistance += getBranchDistance(keys[i]);
		}
		i++;
		branchDistance /= i;
		return branchDistance;
	}

	public static String getReport() {
		double bCoverage = (double) branchesExecutedThisRun.size() / (double) branchesTotal.size();
		return "\t@ Branches Discovered: " + branchesTotal.size() + "\n\t@ Branches Covered: "
				+ branchesExecutedThisRun.size() + "\n\t@ Branch Coverage: "
				+ bCoverage;

	}

	public static String toCsv(boolean headers) {
		double bCoverage = (double) branchesExecutedThisRun.size() / (double) branchesTotal.size();
		String csv = "";

		if (headers){
			csv += "frame_selector,branches,covered_branches,branch_coverage,runtime,clusters,ngram\n";
		}
		String clusters = Properties.NGRAM_TYPE.substring(0, Properties.NGRAM_TYPE.indexOf("-"));
		String ngram = Properties.NGRAM_TYPE.substring(Properties.NGRAM_TYPE.indexOf("-")+1);
		csv += Properties.FRAME_SELECTION_STRATEGY + "," + branchesTotal.size() + "," + branchesExecutedThisRun.size() + ","
				+ bCoverage +"," + Properties.RUNTIME + "," + clusters + "," + ngram + "\n";
		return csv;

	}

	public static void output(String file){
		String branches = "";

		for (String b : branchesTotal){
			branches += b + ",";
		}
		branches = branches.substring(0, branches.length()-1);

		try {
			FileHandler.writeToFile(new File(file), branches);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
