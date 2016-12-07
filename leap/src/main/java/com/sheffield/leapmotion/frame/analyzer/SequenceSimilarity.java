package com.sheffield.leapmotion.frame.analyzer;

/**
 * Created by thomas on 23/03/2016.
 */
public class SequenceSimilarity {
    public String sequence;
    public int freq;
    public float probability;
    public SequenceSimilarity parentSeq;
    public boolean smoothed = false;

    public String parent;

    public SequenceSimilarity(String seq, int freq, String parent) {
        sequence = seq;
        this.freq = freq;
        this.parent = parent;
    }

    @Override
    public String toString() {
        return "|(" + sequence + " | " + parent + ")| = " + freq + ".\n" +
                "p(" + sequence + " | " + parent + ") = " + probability + ".";
    }
}