package com.sheffield.leapmotion.analyzer;

/**
 * Created by thomas on 23/03/2016.
 */
public class SequenceSimilarity {
    public String sequence;
    public int freq;
    public float probability;

    public SequenceSimilarity(String seq, int freq) {
        sequence = seq;
        this.freq = freq;
    }

    @Override
    public String toString() {
        return sequence + " {" + freq + "}: " + probability;
    }
}