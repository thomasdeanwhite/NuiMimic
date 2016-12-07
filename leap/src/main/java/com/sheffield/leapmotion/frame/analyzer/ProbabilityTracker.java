package com.sheffield.leapmotion.frame.analyzer;


import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.output.StateComparator;

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
    protected static float UNSEEN_OBJECT_PROBABILITY = 0.001f;

    public ProbabilityTracker(HashMap<Integer, HashMap<String, Float>> states,
                              HashMap<Integer, Integer> totals){
        this.states = states;
        this.totals = totals;
        currentProbabilities = new HashMap<String, Float>();
    }

    @Override
    public void probabilityListLoaded(List<SequenceSimilarity> sequences, float maxProbability) {
        currentProbabilities.clear();
        int currState = StateComparator.getCurrentState();

        //if we are in an unseen state, we can't do anything
        if (!states.containsKey(currState)){
            return;
        }
        HashMap<String, Float> currentState = states.get(currState);
        float total = 0f;
        for (SequenceSimilarity s : sequences){
            float stateProbability = 0f;//1f / (float) totals.get(currState);
            if (currentState.containsKey(s.sequence)){
                stateProbability = currentState.get(s.sequence);
            }
            float prob = ((1f - Properties.STATE_WEIGHT) * s.probability) +
                    (Properties.STATE_WEIGHT * stateProbability);
            currentProbabilities.put(s.sequence, prob);
            total += prob;
        }

        maxProbability -= UNSEEN_OBJECT_PROBABILITY;


        float scalar = maxProbability / total;

        float maxProb = 0f;

        for (String s : currentProbabilities.keySet()){
            float p = currentProbabilities.get(s) * scalar;
            currentProbabilities.put(s, p);
            maxProb += p;
        }

        assert maxProb <= 1f;
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

        float stateProbability = 0f;

        if (currentProbabilities.containsKey(output.sequence)) {
            stateProbability = currentProbabilities.get(output.sequence);
        }
        return stateProbability;
        //return UNSEEN_OBJECT_PROBABILITY * (totals.size() - currentProbabilities.size()) / totals.size();
    }
}
