package com.sheffield.leapmotion.frame.analyzer;

import com.sheffield.leapmotion.Properties;

import java.util.ArrayList;
import java.util.HashMap;

public class HillClimbingDataAnalyzer implements DataAnalyzer {

	protected HashMap<String, ArrayList<SequenceSimilarity>> map;
	protected int ngramSize = 0;
	//hashmap of cumulated frequences of candidates for key <String>
	protected HashMap<String, Integer> totals;
	protected ArrayList<String> ngramCandidates;
	protected HashMap<Integer, ArrayList<String>> iof;


	protected ArrayList<ProbabilityListener> probabilityListeners;



	public HillClimbingDataAnalyzer() {
		map = new HashMap<String, ArrayList<SequenceSimilarity>>();
		totals = new HashMap<String, Integer>();
		ngramCandidates = new ArrayList<String>();
		probabilityListeners = new ArrayList<ProbabilityListener>();
		iof = new HashMap<Integer, ArrayList<String>>();
	}

	public float lerpProbability(SequenceSimilarity s, SequenceSimilarity s2, float f){
		if (s.parent.equals(" ") || s.smoothed){
			return s.probability;
		}
		String newSeq = "";

		float probability = 0;

		if (s != null){
			probability = s.probability;
		}
		probability = (1f - f) * lerpProbability(s2, s2.parentSeq, f) + (f * probability);

		s.smoothed = true;
		s.probability = probability;

		return probability;
	}

	public void setupParents(SequenceSimilarity ss){

//		if (Properties.LERP_RATE == 1f) {//lerp disabled
//			return;
//		}

		if (ss.parentSeq != null){
			return;
		}
		String[] parents = ss.parent.split(" ");
		String newSeq = "";
		for (int i = 1; i < parents.length; i++){
			newSeq += parents[i] + " ";
		}
		newSeq = newSeq.trim();
		if (newSeq.length() == 0){
			newSeq = " ";
		}
		SequenceSimilarity newS = null;

		if (map.containsKey(newSeq)) {
			for (SequenceSimilarity ns : map.get(newSeq)) {
				if (ns.sequence.equals(ss.sequence)) {
					newS = ns;
					break;
				}
			}
		}

		if (newS == null){
			int index = ss.parent.indexOf(" ");
			String parent = " ";
			if (index >= 0){
				parent = ss.parent.substring(index);
			}
			newS = new SequenceSimilarity(ss.sequence, 0, parent);
		}

		assert(ss.sequence == newS.sequence);

		ss.parentSeq = newS;
	}


	public void analyze(ArrayList<String> ps, boolean logBase) {

		HashMap<String, Integer> occurances = new HashMap<String, Integer>();

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
					seqs.add(new SequenceSimilarity(seq, freq, d[0]));
					if (!ngramCandidates.contains(seq) && !seq.equals("NULL") && seq.trim().length() > 0){
						ngramCandidates.add(seq);
					}
				}
				if (logBase) {
					total *= log(freq);
				} else {
					total += freq;
				}
			}

            String[] par = d[0].split(" ");

            String parent = "";

            for (int i = 1; i < par.length; i++){
                parent += par[i] + " ";
            }

            //parent = parent.trim();

			for (SequenceSimilarity ss : seqs) {
                String label = parent + ss.sequence;

                String[] m = label.split(" ");

                for (int i = 0; i < m.length; i++) {
                    String minimisation = "";

                    for (int j = i; j < m.length; j++){
                        minimisation += m[j] + " ";
                    }
                    minimisation = minimisation.trim();
                    if (!occurances.containsKey(minimisation)) {
                        occurances.put(minimisation, 0);
                    }
                    occurances.put(minimisation, occurances.get(minimisation) + ss.freq);

                }


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

        map.put(" ", new ArrayList<SequenceSimilarity>());
        totals.put(" ", 0);

        int wordCount = 0;

        for (String s : occurances.keySet()){
            int freq = occurances.get(s);

            String[] sub = s.split(" ");
            String value = sub[sub.length-1];

            for (int i = 0; i < sub.length-1; i++){
                String v = "";
                for (int j = i; j < sub.length-1; j++){
                    v = sub[j] + " ";
                }
				v = v.trim();
                if (!map.containsKey(v)){
                    map.put(v, new ArrayList<SequenceSimilarity>());
                }
                boolean found = false;
                for (SequenceSimilarity ss : map.get(v)){
                    if (ss.sequence.equals(value)){
                        found = true;
                        ss.freq += freq;
                    }
                }

                if (!found) {
					if (Properties.LAPLACE_SMOOTHING) {
						freq = 1;
					}
					map.get(v).add(new SequenceSimilarity(value, freq, v));
                }

                if (!totals.containsKey(v)){
                    totals.put(v, 0);
                }

                totals.put(v, totals.get(v) + freq);
            }

            boolean found = false;
            for (SequenceSimilarity ss : map.get(" ")){
                if (ss.sequence.equals(value)){
                    found = true;
                    ss.freq += freq;
                }
            }

            if (!found) {
                map.get(" ").add(new SequenceSimilarity(value, freq, " "));
            }

            totals.put(" ", totals.get(" ") + freq);

            wordCount += freq;

            if (!iof.containsKey(freq)){
                iof.put(freq, new ArrayList<String>());
            }

            iof.get(freq).add(s);
        }

        for (String s : map.keySet()){
            for (SequenceSimilarity ss : map.get(s)){
                ss.probability = ss.freq / (float)totals.get(ss.parent);

				setupParents(ss);
            }
        }


			for (String upLevel : map.keySet()){

				ArrayList<SequenceSimilarity> seqs = map.get(upLevel);

				for (String sName : ngramCandidates) {
					SequenceSimilarity s = null;
					for (SequenceSimilarity ss : seqs) {
						if (ss.sequence.equals(sName)) {
							s = ss;
							break;
						}
					}

					SequenceSimilarity parent = null;

					String ul = upLevel;

					int missed = 0;

					if (map.containsKey(ul)) {
						SequenceSimilarity s1 = null;
						for (SequenceSimilarity ss : map.get(ul)) {
							if (ss.sequence.equals(sName)) {
								s = ss;
								break;
							}
						}
						parent = s;
					}


					while (parent == null && ul.length() > 0) {
						missed++;
						int newIndex = ul.indexOf(" ");
						String newLevel = " ";

						if (newIndex != -1) {
							newLevel = ul.substring(newIndex).trim();
						}
						if (map.containsKey(newLevel)) {
							for (SequenceSimilarity ss : map.get(newLevel)) {
								if (ss.sequence.equals(sName)) {
									s = ss;
									break;
								}
							}
							parent = s;
						}
						ul = newLevel;
					}

					SequenceSimilarity p = parent;

					while (p != null && ul.length() > 0) {
						int newIndex = ul.indexOf(" ");
						String newLevel = " ";

						if (newIndex != -1) {
							newLevel = ul.substring(newIndex).trim();
						}
						if (map.containsKey(newLevel)) {
							for (SequenceSimilarity ss : map.get(newLevel)) {
								if (ss.sequence.equals(sName)) {
									s = ss;
									break;
								}
							}
							p = s;
						}
						ul = newLevel;
					}
					if (parent != null) {
						if (Properties.LAPLACE_SMOOTHING){
							s.probability = s.freq / totals.get(s);
						} else {
							float probability = (float) (Math.pow(1f - Properties.LERP_RATE, missed) *
									lerpProbability(s, parent, Properties.LERP_RATE));
							if (s.parent.equals(upLevel)) {
								s.probability = probability;
							} else {
								s = new SequenceSimilarity(sName, 0, upLevel);
								s.probability = probability;
								s.smoothed = true;
								setupParents(s);
								map.get(upLevel).add(s);
							}
						}
					}
				}


		}

		for (String upLevel : map.keySet()){
			ArrayList<SequenceSimilarity> seqs = map.get(upLevel);
			float maxProb = 0f;
			for (SequenceSimilarity ss : seqs){
				maxProb += ss.probability;
			}

			maxProb = 1f / maxProb;

			for (SequenceSimilarity ss : seqs){
				ss.probability *= maxProb;
			}
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
	public String next(String label) {
		return next();
	}

	@Override
	public boolean hasNext(String label) {
		return false;
	}

	@Override
	public String getCurrentKey() {
		return null;
	}

	@Override
	public void addProbabilityListener(ProbabilityListener pbl) {
		probabilityListeners.add(pbl);
	}

	public double log(float f){
		return Math.log(f);
	}

}
