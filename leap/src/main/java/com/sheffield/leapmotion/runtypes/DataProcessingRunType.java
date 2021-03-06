package com.sheffield.leapmotion.runtypes;

import com.google.gson.Gson;
import com.leapmotion.leap.Gesture;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.frame.analyzer.machinelearning.clustering.ClusterResult;
import com.sheffield.leapmotion.frame.analyzer.machinelearning.clustering.WekaClusterer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.sheffield.leapmotion.frame.analyzer.machinelearning.ngram.NGram;
import com.sheffield.leapmotion.frame.analyzer.machinelearning.ngram.NGramModel;
import com.sheffield.leapmotion.output.StateComparator;
import com.sheffield.leapmotion.util.FileHandler;
import weka.core.Instance;

import static com.sheffield.leapmotion.Properties.DIRECTORY;
import static com.sheffield.leapmotion.Properties.INPUT;

/**
 * Created by thomas on 08/02/17.
 */
public class DataProcessingRunType implements RunType {

    public static final int N = 3;

    @Override
    public int run() {

//        DataRawProcessingRunType drpr = new DataRawProcessingRunType();
//
//        drpr.run();

        Gson gson = new Gson();

        HashMap<String, String> files = new HashMap<String, String>();

        files.put("joint_positions_pool.ARFF", "joint_position");
        files.put("hand_positions_pool.ARFF", "hand_position");
        files.put("hand_rotations_pool.ARFF", "hand_rotation");
        files.put("gesture_circle_pool.ARFF", "gesture_circle");
        files.put("gesture_keytap_pool.ARFF", "gesture_keytap");
        files.put("gesture_screentap_pool.ARFF", "gesture_screentap");
        files.put("gesture_swipe_pool.ARFF", "gesture_swipe");
        files.put("stabilised_tip_pool.ARFF", "stabilised_tip");
        files.put("featureless_hand_pool.ARFF", "feature_hand");

        HashMap<String, ClusterResult> results = new HashMap<String, ClusterResult>();

        if (Properties.DIRECTORY.toLowerCase().endsWith("/processed")) {
            Properties.DIRECTORY = Properties.DIRECTORY.substring(0, Properties.DIRECTORY.lastIndexOf("/"));
        }
        String dataDir = Properties.DIRECTORY + "/" + Properties.INPUT[0];
        String originDir = dataDir;
        List<String> trainingFiles = new ArrayList<>();

        for (int i = 1; i < INPUT.length; i++){
            trainingFiles.add(DIRECTORY + "/" + INPUT[i]);
        }

        if (trainingFiles.size() > 0 && Properties.INPUT[0].contains("-")){
            dataDir = Properties.DIRECTORY + "/" +
                    Properties.INPUT[0].substring(1 + Properties.INPUT[0]
                            .indexOf(
                            "-"
                    ));
        }

        File ddir = new File(dataDir);

        if (!ddir.exists()){
            ddir.mkdirs();
        }



        HashMap<Integer, String> stateSequences = new HashMap<Integer, String>();

        HashMap<Integer, Integer[]> rawStates = new HashMap<Integer, Integer[]>();

        try {
            ArrayList<String> allLines = new ArrayList<>();

            String[] orig = FileHandler.readFile(new File(originDir +
                    "/dct_pool")).split("\n");

            for (String o : orig){
                allLines.add(o);
            }

            for (String s : trainingFiles){
                String[] l = FileHandler.readFile(new File
                        (s +
                                "/dct_pool"))
                        .split("\n");
                for (String li : l){
                    allLines.add(li);
                }
            }

            String[] lines = new String[allLines.size()];

            allLines.toArray(lines);

            for (String s : lines){
                if (s.trim().length() == 0 || !s.contains(":")){
                    continue;
                }
                String[] elements = s.split(":");
                if (elements.length < 2){
                    continue;
                }
                String state = elements[0];
                String candidates = elements[1];

                String[] stateElements = state.split(",");

                Integer[] stateHist = new Integer[stateElements.length];

                try {
                    for (int i = 0; i < stateHist.length; i++) {
                        stateHist[i] = Integer.parseInt(stateElements[i]);
                    }
                } catch (NumberFormatException e){
                    App.out.println(e);
                    continue;
                }

                Integer currentState = StateComparator.addState(stateHist);
                if (!stateSequences.containsKey(currentState)){
                    stateSequences.put(currentState, candidates);
                } else {
                    stateSequences.put(currentState, stateSequences.get(currentState) + " " + candidates);
                }

                if (!rawStates.containsKey(currentState)){
                    rawStates.put(currentState, stateHist);
                }
            }
        } catch (IOException e) {
            e.printStackTrace(App.out);
            System.exit(-1);
        }

        for (String s : files.keySet()) {

            App.out.println("\r" + s);

            String joints = originDir + "/" + s;
            List<String> fs = new ArrayList<>();

            for (String tf : trainingFiles){
                fs.add(tf + "/" + s);
            }
            try {

                WekaClusterer wc = new WekaClusterer(joints, fs);

                ClusterResult cr = wc.cluster();

                results.put(s, cr);

                HashMap<String, String> assignments = cr.getAssignments();
                HashMap<String, Instance> centroids = cr.getCentroids();

                ArrayList<String> keysMod = new ArrayList<>();

                keysMod.addAll(assignments.keySet());

                ArrayList<String> keys = new ArrayList<>();

                for (int i = 0; i < keysMod.size(); i++){
                    keys.add(keysMod.get(i).replace(s, ""));
                }

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
                File outputSequence = new File(dataDir + "/processed/"  + Properties.CLUSTERS + "-" + Properties.N + "/" + files.get(s) + ".raw_sequence");

                if (!outputSequence.exists()){
                    if (outputSequence.getParentFile() != null && !outputSequence.getParentFile().exists()){
                        outputSequence.getParentFile().mkdirs();
                    }
                    outputSequence.createNewFile();
                }

                FileHandler.writeToFile(outputSequence, "");

                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < clusterOrder.size()-1; i++){
                    sb.append(clusterOrder.get(i) + " ");
                }
                FileHandler.appendToFile(outputSequence, sb.toString()
                        .replace(" ", ","));


                HashMap<Integer, NGram> stateNgrams = new HashMap<Integer, NGram>();

                for (Integer state : stateSequences.keySet()){
                    String[] stateInfo = stateSequences.get(state).split(",");

                    String replacedSequence = "";

                    for (String candidate : stateInfo){
                        candidate = candidate.trim();
                        if (candidate.length() > 0){

                            int index = keys.indexOf(candidate);

                            if (index < 0){
                                //throw new IllegalArgumentException("Candidate " + candidate + " not found!");
                                continue;
                            }

                            replacedSequence += " " + clusterOrder.get(index);
                        }
                    }

                    NGram stateGram = NGramModel.getNGram(N, replacedSequence);

                    stateGram.calculateProbabilities();

                    stateNgrams.put(state, stateGram);
                }



                FileHandler.appendToFile(outputSequence, clusterOrder.get
                        (clusterOrder.size()-1) + "\n");

                StringBuilder keyB = new StringBuilder();

                for (int i = 0; i < keys.size()-1; i++){
                    keyB.append(keys.get(i) + ",");
                }
                FileHandler.appendToFile(outputSequence, keyB.toString());

                FileHandler.appendToFile(outputSequence, keys.get(keys.size()-1));

                File outputClusters = new File(dataDir + "/processed/" + Properties.CLUSTERS + "-" + Properties.N + "/" + files.get(s) + "_data");

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


                NGram ng = NGramModel.getNGram(N, sb.toString());

                ng.calculateProbabilities();

                File ngramOutput = new File(dataDir + "/processed/" + Properties.CLUSTERS + "-" + Properties.N + "/" + files.get(s) + "_ngram");

                if (!ngramOutput.exists()){
                    if (ngramOutput.getParentFile() != null && !ngramOutput.getParentFile().exists()){
                        ngramOutput.getParentFile().mkdirs();
                    }
                    ngramOutput.createNewFile();
                }

                FileHandler.writeToFile(ngramOutput, gson.toJson(ng));

                stateNgrams.put(-1, ng);

                FileHandler.writeToFile(new File(dataDir + "/processed/" + Properties.CLUSTERS + "-" + Properties.N + "/" + files.get(s) + "_stategram"), gson.toJson(stateNgrams));

            } catch (Exception e) {
                e.printStackTrace(App.out);
            }
        }

        try {
            NGram gestureNgram = null;
            HashMap<String, String> handGestures = new HashMap<>();

            String gestureString = FileHandler.readFile(new File(originDir +
                    "/sequence_gesture_data.csv"));

            for (String gs : trainingFiles){
                String ng = FileHandler.readFile(new File(gs +
                        "/sequence_gesture_data.csv"));
                gestureString += " " + ng;
            }

            StringBuilder newSentence = new StringBuilder();

            String[] gestures = gestureString.split(" ");

            for (String s : gestures){
                if (!s.equals(Gesture.Type.TYPE_INVALID.toString()) && s.contains("::")){

                    String[] split =s.split("::");

                    String id = split[0];

                    String gest = s.replace(id + "::", "");

                    newSentence.append(gest);
                    newSentence.append(" ");

                    handGestures.put(id, gest);
                } else {
                    newSentence.append(s);
                    newSentence.append(" ");
                }
            }

            String gestString = newSentence.toString();
            gestureNgram = NGramModel.getNGram(N, gestString);

            gestureNgram.calculateProbabilities();

            File outputSequence = new File(dataDir + "/processed/" + + Properties.CLUSTERS + "-" + Properties.N + "/gesture_type_ngram");

            if (!outputSequence.exists()){
                if (outputSequence.getParentFile() != null && !outputSequence.getParentFile().exists()){
                    outputSequence.getParentFile().mkdirs();
                }
                outputSequence.createNewFile();
            }

            FileHandler.writeToFile(outputSequence, gson.toJson(gestureNgram));

            String rawGestureString = dataDir + "/processed/" + Properties.CLUSTERS + "-" + Properties.N + "/gesture_type_data.raw_sequence";

            FileHandler.writeToFile(new File(rawGestureString), gestString);


            String rawStatesFile = dataDir + "/processed/" + Properties.CLUSTERS + "-" + Properties.N + "/raw_states";

            FileHandler.writeToFile(new File(rawStatesFile), gson.toJson(rawStates));

            HashMap<Integer, NGram> stateNgrams = new HashMap<Integer, NGram>();

            for (Integer state : stateSequences.keySet()){
                String[] stateInfo = stateSequences.get(state).split(",");

                String replacedSequence = "";

                for (String candidate : stateInfo){
                    candidate = candidate.trim();
                    if (candidate.length() > 0){

                        replacedSequence += " " + handGestures.get(candidate);
                    }
                }

                NGram stateGram = NGramModel.getNGram(N, replacedSequence);

                stateGram.calculateProbabilities();

                stateNgrams.put(state, stateGram);

            }

            File ngramOutput = new File(dataDir + "/processed/" + Properties.CLUSTERS + "-" + Properties.N + "/gesture_type_stategram");

            if (!ngramOutput.exists()){
                if (ngramOutput.getParentFile() != null && !ngramOutput.getParentFile().exists()){
                    ngramOutput.getParentFile().mkdirs();
                }
                ngramOutput.createNewFile();
            }

            stateNgrams.put(-1, gestureNgram);

            FileHandler.writeToFile(ngramOutput, gson.toJson(stateNgrams));
        } catch (IOException e) {
            e.printStackTrace();
            return 1;
        }


        return 0;
    }
}
