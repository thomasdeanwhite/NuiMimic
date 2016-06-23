package com.sheffield.leapmotion.analyzer;

import com.sheffield.instrumenter.FileHandler;
import com.sheffield.leapmotion.App;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AnalyzerApp {

	public static final int PROGRESS_BARS = 60;
	public static final int SEQUENCE_LENGTH = 1000;

	private boolean logBase = false;

	private String contents;
	private DataAnalyzer dataAnalyzer;
	private ArrayList<String> probabilities;

	public static long framesBeingProcessed = 0;

	public void setLogBase(boolean logBase){
		this.logBase = logBase;
	}

	public static void main(String[] args) {
		System.out.println("- Loading probabilities...");
		AnalyzerApp app = new AnalyzerApp("H:/data/leapmotion/processed/circle-25-2.positiondata");
		System.out.println("Probabilities loaded.\n- Starting Sequence Generation...");
		app.analyze();
		System.out.println("Sequence Generation Complete.\n- Starting Output");
		app.output();
		System.out.println("Output complete.");

	}

	public DataAnalyzer getDataAnalyzer() {
		return dataAnalyzer;
	}

	public AnalyzerApp(String file) {
		App.out.println("* Loading Sequence Data from " + file);
		try {
			contents = FileHandler.readFile(new File(file));

			dataAnalyzer = new ProbabilityDataAnalyzer();

			probabilities = new ArrayList<String>();

			String[] ps = contents.split("\n");

			for (int i = 0; i < ps.length; i += 3) {
				String conts = ps[i].trim() + ":" + ps[i + 1].trim();
				probabilities.add(conts);
				// System.out.print("\r" + conts);
			}

			App.out.println("\t*! Sequence Loading Done (" + file + ")");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void analyze() {
		try {
			App.out.println("- Processing Sequence");
			dataAnalyzer.analyze(probabilities, logBase);
			App.out.println("\r- Sequence processed!");

		} catch (Exception e){
			e.printStackTrace(App.out);
		}
	}

	public void output() {
		dataAnalyzer.output("processed-data");
	}

	public void addProbabilityListener(ProbabilityListener pbl){
		dataAnalyzer.addProbabilityListener(pbl);
	}

}
