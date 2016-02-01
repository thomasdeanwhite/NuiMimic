package com.sheffield.instrumenter.analysis.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.sheffield.instrumenter.Properties;
import com.sheffield.instrumenter.analysis.ClassAnalyzer;

public class TaskTimer {
	private static long applicationStart = System.currentTimeMillis();
	private static Task currentTask;
	private static FileOutputStream out;

	static {
		File file = new File(Properties.LOG_DIR + "timings-" + System.currentTimeMillis() + ".csv");
		try {
			out = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace(ClassAnalyzer.out);
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace(ClassAnalyzer.out);
				}
			}
		});
	}

	public static void taskStart(Task task) {
		currentTask = task;
		task.start();
	}

	public static void taskEnd() {
		if (currentTask != null) {
			currentTask.end();
			report();
			currentTask = null;
		}
	}

	public static void report() {
		StringBuilder sb = new StringBuilder();
		sb.append(currentTask.asString());
		sb.append(",");
		sb.append(currentTask.getStartTime() - applicationStart);
		sb.append(",");
		sb.append(currentTask.getEndTime() - applicationStart);
		sb.append(",");
		sb.append(currentTask.getEndTime() - currentTask.getStartTime());
		sb.append("\n");
		try {
			out.write(sb.toString().getBytes(), 0, sb.toString().length());
			out.flush();
		} catch (IOException e) {
			e.printStackTrace(ClassAnalyzer.out);
		}
	}
}
