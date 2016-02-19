package com.sheffield.instrumenter.analysis.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.sheffield.instrumenter.Properties;
import com.sheffield.instrumenter.analysis.ClassAnalyzer;

public class TaskTimer {
	private static long applicationStart = System.currentTimeMillis();
	private static Task currentTask;
	private static FileOutputStream out;

	static {
		DateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss");
		File dir = new File(Properties.LOG_DIR + "/timings/");
		File file = new File(dir.getAbsolutePath() + "/" + format.format(Calendar.getInstance().getTime()) + ".csv");
		try {
			if (!dir.exists()) {
				dir.mkdir();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			out = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace(ClassAnalyzer.out);
		} catch (IOException e) {
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
