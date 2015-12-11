package com.sheffield.leapmotion.analyzer;

import java.util.ArrayList;
import java.util.Random;

public class HMMDataAnalyzer extends HillClimbingDataAnalyzer {

	private Random random = new Random();

	@Override
	public void output(String directory) {
		String finalSequence = "";
		ArrayList<String> sequence = new ArrayList<String>();
		String start = (String) map.keySet().toArray()[0];
		String[] ss = start.split(" ");
		for (String s : ss) {
			sequence.add(s);
		}
		for (int i = 0; i < AnalyzerApp.SEQUENCE_LENGTH; i++) {

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
				sequence.add(getBackupSequence());
				i--;
				continue;
			}

			double r = Math.random();

			for (SequenceSimilarity s : seqs) {
				float probability = s.probability;
				if (sequence.contains(s)) {
					probability /= 3;
				}
				if (probability <= r) {
					newValue = s;
					break;
				}
				r -= s.probability;
			}
			if (newValue == null) {
				sequence.add(getBackupSequence());
				i--;
				continue;
			}

			sequence.add(newValue.sequence);
			finalSequence += newValue.sequence + " ";
		}
		System.out.println(finalSequence);
	}

	public String getBackupSequence() {
		return "" + random.nextInt(100);
	}

}
