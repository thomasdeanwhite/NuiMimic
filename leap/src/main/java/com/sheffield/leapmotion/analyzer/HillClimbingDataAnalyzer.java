package com.sheffield.leapmotion.analyzer;

import java.util.ArrayList;
import java.util.HashMap;

public class HillClimbingDataAnalyzer implements DataAnalyzer {

	protected HashMap<String, ArrayList<SequenceSimilarity>> map;
	protected int ngramSize = 0;
	//hashmap of cumulated frequences of candidates for key <String>
	protected HashMap<String, Integer> totals;
	protected ArrayList<String> ngramCandidates;

	protected ArrayList<ProbabilityListener> probabilityListeners;



	public HillClimbingDataAnalyzer() {
		map = new HashMap<String, ArrayList<SequenceSimilarity>>();
		totals = new HashMap<String, Integer>();
		ngramCandidates = new ArrayList<String>();
		probabilityListeners = new ArrayList<ProbabilityListener>();
	}


	public void analyze(ArrayList<String> ps, boolean logBase) {

		for (String s : ps) {
			String[] d = s.split(":");
			String[] d2 = d[1].split("\\|");
			String[] series = d[0].split(" ");
			String last = series[series.length - 1];
			ngramSize = series.length;
			ArrayList<SequenceSimilarity> seqs = new ArrayList<SequenceSimilarity>();
			float total = 1;
			for (int i = 0; i < d2.length; i++) {
				d2[i] = d2[i].trim();
				if (d2[i].length() <= 1) {
					break;
				}
				String[] d3 = d2[i].split(" ");

				d3[0] = d3[0].trim();

				if (d3[0].equals(last) && AnalyzerApp.REMOVE_REPEATED) {
					continue;
				}

				d3[1] = d3[1].trim();

				d3[1] = d3[1].replaceAll("\\{", "");
				d3[1] = d3[1].replaceAll("\\}", "");
				int freq = 0;
				try {
					freq = Integer.parseInt(d3[1]);
				} catch (NumberFormatException e) {
					continue;
				}
				String seq = d3[0];
				boolean exists = false;
				for (int j = 0; j < seqs.size(); j++) {
					if (seqs.get(j).sequence.equals(seq)) {
						exists = true;
						seqs.get(j).freq += freq;
						break;
					}
				}

				if (!exists) {
					seqs.add(new SequenceSimilarity(seq, freq));
					if (!ngramCandidates.contains(seq) && !seq.equals("NULL")){
						ngramCandidates.add(seq);
					}
				}
				if (logBase) {
					total *= log(freq);
				} else {
					total += freq;
				}
			}
			for (SequenceSimilarity ss : seqs) {
				if (logBase) {
					ss.freq = (int)log(ss.freq);
				}
				ss.probability = ss.freq / total;
				// System.out.println(d[0] + ":" + ss.sequence + " " + ss.freq +
				// " " + ss.probability);
			}
			map.put(d[0], seqs);
			totals.put(d[0], (int)total);
		}

	}

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

			int highestFreq = 0;

			SequenceSimilarity newValue = null;

			// System.out.println(key);

			if (seqs == null) {
				sequence.add(sequence.get(ngramSize - 2));
				continue;
			}

			for (SequenceSimilarity s : seqs) {
				if (s.freq > highestFreq) {
					highestFreq = s.freq;
					newValue = s;
				}
			}
			if (newValue == null) {
				sequence.add(sequence.get(ngramSize - 2));
				continue;
			}

			sequence.add(newValue.sequence);
			finalSequence += newValue.sequence + " ";
		}
		System.out.println(finalSequence);
	}

	public String next() {
		// TODO Auto-generated method stub
		throw new IllegalStateException();
	}

	@Override
	public void addProbabilityListener(ProbabilityListener pbl) {
		probabilityListeners.add(pbl);
	}

	public double log(float f){
		return Math.log(f);
	}

}
