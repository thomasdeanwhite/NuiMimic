package com.sheffield.instrumenter.analysis;

import com.sheffield.instrumenter.Properties;
import com.sheffield.instrumenter.instrumentation.LoggingUncaughtExceptionHandler;
import com.sheffield.instrumenter.listeners.StateChangeListener;
import com.sheffield.instrumenter.states.EuclideanStateRecognizer;
import com.sheffield.instrumenter.states.StateRecognizer;
import com.sheffield.leapmotion.sampler.FileHandler;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClassAnalyzer {

	private static ArrayList<ThrowableListener> throwableListeners;

	private static ArrayList<String> branchesToCover;

	public static PrintStream out = System.out;

	private static ArrayList<String> branchesExecuted;

	private static ArrayList<String> branchesPositiveExecuted;

	private static ArrayList<String> branchesNegativeExecuted;

	private static ArrayList<String> distancesWaiting;

	private static ArrayList<String> branchesTotal;

	private static ArrayList<String> branchesDistance;

	private static HashMap<String, BranchType> branchTypes;

	private static HashMap<String, Integer> callFrequencies;

	private static HashMap<String, Float> branchDistance;

	private static StateRecognizer stateRecognizer;

	private static final float BRANCH_DISTANCE_ADDITION = 50f;

	private static ArrayList<StateChangeListener> stateChangeListeners;

	static {
		Thread.currentThread().setUncaughtExceptionHandler(new LoggingUncaughtExceptionHandler());
		throwableListeners = new ArrayList<ThrowableListener>();

		branchesToCover = new ArrayList<String>();

		branchesExecuted = new ArrayList<String>();
		branchesPositiveExecuted = new ArrayList<String>();
		branchesNegativeExecuted = new ArrayList<String>();
		branchesTotal = new ArrayList<String>();

		branchesDistance = new ArrayList<String>();

		branchTypes = new HashMap<String, BranchType>();
		branchDistance = new HashMap<String, Float>();
		distancesWaiting = new ArrayList<String>();

		callFrequencies = new HashMap<String, Integer>();

		stateRecognizer = new EuclideanStateRecognizer();

		stateChangeListeners = new ArrayList<StateChangeListener>();
	}

	public static void addThrowableListener(ThrowableListener tl){
		throwableListeners.add(tl);
	}

	public static void throwableThrown(Throwable throwable){
		for (ThrowableListener t :throwableListeners){
			t.throwableThrown(throwable);
		}
	}

	public static void setOut(PrintStream stream) {
		out = stream;
	}

	public static void addBranchToCover(String s) {
		branchesToCover.add(s);
	}

	public static void addStateChangeListener(StateChangeListener scl) {
		stateChangeListeners.add(scl);
	}

	public static void branchFound(String branch) {
		if (!branchesTotal.contains(branch)) {
			branchesTotal.add(branch);
		}
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

	public static synchronized void branchExecuted(boolean hit, String branch) {

		if (hit && !branchesPositiveExecuted.contains(branch)) {
			branchesPositiveExecuted.add(branch);
		}

		if (!hit && !branchesNegativeExecuted.contains(branch)) {
			branchesNegativeExecuted.add(branch);
		}

		if (!branchesExecuted.contains(branch)) {
			branchesExecuted.add(branch);
		}
	}

	public static void branchExecutedDistance(int i, int j, String branch) {
		if (!distancesWaiting.contains(branch)) {
			distancesWaiting.add(branch);
		}
		calculateBranchDistance(branch, i, j);
	}

	public static void branchExecutedDistance(float i, float j, String branch) {
		if (!distancesWaiting.contains(branch)) {
			distancesWaiting.add(branch);
		}
		calculateBranchDistance(branch, i, j);
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

	public static double branchCoverage() {
		return (branchesPositiveExecuted.size() + branchesNegativeExecuted.size())
				/ (2 * (double) branchesTotal.size());
	}

	public static double calculateBranchDistance(String branch, float b1, float b2) {

		b1 += BRANCH_DISTANCE_ADDITION;
		b2 += BRANCH_DISTANCE_ADDITION;

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
		bd = Math.min(1f, Math.max(0f, bd));
		bd = (float) Math.pow(bd, 0.005);
		branchDistance.put(branch, bd);

		return bd;

	}

	/**
	 * Returns distance between negative and positive branch hit. 0 is a positive hit, 1 is as far away from positive as possible.
	 *
	 * @param branch
	 * @return
	 */
	public static double getBranchDistance(String branch) {
		if (branchesExecuted.contains(branch)) {
			return 0f;
		}
		if (!branchDistance.containsKey(branch)) {
			return 1;
		}
		return branchDistance.get(branch);
	}

	public static List<String> getBranchesExecuted(String className) {
		List<String> branchesExecutedInClass = new ArrayList<String>();
		for (String b : branchesExecuted) {
			if (b.startsWith(className)) {
				branchesExecutedInClass.add(b.substring(className.length() + 1));
			}
		}
		return branchesExecutedInClass;

	}

	public static synchronized List<String> getBranchesExecuted() {
		return branchesExecuted;
	}

	public static String getReport() {
		double bCoverage = branchCoverage();
		return "\t@ Branches Discovered: " + branchesTotal.size() + "\n\t@ Branches Covered: "
				+ branchesExecuted.size() + "\n\t@ Branch Coverage: "
				+ bCoverage;

	}

	public static String toCsv(boolean headers) {
		double bCoverage = branchCoverage();
		String csv = "";

		if (headers) {
			csv += "frame_selector,branches,covered_branches,branch_coverage,runtime,clusters,ngram,positive_hits,negative_hits\n";
		}
		String clusters = Properties.NGRAM_TYPE.substring(0, Properties.NGRAM_TYPE.indexOf("-"));
		String ngram = Properties.NGRAM_TYPE.substring(Properties.NGRAM_TYPE.indexOf("-") + 1);
		csv += Properties.FRAME_SELECTION_STRATEGY + "," + branchesTotal.size() + "," + branchesPositiveExecuted.size()
				+ "," + bCoverage + "," + Properties.RUNTIME + "," + clusters + "," + ngram + ","
				+ branchesPositiveExecuted.size() + "," + branchesNegativeExecuted.size() + "\n";
		return csv;

	}

	public static void output(String file) {

		int counter = 0;
		int bars = 50;
		StringBuilder sb = new StringBuilder("");
		for (String b : branchesTotal) {
			sb.append(b);
			sb.append(",");
			String progress = "[";
			float percent = counter / (float) branchesTotal.size();
			int b1 = (int) (percent * bars);
			for (int i = 0; i < b1; i++) {
				progress += "-";
			}
			progress += ">";
			int b2 = bars - b1;
			for (int i = 0; i < b2; i++) {
				progress += " ";
			}
			progress += "] " + (int) (percent * 100) + "% [" + counter + " of " + branchesTotal.size() + "]";
			out.print("\r" + progress);
			counter++;
		}
		String branches = sb.substring(0, sb.length() - 1);

		try {
			FileHandler.writeToFile(new File(file), branches);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
