package com.sheffield.leapmotion.analyzer;

import java.util.ArrayList;
import java.util.Random;

public class ProbabilityDataAnalyzer extends HillClimbingDataAnalyzer {

	private Random random = new Random();
	private ArrayList<String> sequence;

	public ProbabilityDataAnalyzer() {
		super();
		sequence = new ArrayList<String>();
	}

	@Override
	public void output(String directory) {
		String finalSequence = "";
		for (int i = 0; i < AnalyzerApp.SEQUENCE_LENGTH; i++) {
			String next = nextHand();
			finalSequence += next + " ";
		}
		System.out.println(finalSequence);
	}

	@Override
	public String nextHand() {
		if (sequence.size() == 0) {
			String start = (String) map.keySet().toArray()[0];
			String[] ss = start.split(" ");
			for (String s : ss) {
				sequence.add(s);
			}
		}

		while (sequence.size() > ngramSize) {
			sequence.remove(0);
		}

		String key = "";
		for (String s : sequence) {
			key += s + " ";
		}
		key = key.substring(0, key.length() - 1);
		ArrayList<SequenceSimilarity> seqs = map.get(key);

		SequenceSimilarity newValue = null;

		if (seqs == null) {
			String newHand = getBackupSequence();
			sequence.add(newHand);
			return newHand;
		}

		double r = Math.random();

		for (SequenceSimilarity s : seqs) {
			float probability = s.probability;
			// if (sequence.contains(s)) {
			// probability /= s.freq / 3;
			// }
			if (r < probability) {
				newValue = s;
				break;
			}
			r -= s.probability;
		}
		if (newValue == null) {
			String newHand = getBackupSequence();
			sequence.add(newHand);
			return newHand;
		}

		sequence.add(newValue.sequence);
		return newValue.sequence;
	}

	public String getBackupSequence() {
		int number = random.nextInt(map.keySet().size());
		for (String s : map.keySet()){
			if (number <= 0){
				String s1 = s.split(" ")[0];
				return s1;
			}
			number--;
		}
		return "";
	}

}
