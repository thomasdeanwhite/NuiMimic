package com.sheffield.instrumenter.analysis.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TaskTimer {
	private static long applicationStart = System.currentTimeMillis();
	private static Task currentTask;
	private static final Logger LOGGER = LogManager.getLogger("TimingsLogger");

	public static void taskStart(Task task) {
		currentTask = task;
		task.start();
	}

	public static void taskEnd() {
		currentTask.end();
		report();
		currentTask = null;
	}

	public static void report() {
		LOGGER.info(currentTask.asString() + ":\n\tStart Time: " + (currentTask.getStartTime() - applicationStart)
				+ "\n\tEnd Time:" + (currentTask.getEndTime() - applicationStart) + "\n\tTotal Time: "
				+ (currentTask.getEndTime() - currentTask.getStartTime()));
	}
}
