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
            String next = next();
            finalSequence += next + " ";
        }
        System.out.println(finalSequence);
    }

    @Override
    public String next() {
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

        if (seqs == null) {
            String newHand = getBackupSequence();
            sequence.add(newHand);
            return newHand;
        }

        SequenceSimilarity newValue = null;

        double r = Math.random();
        // Apply additive smoothing: if r < n then select another random candidate
        float maxProbability = 1.0f - (ngramCandidates.size() / ((float) totals.get(key) + ngramCandidates.size()));
        if (r < maxProbability) {
            r = Math.random();
            for (ProbabilityListener pbl : probabilityListeners) {
                pbl.probabilityListLoaded(seqs, maxProbability);
            }
            for (SequenceSimilarity s : seqs) {
                float probability = s.probability;
                for (ProbabilityListener pbl : probabilityListeners) {
                    probability = pbl.changeProbability(s);
                }
                if (r < probability) {
                    newValue = s;
                    break;
                }
                r -= s.probability;
            }
        }

        // if newValue == null, either no sequence exists (sparse) or additive smoothing in action
        if (newValue == null) {
            String newHand = getBackupSequence();
            sequence.add(newHand);
            return newHand;
        }

        sequence.add(newValue.sequence);
        return newValue.sequence;
    }

    public String getBackupSequence() {
        int number = random.nextInt(ngramCandidates.size());
        return ngramCandidates.get(number);
    }

}
