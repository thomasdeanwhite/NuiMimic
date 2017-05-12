package com.sheffield.leapmotion.runtypes;

import com.google.gson.Gson;
import com.sheffield.leapmotion.App;
import static com.sheffield.leapmotion.Properties.*;
import com.sheffield.leapmotion.frame.analyzer.machinelearning.clustering.ClusterResult;
import com.sheffield.leapmotion.frame.analyzer.machinelearning.clustering.WekaClusterer;
import com.sheffield.leapmotion.frame.analyzer.machinelearning.ngram.NGram;
import com.sheffield.leapmotion.frame.analyzer.machinelearning.ngram.NGramModel;
import com.sheffield.leapmotion.output.StateComparator;
import com.sheffield.leapmotion.util.FileHandler;
import com.sheffield.output.Csv;
import weka.core.Instance;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by thomas on 08/02/17.
 */
public class IntrinsicEvaluationRunType implements RunType {

    @Override
    public int run() {

        DataRawProcessingRunType drpr = new DataRawProcessingRunType();

        drpr.run();

        Gson gson = new Gson();

        HashMap<String, String> files = new HashMap<String, String>();

        files.put("joint_positions_pool.ARFF", "joint_position");
        files.put("hand_positions_pool.ARFF", "hand_position");
        files.put("hand_rotations_pool.ARFF", "hand_rotation");

        HashMap<String, ClusterResult> results = new HashMap<String, ClusterResult>(3);

        if (DIRECTORY.toLowerCase().endsWith("/processed")) {
            DIRECTORY = DIRECTORY.substring(0, DIRECTORY.lastIndexOf("/"));
        }
        String dataDir = DIRECTORY + "/" + INPUT[0];

        for (String s : files.keySet()) {

            String joints = dataDir + "/" + s;

            WekaClusterer wc = new WekaClusterer(joints);

            try {
                ClusterResult cr = wc.cluster();

                results.put(s, cr);

                HashMap<String, String> assignments = cr.getAssignments();
                HashMap<String, Instance> centroids = cr.getCentroids();

                ArrayList<String> keys = new ArrayList<>();

                keys.addAll(assignments.keySet());

                keys.sort(new Comparator<String>() {
                    @Override
                    public int compare(String s, String t1) {
                        return s.compareTo(t1);
                    }
                });

                ArrayList<String> clusterOrder = new ArrayList<>();

                for (String key : keys){
                    clusterOrder.add(assignments.get(key));
                }

                File outputSequence = new File(dataDir + "/processed/" + files.get(s) + ".raw_sequence");

                if (!outputSequence.exists()){
                    if (outputSequence.getParentFile() != null && !outputSequence.getParentFile().exists()){
                        outputSequence.getParentFile().mkdirs();
                    }
                    outputSequence.createNewFile();
                }

                Csv csv = new Csv();

                int size = (int) Math.sqrt(clusterOrder.size());

                float perplexity = 0f;

                for (int i = N; i < size; i++){
                    String candidate = "";

                    for (int j = i-N; j < i; j++){
                        candidate += clusterOrder.get(j) + " ";
                    }

                    StringBuilder sb = new StringBuilder();

                    for (int k = i; k < clusterOrder.size()-1; k++){
                        sb.append(clusterOrder.get(i) + " ");
                    }


                    NGram ng = NGramModel.getNGram(N, sb.toString());

                    ng.calculateProbabilities();

                    perplexity += Math.pow(1f/ng.getProbability(candidate), 1/N);
                }


                csv.add("preplexity", "" + perplexity);
                csv.add("cluster", "" + CLUSTERS);
                csv.add("N", "" + N);

                File f = new File("NuiMimicEvaluation.csv");

                if (!f.exists()){
                    f.createNewFile();
                    FileHandler.writeToFile(f, csv.getHeaders());
                }

                FileHandler.appendToFile(f, csv.getValues());

            } catch (Exception e) {
                e.printStackTrace(App.out);
            }
        }

        return 0;
    }
}
