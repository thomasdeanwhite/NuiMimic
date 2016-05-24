package com.sheffield.leapmotion;

import com.sheffield.instrumenter.analysis.ClassAnalyzer;
import com.sheffield.instrumenter.analysis.ClassNode;
import com.sheffield.instrumenter.analysis.DependencyTree;
import com.sheffield.instrumenter.analysis.ThrowableListener;
import com.sheffield.instrumenter.instrumentation.ClassReplacementTransformer;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.Branch;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.Line;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.LineHit;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.display.DisplayWindow;
import com.sheffield.leapmotion.output.DctStateComparator;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.Permission;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class App implements ThrowableListener {
    public static Random random = new Random();
    public static App APP;
    public static boolean CLOSING = false;
    public static boolean RECORDING_STARTED = false;
    private static boolean ENABLE_APPLICATION_OUTPUT = false;
    public static boolean IS_INSTRUMENTING = false;
    public static int RECORDING_INTERVAL = 60000;


    public static float LAST_LINE_COVERAGE = 0f;

    public static int relatedLines = 0;
    public static int relatedBranches = 0;

    public static ArrayList<ClassTracker> relatedClasses = new ArrayList<ClassTracker>();

    public static int timePassed = 0;

    //check states every x-ms
    public static final long STATE_CHECK_TIME = 5000;
    public long lastStateCheck = 0;

    ArrayList<String> classSeen = new ArrayList<String>();

    private static Thread mainThread = null;

    public static DisplayWindow DISPLAY_WINDOW = null;
    private static PrintStream originalOut = System.out;

    public static PrintStream out = new PrintStream(originalOut) {
        @Override
        public void println(String s) {
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
        File classes = new File("testing_output/errors/RUN" + Properties.CURRENT_RUN + "-error.log");
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
        output(true, timePassed);
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
                    App.out.println("LeapJava loaded dynamically.");
                    App.getApp().setStatus(AppStatus.TESTING);
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
            } else if (RECORDING_STARTED) {
                App.getApp().end();
                while (App.getApp().status() != AppStatus.CLOSING) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                App.getApp().output(true, timePassed);
                super.checkExit(status);
            }
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

    public static void setTesting() {
        App.out.println("- Status changed to: Testing.");
        App.getApp().setStatus(AppStatus.TESTING);
        if (getApp() == null) {
            background(null);
        }
    }

    public static void startTesting() {
        App.out.println("- Testing Entry Point Triggered.");
        App.getApp().setStatus(AppStatus.TESTING);
        background(null);
    }

    public void setup() {
        if (Properties.VISUALISE_DATA) {
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
        Properties.CURRENT_RUN = testIndex;

        ClassAnalyzer.addThrowableListener(new ThrowableListener() {
            @Override
            public void throwableThrown(Throwable t) {
                t.printStackTrace(App.out);
            }
        });

        SeededController.getController();
        startTime = System.currentTimeMillis();
        lastSwitchTime = startTime;
        timeBetweenSwitch = 1000 / Properties.FRAMES_PER_SECOND;
        if (!ENABLE_APPLICATION_OUTPUT) {
            PrintStream dummyStream = new PrintStream(new OutputStream() {

                @Override
                public void write(int b) throws IOException {
                    // TODO Auto-generated method stub
                    //App.out.write(b);
                }

            }, true);

            out = System.out;

            System.setOut(dummyStream);
        }
        System.setSecurityManager(new NoExitSecurityManager());
        App.out.println("- Setup Complete");
    }

    public static App getApp() {
        if (APP == null) {
            APP = new App();
        }
        return APP;
    }

    public static void main(String[] args) {
        App.out.print("- Instrumenting JAR with options: ");
        String[] defaultHiddenPackages = new String[]{"com/sheffield/leapmotion", "com/google/gson",
                "com/leapmotion", "javax/", "org/json", "org/apache/commons/cli",
                "org/junit", "Launcher", "com/google", "org/apache", "net", "com/garg", "net/sourceforge",
                "org", "java", "com/steady", "com/thought"};
        for (String s : defaultHiddenPackages) {
            ClassReplacementTransformer.addForbiddenPackage(s);
        }
        ENABLE_APPLICATION_OUTPUT = true;
        IS_INSTRUMENTING = true;
        Properties.LOG = false;
        ClassAnalyzer.setOut(App.out);
        for (String s : args) {
            App.out.print(s + " ");
        }
        App.out.println(".");
        Properties.instance().setOptions(args);

        Properties.INSTRUMENTATION_APPROACH = Properties.InstrumentationApproach.ARRAY;
        try {
            LeapMotionApplicationHandler.instrumentJar(Properties.JAR_UNDER_TEST);
            String dir = Properties.JAR_UNDER_TEST.substring(0, Properties.JAR_UNDER_TEST.lastIndexOf("/") + 1);
            String output = dir + "branches.csv";
            String output2 = dir + "lines.csv";
            App.out.print("+ Writing output to: " + dir + " {branches.csv, lines.csv}");
            ClassAnalyzer.output(output, output2);
            App.out.println("\r+ Written output to: " + dir + " {branches.csv, lines.csv}");


            ArrayList<ClassNode> nodes = DependencyTree.getDependencyTree().getPackageNodes("com.leapmotion");
            HashSet<String> lines = new HashSet<String>();
            ArrayList<String> relatedClasses = new ArrayList<String>();
            for (ClassNode cn : nodes) {
                String[] link = cn.toNomnoml().split("\n");
                for (String s : link) {
                    lines.add(s);
                }
                String[] classes = cn.toString().split("\n");

                for (String s : classes) {
                    if (s.length() > 0) {
                        if (!s.contains("com.leapmotion.leap") && !relatedClasses.contains(s)) {
                            relatedClasses.add(s);
                            App.out.println(s);
                        }
                    }
                }
            }

            ArrayList<ClassNode> options = DependencyTree.getDependencyTree().getPackageNodes("JOptionsPane");

            for (ClassNode cn : options) {
                App.out.println("OPTIONS: " + cn.toNomnoml());
            }


            for (String s : lines) {
                App.out.println(s);
            }

            String related = dir + "related_classes.csv";

            File relatedFile = new File(related);

            if (!relatedFile.exists()) {
                relatedFile.createNewFile();
            }

            String classes = "";

            for (String c : relatedClasses) {
                if (c == null || c.length() == 0) {
                    continue;
                }
                classes += c + "," + ClassAnalyzer.getCoverableLines(c).size() + "," + ClassAnalyzer.getCoverableBranches(c).size() + "\n";
            }
            FileHandler.writeToFile(relatedFile, "class,lines,branches\n");
            FileHandler.appendToFile(relatedFile, classes);

        } catch (Exception e) {
            e.printStackTrace(App.out);
        }
    }

    public static void runAgent() {
        String command = "java -javaagent:lm-agent.jar -jar " + Properties.JAR_UNDER_TEST;
        App.out.print("Executing command: \n\t" + command);
        try {
            Process p = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void background(String[] args) {
        Properties.INSTRUMENTATION_APPROACH = Properties.InstrumentationApproach.ARRAY;
        Properties.USE_CHANGED_FLAG = true;
        Properties.LOG = false;

        if (mainThread != null) {
            App.out.println("Found thread already running!");
            return;
        }

        Properties.DIRECTORY += "/processed";

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
        mainThread = new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub

                App app = App.getApp();
                app.setup();
                app.start();
                App.out.println("- Starting Frame Seeding");
                int delay = (int) (1000f / Properties.FRAMES_PER_SECOND);

                try {
                    Thread.sleep(Properties.DELAY_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                long lastTime = System.currentTimeMillis();
                long startTime = lastTime;
                long lastTimeRecorded = 0;
                while (app.status() != AppStatus.FINISHED) {
                    int timePassed = (int) (System.currentTimeMillis() - lastTime);
                    app.tick();
                    try {
                        int d = delay - timePassed;
                        if (d >= 0) {
                            Thread.sleep(d);
                        }
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (lastTime - lastTimeRecorded >= RECORDING_INTERVAL) {
                        ClassAnalyzer.collectHitCounters(false);
                        App.getApp().output(false, (int) (System.currentTimeMillis() - startTime));
                        lastTimeRecorded = lastTime;
                    }
                    lastTime += timePassed;
                }
                App.out.println("- Gathering Testing Information...");
                ClassAnalyzer.collectHitCounters(false);
                timePassed = (int) (System.currentTimeMillis() - startTime);
                App.getApp().output(true, timePassed);
                System.exit(0);

            }

        });
        mainThread.start();

    }

    public void output(boolean finished, int runtime) {
        if (finished) {
            App.out.println("- Finished testing: ");
            App.out.println("@ Coverage Report: ");
            App.out.println(ClassAnalyzer.getReport());
            if (Properties.VISUALISE_DATA)
                App.DISPLAY_WINDOW.setVisible(false);

            BufferedImage bi = null;

            try {
                Robot robot = new Robot();
                bi = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            } catch (AWTException e) {
                e.printStackTrace();
            }

            File outFldr = new File("testing_output/result_states");
            outFldr.mkdirs();

            File output = new File(outFldr, "RUN-" + Properties.CURRENT_RUN + "-" + System.currentTimeMillis() + "-" + Properties.GESTURE_FILES[0] + "-" + Properties.RUNTIME + "ms.png");
            try {
                ImageIO.write(bi, "png", output);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (Properties.VISUALISE_DATA)
                App.DISPLAY_WINDOW.dispatchEvent(new WindowEvent(App.DISPLAY_WINDOW, WindowEvent.WINDOW_CLOSING));
        }
        File csv = new File("testing_output/logs/RUN" + Properties.CURRENT_RUN + "-test-results.csv");
        if (csv.getParentFile() != null) {
            csv.getParentFile().mkdirs();
        }
        try {
            boolean newFile = !csv.getAbsoluteFile().exists();
            if (newFile) {
                csv.createNewFile();
            }
            ClassAnalyzer.setOut(App.out);
            StringBuilder linesHit = new StringBuilder();
            ArrayList<LineHit> linesCovered = ClassAnalyzer.getLinesCovered();
            for (LineHit lh : linesCovered) {
                String className = lh.getLine().getClassName();
                if (classSeen.contains(className)) {
                    className = "" + classSeen.indexOf(className);
                } else {
                    File classes = new File("testing_output/logs/RUN" + Properties.CURRENT_RUN + "-test-results.classes.csv");
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
                linesHit.append(className + "#" + lh.getLine().getLineNumber() + ";");
            }

            String gestureFiles = "";
            for (String s : Properties.GESTURE_FILES) {
                gestureFiles += s + ";";
            }

            if (gestureFiles.length() > 0)
                gestureFiles.substring(0, gestureFiles.length()-1);


            String info = ClassAnalyzer.toCsv(newFile, runtime, "frame_selector,gesture_files,starting_states,states_found,states_discovered," +
                    "final_sate,related_lines_total,related_lines_covered,related_line_coverage," +
                    "related_branches_total,related_branches_covered,relate_branch_coverage,bezier_points,switch_time");
            LAST_LINE_COVERAGE = ClassAnalyzer.getLineCoverage();
            int states = DctStateComparator.statesVisited.size();
            info += "," + Properties.FRAME_SELECTION_STRATEGY + "," + gestureFiles + "," + (states - DctStateComparator.statesFound) + "," + DctStateComparator.statesFound + "," + DctStateComparator.getStatesVisited().size() + "," + DctStateComparator.getCurrentState();


            int lineHits = 0;
            int branchHits = 0;
            if (relatedClasses.size() > 0) {
                for (ClassTracker ct : relatedClasses) {
                    List<Line> lines = ClassAnalyzer.getCoverableLines(ct.className);
                    for (Line l : lines) {
                        if (l.getHits() > 0) {
                            lineHits++;
                        }
                    }
                    List<Branch> branches = ClassAnalyzer.getCoverableBranches(ct.className);
                    for (Branch b : branches) {
                        if (b.getFalseHits() > 0 && b.getTrueHits() > 0) {
                            branchHits++;
                        }
                    }
                }
                info += "," + relatedLines + "," + lineHits + "," + (lineHits / (float) relatedLines) + "," +
                        relatedBranches + "," + branchHits + "," + (branchHits / (float) relatedBranches);
            } else {
                info += ",0,0,0,0,0,0";
            }

            if (Properties.SWITCH_TIME <= 1){
                info += "," + 0;
            } else {
                info += "," + com.sheffield.leapmotion.Properties.BEZIER_POINTS;
            }

            info += "," + Properties.SWITCH_TIME;

            FileHandler.appendToFile(csv, info + "\n");

            File classes = new File("testing_output/logs/RUN" + Properties.CURRENT_RUN + "-test-results.lines_covered.csv");
            if (classes.getParentFile() != null) {
                classes.getParentFile().mkdirs();
            }
            if (!classes.exists()) {
                classes.createNewFile();
            }

            FileHandler.appendToFile(classes, linesHit.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        startTime = System.currentTimeMillis();
    }

    public void tick() {
        long time = System.currentTimeMillis();
        timeBetweenSwitch = 1000 / Properties.FRAMES_PER_SECOND;
        SeededController sc = SeededController.getSeededController();
        if (time - lastSwitchTime > timeBetweenSwitch) {
            sc.tick();
            lastSwitchTime = time;
        }

        if (time - lastStateCheck > STATE_CHECK_TIME) {
            lastStateCheck = time;
            try {
                DctStateComparator.captureState();
            } catch (Exception e) {
                e.printStackTrace(App.out);
            }
        }

        long runtime = System.currentTimeMillis() - startTime;

        String progress = "[";

        final int bars = 30;
        float percent = runtime / (float) Properties.RUNTIME;
        int b1 = (int) (percent * bars);
        for (int i = 0; i < b1; i++) {
            progress += "-";
        }
        progress += ">";
        int b2 = bars - b1;
        for (int i = 0; i < b2; i++) {
            progress += " ";
        }
        progress += "] " + ((int) (percent * 1000)) / 10f + "%";
        out.print("\rExecuting: " + progress + ". Coverage: " + LAST_LINE_COVERAGE);

        if (runtime > Properties.RUNTIME) {
            status = AppStatus.FINISHED;
        }
    }

    public void end() {
        status = AppStatus.FINISHED;
    }

    public void close() {
        status = AppStatus.CLOSING;
    }
}
