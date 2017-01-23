package com.sheffield.leapmotion;

import com.sheffield.instrumenter.analysis.ClassAnalyzer;
import com.sheffield.instrumenter.analysis.DependencyTree;
import com.sheffield.instrumenter.analysis.ThrowableListener;
import com.sheffield.instrumenter.instrumentation.ClassReplacementTransformer;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.Branch;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.BranchHit;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.Line;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.LineHit;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.display.DisplayWindow;
import com.sheffield.leapmotion.frame.analyzer.AnalyzerApp;
import com.sheffield.leapmotion.frame.analyzer.StateIsolatedAnalyzerApp;
import com.sheffield.leapmotion.instrumentation.MockSystem;
import com.sheffield.leapmotion.output.StateComparator;
import com.sheffield.leapmotion.output.TestingStateComparator;
import com.sheffield.leapmotion.runtypes.InstrumentingRunType;
import com.sheffield.leapmotion.runtypes.ModelGeneratingRunType;
import com.sheffield.leapmotion.runtypes.ReconstructingRunType;
import com.sheffield.leapmotion.runtypes.RunType;
import com.sheffield.leapmotion.runtypes.StateRecognisingRunType;
import com.sheffield.leapmotion.runtypes.VisualisingRunType;
import com.sheffield.leapmotion.runtypes.agent.LeapmotionAgentTransformer;
import com.sheffield.leapmotion.runtypes.state_identification.ImageStateIdentifier;
import com.sheffield.leapmotion.util.AppStatus;
import com.sheffield.leapmotion.util.ClassTracker;
import com.sheffield.leapmotion.util.FileHandler;
import com.sheffield.leapmotion.util.ProgressBar;
import com.sheffield.leapmotion.util.Tickable;
import com.sheffield.output.Csv;

import javax.imageio.ImageIO;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.security.Permission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class App implements ThrowableListener, Tickable {
    public static Random random = new Random();
    public static App APP;
    public static boolean CLOSING = false;
    public static boolean RECORDING_STARTED = false;
    public static boolean ENABLE_APPLICATION_OUTPUT = false;
    public static boolean IS_INSTRUMENTING = false;
    public static int RECORDING_INTERVAL = 60000;

    private boolean testing = false;


    public static float LAST_LINE_COVERAGE = 0f;

    public static int relatedLines = 0;
    public static int relatedBranches = 0;

    public static ArrayList<ClassTracker> relatedClasses = new ArrayList<ClassTracker>();

    public static int timePassed = 0;

    //check states every x-ms
    public static final long STATE_CHECK_TIME = 10000;
    public long lastStateCheck = 0;
    private int framesSeeded = 0;
    private int fps = 0;
    private long lastFrameSeededCheck = 0;
    private int iterationTimes = 0;
    private int iterations = 0;

    ArrayList<String> classSeen = new ArrayList<String>();

    private static Thread mainThread = null;

    public static DisplayWindow DISPLAY_WINDOW = null;
    private static PrintStream originalOut = System.out;

    public interface TimeHandler {
        void setMillis(long executionTimeMillis);
        void setNanos(long executionTimeNanos);
    }

    public static TimeHandler TIME_HANDLER = new TimeHandler() {
        @Override
        public void setMillis(long executionTime) {
            MockSystem.MILLIS = executionTime;
        }

        @Override
        public void setNanos(long executionTime) {
            MockSystem.NANOS = executionTime;
        }
    };


    public static PrintStream out = new PrintStream(originalOut) {
        @Override
        public void println(String s) {
            if (s == null) {
                return;
            }
            String[] strs = s.split("\n");
            for (int i = 1; i < strs.length; i++) {
                println(strs[i]);
            }

            //Display.getDisplay().addCommand(strs[0]);
            originalOut.println(strs[0]);
        }

        @Override
        public void print(String s) {
            //Display.getDisplay().addCommand(s);
            originalOut.print(s);
        }
    };

    private AppStatus status;
    private long startTime;
    private long lastSwitchTime;
    private long timeBetweenSwitch;

    @Override
    public void throwableThrown(Throwable t) {
        App.out.println("Throwable thrown! " + t.getLocalizedMessage());
        File classes = FileHandler.generateTestingOutputFile("RUN" + Properties.CURRENT_RUN + "-test-results.errors");
        if (classes.getParentFile() != null) {
            classes.getParentFile().mkdirs();
        }
        if (!classes.exists()) {
            try {
                classes.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String output = "";

        for (StackTraceElement ste : t.getStackTrace()) {
            output += ste.getClassName() + "::" + ste.getMethodName() + "#" + ste.getLineNumber() + "\n";
        }
        try {
            FileHandler.appendToFile(classes, output);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //output(true);
    }

    public static class ExitException extends SecurityException {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        public final int status;

        public ExitException(int status) {
            super("Program tried to exit!");
            this.status = status;
        }
    }

    private static class NoExitSecurityManager extends SecurityManager {

        @Override
        public void checkPermission(Permission perm) {
            if (perm.getName().contains("loadLibrary")) {
                if (perm.getName().contains("LeapJava")) {
                    App.out.println("- LeapJava loaded dynamically.");
                    //App.getApp().setStatus(AppStatus.TESTING);
                    // new Exception().printStackTrace(App.out);
                    // throw new IllegalStateException("NO LOAD");
                    // throw new SecurityException("Cannot load LeapLibrary");
                }
            }

            if (perm.getName().toLowerCase().contains("fullscreen")) {
                throw new SecurityException("NO!");
            }
        }

        @Override
        public void checkPermission(Permission perm, Object context) {
        }

        @Override
        public void checkExit(int status) {
            if (CLOSING) {
                super.checkExit(status);
            } //else if (RECORDING_STARTED) {
//                App.getApp().end();
//                while (App.getApp().status() != AppStatus.CLOSING) {
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                }
//                //App.getApp().output(true);
//                super.checkExit(status);
//            }
        }
    }

    public AppStatus status() {
        return status;
    }

    public void setStatus(AppStatus status) {
        this.status = status;
    }

    private App() {
        status = AppStatus.SETUP;

    }

    private static boolean SETUP = false;

    public static void setTesting() {
        if (!SETUP) {
            SETUP = true;
            App.out.println("- Status changed to: Testing.");
            background(null);
            App.getApp().setStatus(AppStatus.TESTING);


        }
    }

    public static void startTesting() {
        App.out.println("- Testing Entry Point Triggered.");
        App.getApp().setTesting();
        //background(null);
    }

    public void setup(boolean initialiseForTesting) {
        testing = initialiseForTesting;
        if (testing) {
            App.getApp().setTesting();
        }
        if (Properties.VISUALISE_DATA && DISPLAY_WINDOW == null) {
            DISPLAY_WINDOW = new DisplayWindow();
        }
        File f = null;
        int testIndex = 0;
        while (f == null || f.exists()) {
            testIndex++;
            f = FileHandler.generateTestingOutputFile("RUN" + testIndex + "-test-results");
            if (f.getParentFile() != null && !f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }
        }
        Properties.CURRENT_RUN = System.currentTimeMillis();

        ClassAnalyzer.addThrowableListener(new ThrowableListener() {
            @Override
            public void throwableThrown(Throwable t) {
                t.printStackTrace(App.out);
            }
        });

        startTime = System.nanoTime();
        lastSwitchTime = startTime;
        timeBetweenSwitch = 1000 / Properties.FRAMES_PER_SECOND;
        System.setSecurityManager(new NoExitSecurityManager());
        App.out.println("- Setup Complete");

        setOutput();

        if (testing) {
            startTesting();
        }
    }

    private static boolean outputSet = false;

    public static void setOutput(){
        if (!ENABLE_APPLICATION_OUTPUT && !outputSet) {
            PrintStream dummyStream = new PrintStream(new OutputStream() {

                @Override
                public void write(int b) throws IOException {
                    // TODO Auto-generated method stub
                    //App.out.write(b);
                }

            }, true);

            out = System.out;

            System.setOut(dummyStream);

//            System.setErr(new PrintStream(new OutputStream() {
//                @Override
//                public void write(int b) throws IOException {
//
//                    FileOutputStream fos = new FileOutputStream(new File(Properties.TESTING_OUTPUT +
//                            "/err/" + Properties.CURRENT_RUN + "error.log"
//                    ));
//
//                    fos.write(b);
//                }
//            }));

            outputSet = true;
        }
    }

    public static App getApp() {
        if (APP == null) {
            APP = new App();
        }
        return APP;
    }

    public static void main(String[] args) {
        for (String s : args) {
            App.out.print(s + " ");
        }
        App.out.println(".");
        Properties.instance().setOptions(args);

        RunType run = null;

        switch (Properties.RUN_TYPE) {
            case INSTRUMENT:
                run = new InstrumentingRunType();
                break;
            case VISUALISE:
                run = new VisualisingRunType();
                break;
            case RECONSTRUCT:
                App.getApp().setup(false);
                run = new ReconstructingRunType();
                break;
            case STATE_RECOGNITION:
                //INPUT should contain an array of histograms.
                ImageStateIdentifier isi = new ImageStateIdentifier() {
                    @Override
                    public int identifyImage(BufferedImage bi,
                                             HashMap<Integer, BufferedImage> seenStates) {
                        StateComparator.captureState(bi);

                        return StateComparator.getCurrentState();
                    }

                    @Override
                    public String getOutputFilename() {
                        return "automatic_recognition.csv";
                    }
                };
                run = new StateRecognisingRunType(isi);
                break;
            case MANUAL_STATE_RECOGNITION:
                //INPUT should contain an array of histograms.
                final Scanner sc = new Scanner(System.in);

                ImageStateIdentifier isiMan = new ImageStateIdentifier() {
                    @Override
                    public int identifyImage(BufferedImage bi, HashMap<Integer, BufferedImage> seenStates) {
                        return sc.nextInt();
                    }

                    @Override
                    public String getOutputFilename() {
                        return "manual_recognition.csv";
                    }
                };
                run = new StateRecognisingRunType(isiMan);
                break;
            case MODEL_GEN_RUNTYPE:
                Properties.RUNTIME = Long.MAX_VALUE;
                Properties.PROCESS_PLAYBACK = true;
                App.DISABLE_BACKGROUND_THREAD = true;

                App.getApp().setup(false);
                run = new ModelGeneratingRunType();
                break;
            default:
                App.out.println("Unimplemented MILLIS");
                break;
        }
        run.run();
        System.exit(0);
    }

    /**
     * Premain that will be triggered when application runs with this
     * attached as a Java agent.
     * @param arg runtime properties to change
     * @param instr Instrumentation instance to attach a ClassFileTransformer
     */
    public static void premain (String arg, Instrumentation instr){
        LeapmotionAgentTransformer lat = new LeapmotionAgentTransformer();

        App.out.println("- Instrumenting JAR");

        String args[] = null;

        if (arg != null && arg.trim().length() > 0){
            args = arg.split(" ");
        }

        if (args != null && args.length > 0) {
            Properties.instance().setOptions(args);
            App.out.println("Options setup");
        } else {
            File options = new File("testing-options");
            if (options.getAbsoluteFile().exists()) {
                try {
                    String opts = FileHandler.readFile(options).trim();

                    App.out.println("- Found options: " + opts);

                    Properties.instance().setOptions(opts.split(" "));

                } catch (IOException e) {
                    Properties.instance().setOptions(new String[]{});
                    e.printStackTrace(App.out);
                }
            } else {
                Properties.instance().setOptions(new String[]{});
            }
        }

        for (String s : Properties.FORBIDDEN_PACKAGES) {
            ClassReplacementTransformer.addForbiddenPackage(s);
        }

        instr.addTransformer(lat);
    }

    public static long START_TIME = 0;

    public static boolean DISABLE_BACKGROUND_THREAD = false;

    public static void background(String[] args) {

        Properties.INSTRUMENTATION_APPROACH = Properties.InstrumentationApproach.ARRAY;
        Properties.USE_CHANGED_FLAG = true;
        Properties.LOG = false;

        if (DISABLE_BACKGROUND_THREAD){
            return;
        }

        if (mainThread != null) {
            App.out.println("Found thread already running!");
            return;
        }

        if (args != null && args.length > 0) {
            Properties.instance().setOptions(args);
            App.out.println("Options setup");
        } else {
            File options = new File("testing-options");
            if (options.getAbsoluteFile().exists()) {
                try {
                    String opts = FileHandler.readFile(options).trim();

                    App.out.println("- Found options: " + opts);

                    Properties.instance().setOptions(opts.split(" "));

                } catch (IOException e) {
                    Properties.instance().setOptions(new String[]{});
                    e.printStackTrace(App.out);
                }
            } else {
                Properties.instance().setOptions(new String[]{});
            }
        }
        App.out.println("- Starting background testing thread.");
        mainThread = getMainThread();
        mainThread.start();

    }

    public void increaseIterationTime(int t){
        iterationTimes += t;
        iterations++;
    }

    public float getAverageIterationTime(){
        return iterationTimes / (float) iterations;
    }

    public void increaseFps(long time){
        if (1000 < time - lastFrameSeededCheck){ // 1 second has passed?
            lastFrameSeededCheck = time;
            fps = framesSeeded;
            framesSeeded = 0;
        }
        framesSeeded++;
    }

    public float getFps(){
        return fps;
    }

    public static Thread getMainThread(){
        return new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub

                App app = App.getApp();
                app.start();
                App.out.println("- Starting Frame Seeding");
                int delay = (int) (1000f / Properties.FRAMES_PER_SECOND);

                try {
                    Thread.sleep(Properties.DELAY_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                long lastTime = System.nanoTime();
                START_TIME = lastTime;
                long lastTimeRecorded = 0;

                while (app.status() != AppStatus.FINISHED) {
                    delay = (int) (1000f / Properties.FRAMES_PER_SECOND);
                    long time = System.nanoTime();
                    int timePassed = (int) ((time - lastTime)/ 1000000);
                    App.getApp().increaseIterationTime(timePassed);
                    App.getApp().increaseFps(time/1000000);
                    app.tick(time/1000000);
                    try {
                        int d = delay - timePassed;
                        if (d >= 0) {
                            Thread.sleep(d);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if ((lastTime - lastTimeRecorded)/1000000 >=
                            RECORDING_INTERVAL &&
                            SeededController.getSeededController()
                                    .allowProcessing() && !Properties
                            .PROCESS_PLAYBACK && !Properties.PROCESS_SCREENSHOTS) {
                        ClassAnalyzer.collectHitCounters(false);

                        App.getApp().output(false);
                        lastTimeRecorded = lastTime;
                    }

                    long timePassedNanos = time - START_TIME;

                    TIME_HANDLER.setMillis(timePassedNanos / 1000000);
                    TIME_HANDLER.setNanos(timePassedNanos);

                    lastTime = time;
                }
                App.out.println("- Gathering Testing Information...");
                ClassAnalyzer.collectHitCounters(false);
                long timePassedNanos = System.nanoTime() - START_TIME;

                TIME_HANDLER.setMillis(timePassedNanos / 1000000);
                TIME_HANDLER.setNanos(timePassedNanos);

                App.getApp().output(true);
                System.exit(0);

            }

        });
    }

    public void output(boolean finished) {
        if (finished) {
            cleanUp();
        }
        try {
            ClassAnalyzer.setOut(App.out);

            outputLineAndBranchHits();

            String gestureFiles = "";
            for (String s : Properties.INPUT) {
                gestureFiles += s + ";";
            }

            if (gestureFiles.length() > 0)
                gestureFiles.substring(0, gestureFiles.length() - 1);

            LAST_LINE_COVERAGE = Math.round((ClassAnalyzer.getLineCoverage() * 100f)) / 100f;
            int states = StateComparator.statesVisited.size();


            Csv testingValues = ClassAnalyzer.toCsv();

            Csv propertyValues = Properties.instance().toCsv();
            Csv csv = new Csv();


            csv.merge(testingValues);
            csv.merge(propertyValues);

            csv.add("statesStarting", "" + (StateComparator.statesVisited.size() - StateComparator.statesFound));
            csv.add("statesFound", "" + StateComparator.statesFound);
            csv.add("statesVisited", "" + StateComparator.getStatesVisited().size());
            csv.add("currentState", "" + StateComparator.getCurrentState());

            csv.add("fps", "" + getFps());
            csv.add("iterationTime", "" + getAverageIterationTime());
            csv.add("iterations", "" + iterations);

            csv.add("TstatesStarting", "" + (TestingStateComparator.statesVisited.size() - StateComparator.statesFound));
            csv.add("TstatesFound", "" + TestingStateComparator.statesFound);
            csv.add("TstatesVisited", "" + TestingStateComparator.getStatesVisited()
                    .size());
            csv.add("TcurrentState", "" + TestingStateComparator.getCurrentState());

            Csv fromFrameSelector = SeededController.getSeededController()
                    .getCsv();

            csv.merge(fromFrameSelector);

            int lineHits = 0;
            int branchHits = 0;

            if (relatedClasses.size() > 0) {
                HashMap<String, ArrayList<String>> relClas = new HashMap<String, ArrayList<String>>();
                for (ClassTracker ct : relatedClasses) {
                    String className = DependencyTree.getClassName(ct.getClassName());
                    String methodName = DependencyTree.getMethodName(ct.getClassName());

                    if (!relClas.containsKey(className)){
                        relClas.put(className, new ArrayList<String>());
                    }

                    relClas.get(className).add(methodName);
                }

                for (String ct : relClas.keySet()){
                    List<Line> lines = ClassAnalyzer.
                            getCoverableLines(ct, relClas.get(ct));
                    for (Line l : lines) {
                        if (l.getHits() > 0) {
                            lineHits++;
                        }
                    }
                    List<Branch> branches = ClassAnalyzer.getCoverableBranches(ct, relClas.get(ct));
                    for (Branch b : branches) {
                        if (b.getFalseHits() > 0) {
                            branchHits++;
                        }

                        if (b.getTrueHits() > 0) {
                            branchHits++;
                        }
                    }
                }
            }

            csv.add("relatedLinesTotal", "" + relatedLines);
            csv.add("relatedLinesCovered", "" + lineHits);
            csv.add("relatedLineCoverage", "" + (lineHits / (float) relatedLines));

            csv.add("relatedBranchesTotal", "" + relatedBranches);
            csv.add("relatedBranchesCovered", "" + branchHits);
            csv.add("relatedBranchCoverage", "" + (branchHits / (float) relatedBranches));
            csv.add("runtime", "" + MockSystem.MILLIS);

            if (Properties.FRAME_SELECTION_STRATEGY.equals(Properties.FrameSelectionStrategy.STATE_ISOLATED)){
                csv.add("dataHitRatio", "" + StateIsolatedAnalyzerApp.hitRatio());
            } else {
                csv.add("dataHitRatio", "" + AnalyzerApp.hitRatio());
            }

            csv.finalize();
            output(csv);
        } catch (IOException e) {
            e.printStackTrace(App.out);
        }
    }

    public void output(Csv csv) {

        File csvFile = FileHandler.generateTestingOutputFile("RUN" + Properties.CURRENT_RUN + "-test-results");
        if (csvFile.getParentFile() != null) {
            csvFile.getParentFile().mkdirs();
        }
        try {
            boolean newFile = !csvFile.getAbsoluteFile().exists();
            String contents = "";

            if (newFile) {
                csvFile.createNewFile();
                contents += csv.getHeaders() + "\n";
            }

            contents += csv.getValues() + "\n";

            FileHandler.appendToFile(csvFile, contents);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cleanUp(){
        App.out.println("- Finished testing: ");
        App.out.println("@ Coverage Report: ");
        App.out.println(ClassAnalyzer.getReport());
        if (Properties.VISUALISE_DATA)
            App.DISPLAY_WINDOW.setVisible(false);

        BufferedImage bi = StateComparator.screenshot();

        File outFldr = new File(Properties.TESTING_OUTPUT + "/result_states");
        outFldr.mkdirs();

        File output = new File(outFldr, Properties.FRAME_SELECTION_STRATEGY + "-RUN-" + Properties.CURRENT_RUN + "-" + System.currentTimeMillis() + "-" + Properties.INPUT[0] + "-" + Properties.RUNTIME + "ms.png");
        try {
            ImageIO.write(bi, "png", output);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (Properties.VISUALISE_DATA)
            App.DISPLAY_WINDOW.dispatchEvent(new WindowEvent(App.DISPLAY_WINDOW, WindowEvent.WINDOW_CLOSING));
    }

    public void outputLineAndBranchHits() throws IOException {
        StringBuilder linesHit = new StringBuilder();
        ArrayList<LineHit> linesCovered = ClassAnalyzer.getLinesCovered();
        for (LineHit lh : ClassAnalyzer.getTotalLines()) {

            String className = lh.getLine().getClassName();

            if (classSeen.contains(className)) {
                className = "" + classSeen.indexOf(className);
            } else {
                File classes = FileHandler.generateTestingOutputFile("RUN" + Properties.CURRENT_RUN + "-test-results.classes");

                if (classes.getParentFile() != null) {
                    classes.getParentFile().mkdirs();
                }
                if (!classes.exists()) {
                    classes.createNewFile();
                }
                classSeen.add(className);
                FileHandler.appendToFile(classes, className + ":");
                className = "" + classSeen.indexOf(className);
                FileHandler.appendToFile(classes, className + "\n");
            }

            if (linesCovered.contains(lh)) {
                linesHit.append(className + "#" + lh.getLine().getLineNumber() + ";");
            }
        }

        StringBuilder branchesHit = new StringBuilder();
        List<BranchHit> branchesCovered = ClassAnalyzer.getBranchesExecuted();
        for (BranchHit lh : branchesCovered) {
            String className = lh.getBranch().getClassName();

            if (classSeen.contains(className)) {
                className = "" + classSeen.indexOf(className);
            } else {
                File classes = FileHandler.generateTestingOutputFile("RUN" + Properties.CURRENT_RUN + "-test-results.classes");
                if (classes.getParentFile() != null) {
                    classes.getParentFile().mkdirs();
                }
                if (!classes.exists()) {
                    classes.createNewFile();
                }
                classSeen.add(className);
                FileHandler.appendToFile(classes, className + ":");
                className = "" + classSeen.indexOf(className);
                FileHandler.appendToFile(classes, className + "\n");
            }
            branchesHit.append(className + "#" + lh.getBranch().getLineNumber() + ";");
        }

        File classes = FileHandler.generateTestingOutputFile("RUN" + Properties.CURRENT_RUN + "-test-results.lines_covered");
        if (classes.getParentFile() != null) {
            classes.getParentFile().mkdirs();
        }
        if (!classes.exists()) {
            classes.createNewFile();
        }

        FileHandler.appendToFile(classes, linesHit.toString() + "\n");

        File branches = FileHandler.generateTestingOutputFile("RUN" + Properties.CURRENT_RUN + "-test-results.branches_covered");
        if (branches.getParentFile() != null) {
            branches.getParentFile().mkdirs();
        }
        if (!branches.exists()) {
            branches.createNewFile();
        }

        FileHandler.appendToFile(branches, branchesHit.toString() + "\n");
    }

    public void start() {
        startTime = System.nanoTime();
    }


    private long lastUpdate = 0;

    private boolean printHeaders = true;

    public void tick(long time) {
        timeBetweenSwitch = 1000 / Properties.FRAMES_PER_SECOND;
        SeededController sc = SeededController.getSeededController();
        //if (time - lastSwitchTime > timeBetweenSwitch) {

        if (sc.lastTick() < time) {
            sc.tick(time);
            lastSwitchTime = time;
        }
        //}

        if (time - lastStateCheck > STATE_CHECK_TIME &&
        SeededController.getSeededController().allowProcessing()) {
            try {
                StateComparator.captureState();
                lastStateCheck = time;
            } catch (Exception e) {
                e.printStackTrace(App.out);
            }

        }

        if (printHeaders){
            App.out.println(ProgressBar.getHeaderBar(21));
            printHeaders = false;
        }


        long start = (startTime/1000000);
        long timePassed = time - start;

        String progress = ProgressBar.getProgressBar(21, getProgress());

        out.print("\r" + progress + ". Cov: " + LAST_LINE_COVERAGE + ". " + SeededController.getSeededController().status());

        if (timePassed > Properties.RUNTIME) {
            App.out.println(time + " - " + start + " = " + timePassed + "\n"
                    + timePassed + " > " + Properties.RUNTIME);
            status = AppStatus.FINISHED;
        }
    }

    public float getProgress(){
        return timePassed / (float) Properties.RUNTIME;
    }

    public long lastTick() {
        return lastUpdate;
    }

    public void end() {
        status = AppStatus.FINISHED;
    }

    public void close() {
        status = AppStatus.CLOSING;
    }
}
