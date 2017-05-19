package com.sheffield.leapmotion.runtypes;

import com.google.gson.Gson;
import com.sheffield.leapmotion.*;

import static com.sheffield.leapmotion.Properties.*;

import com.sheffield.leapmotion.Properties;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * Created by thomas on 08/02/17.
 */
public class IntrinsicEvaluationRunType implements RunType {
    private HashMap<String, ArrayList<Float>> perplexity = new HashMap<>();

    @Override
    public int run() {

        HashMap<String, String> files = new HashMap<String, String>();

        files.put("joint_positions_pool.ARFF", "joint_position");
        files.put("hand_positions_pool.ARFF", "hand_position");
        files.put("hand_rotations_pool.ARFF", "hand_rotation");
        files.put("hand_joints_pool.ARFF", "hand_joints");

        HashMap<String, ClusterResult> results = new HashMap<String, ClusterResult>(3);

        if (DIRECTORY.toLowerCase().endsWith("/processed")) {
            DIRECTORY = DIRECTORY.substring(0, DIRECTORY.lastIndexOf("/"));
        }
        String dataDir = DIRECTORY + "/" + INPUT[0];


        long seed = System.currentTimeMillis();

        files.keySet().stream().map(s -> {

            String joints = dataDir + "/" + s;

            WekaClusterer wc = new WekaClusterer(joints);

            try {
                ClusterResult cr = wc.cluster();

                results.put(s, cr);

                HashMap<String, String> assignments = cr.getAssignments();

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

                int size = clusterOrder.size();//(int) Math.sqrt(clusterOrder
                        //.size());




                Random r = new Random(seed);

                int sampleSize = clusterOrder.size()/10;
                int start = N + r.nextInt(clusterOrder.size() - N - sampleSize);
                int end = start + sampleSize;

                App.out.println("Calculating " + s + " perplexity between [" + start + " .. " + end + "]");


                IntrinsicEvaluationRunType.this.perplexity.put(s, new ArrayList<>());

                DoubleStream perplexity = IntStream.range(start, end).mapToDouble(i -> {
                    String candidate = "";

                    for (int j = i-N; j < i; j++){
                        candidate += clusterOrder.get(j) + " ";
                    }

                    StringBuilder sb = new StringBuilder();

                    for (int k = i; k < clusterOrder.size(); k++){
                        sb.append(clusterOrder.get(k) + " ");
                    }

                    StringBuilder sbPrev = new StringBuilder();

                    for (int k = 0; k < i - N; k++){
                        sbPrev.append(clusterOrder.get(k) + " ");
                    }


                    NGram ng = NGramModel.getNGram(N, sb.toString());
                    NGram ngPrev = NGramModel.getNGram(N, sbPrev.toString());

                    ng.merge(ngPrev);

                    ng.calculateProbabilities();

                    float probability = ng.getProbability(candidate);

                    while(probability == 0f){
                        candidate = candidate.substring(candidate.indexOf(" "));
                        probability = ng.getProbability(candidate) * 0.0001f;
                    }

//                    IntrinsicEvaluationRunType.this.perplexity.get(s).add((float)Math.pow(1f/probability,
//                            1/(float)N));
                    return (float)Math.pow(1f/probability, 1/(float)N);
                });

                float perplex = (float)perplexity.sum();


                csv.add("preplexity", "" + perplex);
                csv.add("cluster", "" + CLUSTERS);
                csv.add("N", "" + N);
                csv.add("model", s);
                csv.add("dataPool", Properties.INPUT[0]);

                csv.finalize();

                File f = new File("NuiMimicEvaluation.csv");

                if (!f.exists()){
                    f.createNewFile();
                    FileHandler.writeToFile(f, csv.getHeaders() + "\n");
                }

                FileHandler.appendToFile(f, csv.getValues() + "\n");

                App.out.println(s + " perplexity: " + perplex);

            } catch (Exception e) {
                e.printStackTrace(App.out);
            }
            return s;
        }).collect(Collectors.toList());

        return 0;
    }
}
