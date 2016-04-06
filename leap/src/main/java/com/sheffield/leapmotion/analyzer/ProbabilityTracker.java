package com.sheffield.leapmotion.analyzer;

import com.sheffield.leapmotion.sampler.com.sheffield.leapmotion.sampler.output.DctStateComparator;

import java.util.HashMap;
import java.util.List;

/**
 * Created by thomas on 29/03/2016.
 */
public class ProbabilityTracker implements ProbabilityListener {
    private HashMap<Integer, HashMap<String, Float>> states;
    private HashMap<Integer, Integer> totals;
    private HashMap<String, Float> currentProbabilities;

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


        float scalar = maxProbability / total;

        for (String s : currentProbabilities.keySet()){
            currentProbabilities.put(s, currentProbabilities.get(s) * scalar);
        }
    }

    @Override
    public float changeProbability(SequenceSimilarity output) {
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
        return 0f;
    }
}
