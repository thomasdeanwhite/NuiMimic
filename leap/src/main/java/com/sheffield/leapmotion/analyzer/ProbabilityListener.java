package com.sheffield.leapmotion.analyzer;

import java.util.List;

/**
 * Created by thomas on 23/03/2016.
 */
public interface ProbabilityListener {
    void probabilityListLoaded(List<SequenceSimilarity> seqs, float maxProbability);
    float changeProbability(SequenceSimilarity output);
}
