package com.sheffield;

import com.scythe.instrumenter.analysis.ClassAnalyzer;
import com.scythe.instrumenter.instrumentation.objectrepresentation.Branch;
import com.scythe.instrumenter.instrumentation.objectrepresentation.BranchHit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GoalTracker {

    private ArrayList<BranchHit> goals;
    private HashMap<Integer, BranchHit> coveredGoals;

    public GoalTracker (){
        List<BranchHit> branches = ClassAnalyzer.getAllBranches();

        goals = new ArrayList<>();
        coveredGoals = new HashMap<Integer, BranchHit>();

        //TODO: get covered and target goals from ClassAnalayzer

        //TODO: Implement covered and target goals in ClassAnalyzer
    }

}
