package com.sheffield.leapmotion.frame.analyzer;

import com.sheffield.leapmotion.App;

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

			float totalProbability = 0;
			float newProbability = 1f;

			do {

				double r = Math.random();

				for (SequenceSimilarity s : seqs) {
					float probability = s.probability * newProbability;
					if (probability == 0) {
						continue;
					}
					for (ProbabilityListener pbl : probabilityListeners) {
						probability = pbl.changeProbability(s, seqs);
					}
//				if (sequence.contains(s)) {
//					probability /= 3;
//				}
					if (probability <= r) {
						newValue = s;
						break;
					}
					r -= s.probability;

					totalProbability += s.probability;
				}

				newProbability = 1f / totalProbability;
			} while(false);//newProbability != 1f);


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

	private int backUpSeeded = 0;

	public String getBackupSequence() {
		backUpSeeded++;
		App.out.println(backUpSeeded);
		return "" + random.nextInt(100);
	}

}
