package com.sheffield.leapmotion;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sheffield.instrumenter.InstrumentationProperties;
import com.sheffield.instrumenter.analysis.ClassAnalyzer;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.BranchHit;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.LineHit;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by thomas on 04/05/2016.
 */
public class Properties extends InstrumentationProperties {

    /*
            Properties for Leap Motion Testing
     */
    @Parameter(key = "dataPoolDirectory", description = "Directory containing data pool", hasArgs = true, category = "Leap Motion Testing")
    public static String DIRECTORY = "C:/data/leap-motion/processed";

    @Parameter(key = "playbackFile", description = "File to playback (containing serialized ArrayList<com.leap.leapmotion.Frame> objects)", hasArgs = true, category = "Leap Motion Testing")
    public static String PLAYBACK_FILE = null;

    @Parameter(key = "framesPerSecond", description = "Number of frames to seed per second", hasArgs = true, category = "Leap Motion Testing")
    public static long FRAMES_PER_SECOND = 60;

    @Parameter(key = "switchTime", description = "Time for interpolation between frames", hasArgs = true, category = "Leap Motion Testing")
    public static int SWITCH_TIME = 500;

    @Parameter(key = "startDelayTime", description = "Delay Time before frames are seeded", hasArgs = true, category = "Leap Motion Testing")
    public static long DELAY_TIME = 2000;

    @Parameter(key = "maxLoadedFrames", description = "Frames to retain for com.leap.leapmotion.Frame.frame(int [0->maxLoadedFrames]) method", hasArgs = true, category = "Leap Motion Testing")
    public static int MAX_LOADED_FRAMES = 10;

    @Parameter(key = "runtime", description = "Time to testing application before exiting", hasArgs = true, category = "Leap Motion Testing")
    public static long RUNTIME = 600000;

    @Parameter(key = "currentRun", description = "Can be used for experiments to output the current run", hasArgs = true, category = "Leap Motion Testing")
    public static int CURRENT_RUN = 0;


    @Parameter(key = "gestureFiles", description = "semicolon (;) separated list of gesture files to use for frame generation", hasArgs = true, category = "Leap Motion Testing")
    public static String GESTURE_FILES_STRING = null;
    public static String[] GESTURE_FILES = {"invalid"}; //derived from GESTURE_FILE_STRING

    @Parameter(key = "visualise_data", description = "Displays the currently seeded data in a separate window.", hasArgs = false, category = "Leap Motion Testing")
    public static boolean VISUALISE_DATA = false;


    /*
        Properties for Leap Motion Instrumentation
    */
    @Parameter(key = "jar", description = "Jar to instrument", hasArgs = true, category = "Leap Motion Instrumentation")
    public static String JAR_UNDER_TEST = null;

    @Parameter(key = "cp", description = "Path to library files for application", hasArgs = true, category = "Leap Motion Instrumentation")
    public static String CLASS_PATH = "";

    @Parameter(key = "replace_fingers_method", description = "Replaces com.leap.leapmotion.FingerList.fingers() method with com.leap.leapmotion.FingerList.extended() [for older API versions]", hasArgs = false, category = "Leap Motion Instrumentation")
    public static boolean REPLACE_FINGERS_METHOD = true;

    @Parameter(key = "leave_leapmotion_alone", description = "Leave the Leap Motion API original", hasArgs = false, category = "Leap Motion Instrumentation")
    public static boolean LEAVE_LEAPMOTION_ALONE = false;


    public enum FrameSelectionStrategy {
        RANDOM, EUCLIDEAN, RANDOM_DISTANCE, N_GRAM, EMPTY, ADAPTIVE_RANDOM_DISTANCE, VQ, STATE_DEPENDANT, SINGLE_MODEL
    }

    @Parameter(key = "frameSelectionStrategy", description = "Leave the Leap Motion API original", hasArgs = false, category = "Leap Motion Instrumentation")
    public static FrameSelectionStrategy FRAME_SELECTION_STRATEGY = FrameSelectionStrategy.STATE_DEPENDANT;

    public void setOptions(CommandLine cmd) throws IllegalAccessException {
        for (String s : annotationMap.keySet()) {
            Parameter p = annotationMap.get(s);
            if (p.hasArgs()) {
                String value = cmd.getOptionValue(p.key());
                if (value != null) {
                    setParameter(p.key(), value);
                }
            } else {
                if (cmd.hasOption(p.key())) {
                    setParameter(p.key(), Boolean.toString(true));
                }
            }
        }
    }

    public void setOptions(String[] args) {
        try {
            Options options = new Options();

            for (String s : annotationMap.keySet()) {
                Parameter p = annotationMap.get(s);
                options.addOption(p.key(), p.hasArgs(), p.description());
            }

            CommandLineParser parser = new BasicParser();
            CommandLine cmd = parser.parse(options, args);

            setOptions(cmd);

            if (Properties.PLAYBACK_FILE == null) {
                File playback = new File("playback.sequence");
                if (playback.exists()) {
                    Properties.PLAYBACK_FILE = playback.getAbsolutePath();
                    App.out.println("- Found sequence file at: " + Properties.PLAYBACK_FILE);
                }
            }
            if (!App.IS_INSTRUMENTING) {
                Gson g = new Gson();
                File branches = new File("branches.csv");
                if (branches.getAbsoluteFile().exists()) {
                        String branchesString = FileHandler.readFile(branches);
                        Type mapType = new TypeToken<Map<Integer, Map<Integer, BranchHit>>>() {
                        }.getType();
                        ClassAnalyzer.setBranches((Map<Integer, Map<Integer, BranchHit>>) g.fromJson(branchesString, mapType));

                        App.out.println("- Found branches file at: " + branches.getAbsolutePath());
                }

                File linesFile = new File("lines.csv");
                if (linesFile.getAbsoluteFile().exists()) {
                    try {
                        String linesString = FileHandler.readFile(linesFile);

                        Type mapType = new TypeToken<Map<Integer, Map<Integer, LineHit>>>() {
                        }.getType();
                        ClassAnalyzer.setLines((Map<Integer, Map<Integer, LineHit>>) g.fromJson(linesString, mapType));

                        App.out.println("- Found lines file at: " + linesFile.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                File relatedFile = new File("related_classes.csv");
                if (relatedFile.getAbsoluteFile().exists()) {
                        String[] classes = FileHandler.readFile(relatedFile).split("\n");
                        ArrayList<ClassTracker> clas = new ArrayList<ClassTracker>(classes.length - 1);
                        for (int i = 1; i < classes.length; i++) {
                            if (classes[i].length() > 0) {
                                String[] clInfo = classes[i].split(",");
                                int lines = Integer.parseInt(clInfo[1]);
                                App.relatedLines += lines;
                                int brans = Integer.parseInt(clInfo[2]);
                                App.relatedBranches += brans;
                                clas.add(new ClassTracker(clInfo[0], lines, brans));
                            }
                        }
                        App.relatedClasses = clas;

                        App.out.println("- Found related classes file at: " + linesFile.getAbsolutePath());
                        App.out.println("[" + App.relatedLines + " related lines, " + App.relatedBranches + " related branches]");
                }
            }
            if (Properties.GESTURE_FILES_STRING != null) {
                Properties.GESTURE_FILES = Properties.GESTURE_FILES_STRING.split(";");
            }

        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace(App.out);
        }
    }

    private static Properties instance;

    public static Properties instance() {
        if (instance == null) {
            instance = new Properties();
            for (String s : instance.getParameterNames()) {
                App.out.println(s);
            }
        }
        return instance;
    }
}
