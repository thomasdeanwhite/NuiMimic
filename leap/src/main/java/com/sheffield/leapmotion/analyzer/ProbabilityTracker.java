package com.sheffield.leapmotion.analyzer;


import com.sheffield.leapmotion.output.DctStateComparator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by thomas on 29/03/2016.
 */
public class ProbabilityTracker implements ProbabilityListener {
    protected HashMap<Integer, HashMap<String, Float>> states;
    protected HashMap<Integer, Integer> totals;
    protected HashMap<String, Float> currentProbabilities;
    protected static float UNSEEN_OBJECT_PROBABILITY = 0.05f;

    public ProbabilityTracker(HashMap<Integer, HashMap<String, Float>> states,
                              HashMap<Integer, Integer> totals){
        this.states = states;
        this.totals = totals;
        currentProbabilities = new HashMap<String, Float>();
    }

    @Override
    public void probabilityListLoaded(List<SequenceSimilarity> sequences, float maxProbability) {
        currentProbabilities.clear();
        int currState = DctStateComparator.getCurrentState();

        //if we are in an unseen state, we can't do anything
        if (!states.containsKey(currState)){
            return;
        }
        HashMap<String, Float> currentState = states.get(currState);
        float total = 0f;
        for (SequenceSimilarity s : sequences){
            float stateProbability = 1f / (float) totals.get(currState);
            if (currentState.containsKey(s.sequence)){
                stateProbability = currentState.get(s.sequence);
            }
            float prob = s.probability * stateProbability;
            currentProbabilities.put(s.sequence, stateProbability);
            total += prob;
        }

        maxProbability -= UNSEEN_OBJECT_PROBABILITY;


        float scalar = maxProbability / total;

        for (String s : currentProbabilities.keySet()){
            currentProbabilities.put(s, currentProbabilities.get(s) * scalar);
        }
    }

    @Override
    public float changeProbability(SequenceSimilarity output, ArrayList<SequenceSimilarity> totals) {
        if (output == null){
            return 0;
        }
        if (currentProbabilities.size() == 0){
            //in an unseen state
            return output.probability;
        }
        if (currentProbabilities.containsKey(output.sequence)) {
            return output.probability * currentProbabilities.get(output.sequence);
        }
        return UNSEEN_OBJECT_PROBABILITY * (totals.size() - currentProbabilities.size()) / totals.size();
    }
}
