package com.sheffield.leapmotion.runtypes;

import com.google.gson.Gson;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.frame.analyzer.machinelearning.clustering.ClusterResult;
import com.sheffield.leapmotion.frame.analyzer.machinelearning.clustering.WekaClusterer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import com.sheffield.leapmotion.frame.analyzer.machinelearning.ngram.NGram;
import com.sheffield.leapmotion.frame.analyzer.machinelearning.ngram.NGramModel;
import com.sheffield.leapmotion.util.FileHandler;
import weka.core.FileHelper;
import weka.core.Instance;

/**
 * Created by thomas on 08/02/17.
 */
public class DataProcessingRunType implements RunType {
    @Override
    public int run() {

        int n = 3;

        Gson gson = new Gson();

        HashMap<String, String> files = new HashMap<String, String>();

        files.put("joint_positions_pool.ARFF", "joint_position");
        files.put("hand_positions_pool.ARFF", "hand_position");
        files.put("hand_rotations_pool.ARFF", "hand_rotation");

        HashMap<String, ClusterResult> results = new HashMap<String, ClusterResult>(3);

        if (Properties.DIRECTORY.toLowerCase().endsWith("/processed")) {
            Properties.DIRECTORY = Properties.DIRECTORY.substring(0, Properties.DIRECTORY.lastIndexOf("/"));
        }
        String dataDir = Properties.DIRECTORY + "/" + Properties.INPUT[0];

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

                FileHandler.writeToFile(outputSequence, "");

                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < clusterOrder.size()-1; i++){
                    FileHandler.appendToFile(outputSequence, clusterOrder.get(i) + ",");
                    sb.append(clusterOrder.get(i) + " ");
                }

                FileHandler.appendToFile(outputSequence, clusterOrder.get(clusterOrder.size()-1));

                FileHandler.appendToFile(outputSequence, "\n");

                for (int i = 0; i < keys.size()-1; i++){
                    FileHandler.appendToFile(outputSequence, keys.get(i) + ",");
                }

                FileHandler.appendToFile(outputSequence, keys.get(keys.size()-1));

                File outputClusters = new File(dataDir + "/processed/" + files.get(s) + "_data");

                if (!outputClusters.exists()){
                    if (outputClusters.getParentFile() != null && !outputClusters.getParentFile().exists()){
                        outputClusters.getParentFile().mkdirs();
                    }
                    outputClusters.createNewFile();
                }

                FileHandler.writeToFile(outputClusters, "");

                for (String cent : centroids.keySet()){
                    Instance centroid = centroids.get(cent);

                    FileHandler.appendToFile(outputClusters, cent + "," + centroid.toStringNoWeight() + "\n");
                }


                NGram ng = NGramModel.getNGram(n, sb.toString());

                ng.calculateProbabilities();

                File ngramOutput = new File(dataDir + "/processed/" + files.get(s) + "_ngram");

                if (!ngramOutput.exists()){
                    if (ngramOutput.getParentFile() != null && !ngramOutput.getParentFile().exists()){
                        ngramOutput.getParentFile().mkdirs();
                    }
                    ngramOutput.createNewFile();
                }

                FileHandler.writeToFile(ngramOutput, gson.toJson(ng));

            } catch (Exception e) {
                e.printStackTrace(App.out);
            }
        }

        try {
            String gestureString = FileHandler.readFile(new File(dataDir + "/sequence_gesture_data.csv"));

            NGram gestureNgram = NGramModel.getNGram(n, gestureString);

            gestureNgram.calculateProbabilities();

            File outputSequence = new File(dataDir + "/processed/gesture_type_ngram");

            if (!outputSequence.exists()){
                if (outputSequence.getParentFile() != null && !outputSequence.getParentFile().exists()){
                    outputSequence.getParentFile().mkdirs();
                }
                outputSequence.createNewFile();
            }

            FileHandler.writeToFile(outputSequence, gson.toJson(gestureNgram));

            String rawGestureString = dataDir + "/processed/gesture_type_data.raw_sequence";

            FileHandler.writeToFile(new File(rawGestureString), gestureString);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
