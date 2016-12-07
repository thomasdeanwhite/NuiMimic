package com.sheffield.leapmotion.frame.analyzer;

import java.util.ArrayList;
import java.util.HashMap;
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
    public String getCurrentKey(){
        while (sequence.size() == 0) {
            Object[] candidates = map.keySet().toArray();
            String start = (String)candidates[random.nextInt(candidates.length)];
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
        return key;
    }

    @Override
    public boolean hasNext(String label) {
        ArrayList<SequenceSimilarity> seqs = map.get(label);
        if (seqs == null) {
            return false;
        }
        return true;
    }

    @Override
    public String next() {
        return next(getCurrentKey());

    }

    @Override
    public String next(String key) {
        ArrayList<SequenceSimilarity> seqs = map.get(key);

        if (seqs == null) {
            AnalyzerApp.MISS++;
            String newHand = null;
            HashMap<String, ArrayList<SequenceSimilarity>> buffer = new HashMap<String, ArrayList<SequenceSimilarity>>();

            for (String s : map.keySet()){
                if (s.trim().endsWith(sequence.get(sequence.size()-1))){
                    buffer.put(s, map.get(s));
                }
            }

            int sequenceNumber = sequence.size();

            while (sequenceNumber >= 0 && buffer.size() > 0){
                sequenceNumber--;
                String[] buffersKeys = new String[buffer.keySet().size()];

                buffer.keySet().toArray(buffersKeys);

                newHand = buffersKeys[random.nextInt(buffersKeys.length)];

                String smoothingKey = "";

                for (int i = sequenceNumber; i < sequence.size(); i++){
                    smoothingKey += smoothingKey + " ";
                }

                smoothingKey = smoothingKey.trim();

                boolean found = false;
                for (String s : buffer.keySet()){
                    if (!s.trim().endsWith(smoothingKey)){
                        buffer.remove(s);
                    }
                }

                if (buffer.size() > 0){
                    buffersKeys = new String[buffer.keySet().size()];

                    buffer.keySet().toArray(buffersKeys);

                    newHand = buffersKeys[random.nextInt(buffersKeys.length)];
                    break;
                }
            }

            seqs = map.get(newHand);

            if (seqs == null) {
                newHand = getBackupSequence();
                sequence.add(newHand);
                return newHand;
            }
        } else {
            AnalyzerApp.HITS++;
        }

        SequenceSimilarity newValue = null;

        for (ProbabilityListener pbl : probabilityListeners) {
            pbl.probabilityListLoaded(seqs, 1f);
        }

        //assert(seqs.size() == ngramCandidates.size());

        float totalProbability = 0f;

        float scalingProbability = 1f;

        boolean found = false;

        do {

            double r = Math.random();

            for (SequenceSimilarity ss : seqs) {


                float probability = ss.probability;

                if (probability == 0){
                    continue;
                }

                for (ProbabilityListener pbl : probabilityListeners) {
                    probability = pbl.changeProbability(ss, seqs);
                }

                probability *= scalingProbability;

                if (r <= probability) {
                    newValue = ss;
                    found = true;
                    break;
                }
                r -= probability;

                totalProbability += probability;
            }

            scalingProbability = 1f / totalProbability;
        } while(scalingProbability > 1f && !found);

        // if newValue == null, no sequence exists (sparse)
        if (newValue == null) {
            String newHand = getBackupSequence();
            sequence.add(newHand);
            return newHand;
        }

        sequence.add(newValue.sequence);
        return newValue.sequence;
    }

    private int backupSeeded = 0;

    public String getBackupSequence() {
        int number = random.nextInt(ngramCandidates.size());
        backupSeeded++;
        return ngramCandidates.get(number);
    }

}
