package com.sheffield.instrumenter.analysis;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sheffield.instrumenter.Properties;
import com.sheffield.instrumenter.Properties.InstrumentationApproach;
import com.sheffield.instrumenter.analysis.task.AbstractTask;
import com.sheffield.instrumenter.analysis.task.TaskTimer;
import com.sheffield.instrumenter.instrumentation.ClassStore;
import com.sheffield.instrumenter.instrumentation.LoggingUncaughtExceptionHandler;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.Branch;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.BranchHit;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.Line;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.LineHit;
import com.sheffield.instrumenter.instrumentation.visitors.ArrayClassVisitor;
import com.sheffield.instrumenter.listeners.StateChangeListener;
import com.sheffield.instrumenter.states.EuclideanStateRecognizer;
import com.sheffield.instrumenter.states.StateRecognizer;
import com.sheffield.leapmotion.sampler.FileHandler;

public class ClassAnalyzer {

	private static ArrayList<ThrowableListener> throwableListeners;

	private static ArrayList<String> branchesToCover;

	public static PrintStream out = System.out;

	private static Map<String, Map<Integer, LineHit>> lines;

	private static Map<Integer, BranchHit> branches;

	private static ArrayList<String> distancesWaiting;

	private static ArrayList<String> branchesDistance;

	private static HashMap<String, BranchType> branchTypes;

	private static HashMap<String, Integer> callFrequencies;

	private static HashMap<String, Float> branchDistance;

	private static StateRecognizer stateRecognizer;

	private static final float BRANCH_DISTANCE_ADDITION = 50f;

	private static ArrayList<Class<?>> changedClasses;

	private static ArrayList<StateChangeListener> stateChangeListeners;

	static {
		Thread.currentThread().setUncaughtExceptionHandler(new LoggingUncaughtExceptionHandler());
		throwableListeners = new ArrayList<ThrowableListener>();

		branchesToCover = new ArrayList<String>();

		branches = new HashMap<Integer, BranchHit>();

		branchesDistance = new ArrayList<String>();

		branchTypes = new HashMap<String, BranchType>();
		branchDistance = new HashMap<String, Float>();
		lines = new HashMap<String, Map<Integer, LineHit>>();
		distancesWaiting = new ArrayList<String>();

		callFrequencies = new HashMap<String, Integer>();

		stateRecognizer = new EuclideanStateRecognizer();

		stateChangeListeners = new ArrayList<StateChangeListener>();

		changedClasses = new ArrayList<Class<?>>();
	}

	public static void reset() {
		branches.clear();
		lines.clear();
		changedClasses.clear();
	}

	public static void resetCoverage() {
		for (BranchHit b : branches.values()) {
			b.reset();
		}

		for (String className : lines.keySet()) {
			if (lines.containsKey(className)) {
				for (LineHit lh : lines.get(className).values()) {
					lh.reset();
				}
			}
		}
		if (Properties.INSTRUMENTATION_APPROACH == InstrumentationApproach.ARRAY && Properties.USE_CHANGED_FLAG) {
			for (Class<?> cl : changedClasses) {
				try {
					Field changed = cl.getDeclaredField("__changed");
					changed.setAccessible(true);
					changed.set(cl, false);
				} catch (Exception e) {
					e.printStackTrace(out);
				}
			}
		}
		changedClasses.clear();
	}

	public static void addThrowableListener(ThrowableListener tl) {
		throwableListeners.add(tl);
	}

	public static void throwableThrown(Throwable throwable) {
		for (ThrowableListener t : throwableListeners) {
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

	private static int branchId = 0;

	private static int getNewBranchId() {
		return ++branchId;
	}

	public static int branchFound(String className, int lineNumber) {
		int branchId = getNewBranchId();
		branches.put(branchId, new BranchHit(new Branch(className, lineNumber), 0, 0));
		return branchId;
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

	public static synchronized void branchExecuted(boolean hit, int branchId) {
		BranchHit bh = branches.get(branchId);
		if (bh != null) {
			if (hit) {
				bh.getBranch().trueHit(1);
			} else {
				bh.getBranch().falseHit(1);
			}
			Class<?> cl = ClassStore.get(bh.getBranch().getClassName());
			if (!changedClasses.contains(cl)) {
				changedClasses.add(cl);
			}
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
		int branchesTotal = 0;
		int branchesExecuted = 0;
		for (BranchHit b : branches.values()) {
			if (b.getBranch().getFalseHits() > 0) {
				branchesExecuted++;
			}
			if (b.getBranch().getTrueHits() > 0) {
				branchesExecuted++;
			}
			branchesTotal += 2;
		}
		return branchesExecuted / (double) branchesTotal;
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
		if (!branchDistance.containsKey(branch)) {
			return 1;
		}
		return branchDistance.get(branch);
	}

	public static List<Branch> getBranchesExecuted(String className) {
		List<Branch> branchesHit = new ArrayList<Branch>();
		for (BranchHit bh : branches.values()) {
			if (bh.getBranch().getTrueHits() > 0) {
				branchesHit.add(bh.getBranch());
			}
		}
		return branchesHit;
	}

	public static synchronized List<BranchHit> getBranchesExecuted() {
		List<BranchHit> branchesHit = new ArrayList<BranchHit>();
		for (BranchHit b : branches.values()) {
			if (b.getBranch().getTrueHits() > 0) {
				branchesHit.add(b);
			}
		}
		return branchesHit;
	}

	public static synchronized List<BranchHit> getBranchesNotExecuted() {
		List<BranchHit> branchesHit = new ArrayList<BranchHit>();
		for (BranchHit b : branches.values()) {
			if (b.getBranch().getFalseHits() > 0) {
				branchesHit.add(b);
			}
		}
		return branchesHit;
	}

	public static synchronized List<BranchHit> getAllBranches() {
		List<BranchHit> branchesHit = new ArrayList<BranchHit>();
		for (BranchHit b : branches.values()) {
			branchesHit.add(b);
		}
		return branchesHit;
	}

	public static void lineFound(String className, int lineNumber) {
		if (!lines.containsKey(className)) {
			lines.put(className, new HashMap<Integer, LineHit>());
		}
		lines.get(className).put(lineNumber, new LineHit(new Line(className, lineNumber), -1));
	}

	public static void lineExecuted(String className, int lineNumber) {
		if (!lines.containsKey(className)) {
			lines.put(className, new HashMap<Integer, LineHit>());
		}
		LineHit lh = findOrCreateLine(className, lineNumber);
		lh.getLine().hit(1);
		Class<?> cl = ClassStore.get(lh.getLine().getClassName());
		if (!changedClasses.contains(cl)) {
			changedClasses.add(cl);
		}
	}

	private static LineHit findOrCreateLine(String className, int lineNumber) {
		if (lines.get(className).containsKey(lineNumber)) {
			return lines.get(className).get(lineNumber);
		}
		LineHit lh = new LineHit(new Line(className, lineNumber), -1);
		lines.get(className).put(lineNumber, lh);
		return lh;
	}

	public static double lineCoverage() {
		int totalLines = 0;
		int coveredLines = 0;
		String className = "";
		for (Iterator<String> it = lines.keySet().iterator(); it.hasNext();) {
			className = it.next();
			for (LineHit lh : lines.get(className).values()) {
				if (lh.getLine().getHits() > 0) {
					coveredLines++;
				}
				totalLines++;
			}

		}
		return coveredLines / (double) totalLines;
	}

	public static String getReport() {
		double bCoverage = branchCoverage();
		return "\t@ Branches Discovered: " + getAllBranches().size() + "\n\t@ Branches Covered: "
				+ getBranchesExecuted().size() + "\n\t@ Branch Coverage: " + bCoverage;

	}

	public static String toCsv(boolean headers) {
		double bCoverage = branchCoverage();
		String csv = "";

		if (headers) {
			csv += "frame_selector,branches,covered_branches,branch_coverage,runtime,clusters,ngram,positive_hits,negative_hits,gesture_file,lines_found,lines_covered,line_coverage\n";
		}
		String clusters = Properties.NGRAM_TYPE.substring(0, Properties.NGRAM_TYPE.indexOf("-"));
		String ngram = Properties.NGRAM_TYPE.substring(Properties.NGRAM_TYPE.indexOf("-") + 1);

		String gestureFiles = "";

		for (String s : Properties.GESTURE_FILES) {
			gestureFiles += s + "/";
		}
		int totalLines = 0;
		int coveredLines = 0;
		for (String s : lines.keySet()) {
			Map<Integer, LineHit> lh = lines.get(s);
			totalLines += lh.size();
			for (int i : lh.keySet()) {
				if (lh.get(i).getLine().getHits() > 0) {
					coveredLines++;
				}
			}
		}

		csv += Properties.FRAME_SELECTION_STRATEGY + "," + getAllBranches().size() + "," + getBranchesExecuted().size()
				+ "," + bCoverage + "," + Properties.RUNTIME + "," + clusters + "," + ngram + ","
				+ getBranchesExecuted().size() + "," + getBranchesNotExecuted().size() + "," + gestureFiles + ","
				+ totalLines + "," + coveredLines + "," + (float) coveredLines / (float) totalLines + "\n";
		return csv;

	}

	public static void output(String file, String file2) {

		int counter = 0;
		int bars = 50;
		StringBuilder sb = new StringBuilder("");
		int totalBranches = getAllBranches().size();
		for (BranchHit b : branches.values()) {
			sb.append(b.getBranch().getClassName());
			sb.append("#");
			sb.append(b.getBranch().getLineNumber());
			sb.append(",");
			String progress = "[";
			float percent = counter / (float) totalBranches;
			int b1 = (int) (percent * bars);
			for (int i = 0; i < b1; i++) {
				progress += "-";
			}
			progress += ">";
			int b2 = bars - b1;
			for (int i = 0; i < b2; i++) {
				progress += " ";
			}
			progress += "] " + (int) (percent * 100) + "% [" + counter + " of " + totalBranches + "]";
			out.print("\r" + progress);
			counter++;
		}
		String branches = sb.substring(0, sb.length() - 1);

		try {
			FileHandler.writeToFile(new File(file), branches);
		} catch (IOException e) {
			e.printStackTrace();
		}

		sb = new StringBuilder("");
		int totalLines = lines.size();
		for (String className : lines.keySet()) {
			for (int b : lines.get(className).keySet()) {
				sb.append(className);
				sb.append("#");
				sb.append(b);
				sb.append(",");
				String progress = "[";
				float percent = counter / (float) totalBranches;
				int b1 = (int) (percent * bars);
				for (int i = 0; i < b1; i++) {
					progress += "-";
				}
				progress += ">";
				int b2 = bars - b1;
				for (int i = 0; i < b2; i++) {
					progress += " ";
				}
				progress += "] " + (int) (percent * 100) + "% [" + counter + " of " + totalBranches + "]";
				out.print("\r" + progress);
				counter++;
			}
		}
		String lines = sb.substring(0, sb.length() - 1);

		try {
			FileHandler.writeToFile(new File(file2), lines);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void classAnalyzed(String className, List<BranchHit> branchHitCounterIds,
			List<LineHit> lineHitCounterIds) {
		lines.put(className, new HashMap<Integer, LineHit>());
		for (LineHit lh : lineHitCounterIds) {
			lines.get(className).put(lh.getCounterId(), lh);
		}
		for (BranchHit b : branchHitCounterIds) {
			int branchId = getNewBranchId();
			branches.put(branchId, b);
		}
	}

	private static Line findLineWithCounterId(String className, int i) {
		return lines.get(className).containsKey(i) ? lines.get(className).get(i).getLine() : null;
	}

	private static BranchHit findBranchWithCounterId(String className, int i) {
		for (BranchHit bh : branches.values()) {
			if (bh.getFalseCounterId() == i || bh.getTrueCounterId() == i) {
				return bh;
			}
		}
		return null;
	}

	public static void classChanged(String changedClass) {
		Class<?> cl = ClassStore.get(changedClass);
		if (cl != null) {
			changedClasses.add(cl);
		}
	}

	public static void collectHitCounters() {
		if (Properties.INSTRUMENTATION_APPROACH == InstrumentationApproach.ARRAY) {
			if (Properties.LOG) {
				TaskTimer.taskStart(new CollectHitCountersTimer());
			}
			List<Class<?>> classes = changedClasses;
			if (!Properties.USE_CHANGED_FLAG) {
				classes = new ArrayList<Class<?>>();
				for (String name : lines.keySet()) {
					classes.add(ClassStore.get(name));
				}
				changedClasses.addAll(classes);

			}
			for (Class<?> cl : classes) {
				try {
					Method getCounters = cl.getDeclaredMethod(ArrayClassVisitor.COUNTER_METHOD_NAME, new Class<?>[] {});
					getCounters.setAccessible(true);
					int[] counters = (int[]) getCounters.invoke(null, new Object[] {});
					if (counters != null) {
						for (int i = 0; i < counters.length; i++) {
							Line line = findLineWithCounterId(cl.getName(), i);
							if (line != null) {
								line.hit(counters[i]);
							}
							BranchHit branch = findBranchWithCounterId(cl.getName(), i);
							if (branch != null) {
								if (branch.getTrueCounterId() == i) {
									branch.getBranch().trueHit(counters[i]);
								} else {
									branch.getBranch().falseHit(counters[i]);
								}
							}
						}
					}
					Method resetCounters = cl.getDeclaredMethod(ArrayClassVisitor.RESET_COUNTER_METHOD_NAME,
							new Class[] {});
					resetCounters.setAccessible(true);
					resetCounters.invoke(null, new Object[] {});
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			TaskTimer.taskEnd();
		}
	}

	public static List<Line> getCoverableLines(String className) {
		if (!lines.containsKey(className)) {
			return Collections.<Line> emptyList();
		}
		List<Line> coverableLines = new ArrayList<Line>();
		for (LineHit lh : lines.get(className).values()) {
			coverableLines.add(lh.getLine());
		}
		return coverableLines;
	}

	public static List<Branch> getCoverableBranches(String className) {
		List<Branch> coverableBranches = new ArrayList<Branch>();
		for (BranchHit bh : branches.values()) {
			if (bh.getBranch().getClassName().equals(className)) {
				coverableBranches.add(bh.getBranch());
			}
		}
		return coverableBranches;
	}

	public static List<Class<?>> getChangedClasses() {
		return new ArrayList<Class<?>>(changedClasses);
	}

	private static final class CollectHitCountersTimer extends AbstractTask {
		@Override
		public String asString() {
			return "Collecting hit counters";
		}
	}
}
