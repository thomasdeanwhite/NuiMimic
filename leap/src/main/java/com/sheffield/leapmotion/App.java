package com.sheffield.leapmotion;

import com.sheffield.instrumenter.analysis.ClassAnalyzer;
import com.sheffield.instrumenter.analysis.ClassNode;
import com.sheffield.instrumenter.analysis.DependencyTree;
import com.sheffield.instrumenter.analysis.ThrowableListener;
import com.sheffield.instrumenter.instrumentation.ClassReplacementTransformer;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.Branch;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.BranchHit;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.Line;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.LineHit;
import com.sheffield.leapmotion.analyzer.AnalyzerApp;
import com.sheffield.leapmotion.analyzer.StateIsolatedAnalyzerApp;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.display.DisplayWindow;
import com.sheffield.leapmotion.instrumentation.MockSystemMillis;
import com.sheffield.leapmotion.output.StateComparator;
import com.sheffield.leapmotion.output.TrainingDataVisualiser;
import com.sheffield.leapmotion.runtypes.ImageStateIdentifier;
import com.sheffield.leapmotion.runtypes.StateShowingFrame;
import com.sheffield.output.Csv;
import com.sheffield.util.ClassNameUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class App implements ThrowableListener, Tickable {
    public static Random random = new Random();
    public static App APP;
    public static boolean CLOSING = false;
    public static boolean RECORDING_STARTED = false;
    private static boolean ENABLE_APPLICATION_OUTPUT = false;
    public static boolean IS_INSTRUMENTING = false;
    public static int RECORDING_INTERVAL = 60000;

    private boolean testing = false;


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
        File classes = new File(Properties.TESTING_OUTPUT + "errors/RUN" + Properties.CURRENT_RUN + "-error.log");
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
                //App.getApp().output(true);
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

        if (testing) {
            startTesting();
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
        switch (Properties.RUN_TYPE) {
            case INSTRUMENT:
                instrument();
                break;
            case VISUALISE:
                visualise();
                break;
            case RECONSTRUCT:
                App.getApp().setup(false);
                reconstruct();
                break;
            case STATE_RECOGNITION:
                //INPUT should contain an array of histograms.
                recogniseStates();
                break;
            case MANUAL_STATE_RECOGNITION:
                //INPUT should contain an array of histograms.
                manualRecogniseStates();
                break;
            default:
                App.out.println("Unimplemented RUNTIME");
                break;
        }
    }

    public static void recogniseStates() {
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


        //for (int i = 8; i < 256; i+=8) {
        //StateComparator.cleanUp();
        //Properties.HISTOGRAM_BINS = i;
        processStates(isi);
        //}


        System.exit(0);

    }

    public static void manualRecogniseStates() {

        final Scanner sc = new Scanner(System.in);

        ImageStateIdentifier isi = new ImageStateIdentifier() {
            @Override
            public int identifyImage(BufferedImage bi, HashMap<Integer, BufferedImage> seenStates) {
                return sc.nextInt();
            }

            @Override
            public String getOutputFilename() {
                return "manual_recognition.csv";
            }
        };
        processStates(isi);
    }

    public static void processStates(ImageStateIdentifier isi) {
        try {
            //INPUT should be a directory contaning screenshots
            String directory = Properties.INPUT[0];

            File dir = new File(directory);

            int counter = 1;

            Properties.FRAME_SELECTION_STRATEGY = Properties
                    .FRAME_SELECTION_STRATEGY.NONE;

            Properties.CURRENT_RUN = 0;

            final HashMap<Integer, BufferedImage> seenStates = new HashMap<Integer, BufferedImage>();

            final int STATES_PER_ROW = 5;

            JFrame imageStates = new JFrame("Seen States") {
                @Override
                public void paint(Graphics g) {
                    //super.paint(g);

                    Graphics2D g2d = (Graphics2D) g;

                    g2d.clearRect(0, 0, getWidth(), getHeight());

                    Set<Integer> keys = seenStates.keySet();

                    Integer[] ks = new Integer[keys.size()];

                    keys.toArray(ks);

                    Arrays.sort(ks);

                    int width = getWidth() / STATES_PER_ROW;

                    int height = (width * 9) / 16;

                    for (int i = 0; i < ks.length; i++) {
                        int x = (i % STATES_PER_ROW) * width;

                        int y = (i / STATES_PER_ROW) * height;

                        if (ks[i] == null) {
                            g2d.setColor(Color.RED);
                            g2d.fillRect(x, y, width, height);
                            continue;
                        }

                        int key = ks[i];

                        g2d.drawImage(seenStates.get(key), x, y, width, height, null);

                        g2d.setColor(Color.red);

                        g2d.drawString("" + key, x + 10, y + height / 2);


                    }

                }
            };

            imageStates.setSize(800, 600);

            imageStates.setLocation(960, 0);

            StateShowingFrame image = new StateShowingFrame();
            image.setSize(960, 540);

            image.setLocation(0, 0);

            image.setVisible(true);

            imageStates.setVisible(true);


            File[] files = dir.listFiles();

            String[] fs = new String[files.length];

            for (int i = 0; i < files.length; i++) {
                fs[i] = files[i].getAbsolutePath();
            }

            //Arrays.sort(fs);

            Comparator<String> comp = new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    int n = o1.lastIndexOf("SCREEN") + 6;
                    int n1 = o2.lastIndexOf("SCREEN") + 6;

                    if (n == n1 && n == 5) {
                        return 0;
                    }

                    if (n == 5) {
                        return 1;
                    }

                    if (n1 == 5) {
                        return -1;
                    }

                    int s = Integer.parseInt(o1.substring(n, o1.length() - 4));
                    int s1 = Integer.parseInt(o2.substring(n, o2.length() - 4));

                    return s - s1;
                }
            };

            Arrays.sort(fs, comp);

            for (String s : fs) {

                File f = new File(s);

                if (f.isDirectory()) {
                    continue;
                }

                if (!f.getName().toLowerCase().endsWith(".png")) {
                    continue;
                }

                BufferedImage orig = ImageIO.read(f);

                BufferedImage bi = new BufferedImage(orig.getWidth(), orig.getHeight(), BufferedImage.TYPE_INT_RGB);

                Graphics2D g2 = bi.createGraphics();

                g2.drawImage(orig, 0, 0, null);

                g2.dispose();

                image.setImage(bi);

                image.paint(image.getGraphics());

                int state = isi.identifyImage(bi, seenStates);

                if (!seenStates.containsKey(state)) {
                    seenStates.put(state, bi);

                    imageStates.paint(imageStates.getGraphics());
                }

                Csv csv = new Csv();

                csv.add("imageId", f.getName());

                csv.add("stateAssignment", "" + state);

                csv.add("totalStates", "" + seenStates.keySet().size());

                csv.add("counter", "" + counter);

                csv.add("histogramBins", "" + Properties.HISTOGRAM_BINS);

                csv.add("histogramThreshold", "" + Properties.HISTOGRAM_THRESHOLD);

                if (Properties.FRAME_SELECTION_STRATEGY.equals(Properties.FrameSelectionStrategy.STATE_ISOLATED)){
                    csv.add("dataHitRatio", "" + StateIsolatedAnalyzerApp.hitRatio());
                } else {
                    csv.add("dataHitRatio", "" + AnalyzerApp.hitRatio());
                }

                csv.finalize();

                File csvFile = new File(f.getParentFile().getAbsolutePath(), Properties.TESTING_OUTPUT + "states/" + isi.getOutputFilename());
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
                counter++;
            }

            imageStates.setVisible(false);

            imageStates = null;

            image.setVisible(false);

            image = null;
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void reconstruct() {

        while (Properties.INPUT.length > 0) {

            SeededController sc = SeededController.getSeededController(false);

            Properties.FRAME_SELECTION_STRATEGY = Properties
                    .FrameSelectionStrategy.REPRODUCTION;

            Properties.CURRENT_RUN = 0;

            long startTime = System.currentTimeMillis();
            long time = startTime;
            long endTime = time + Properties.RUNTIME;
            while ((time = System.currentTimeMillis()) < endTime) {
                sc.tick(time);
                sc.frame();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Csv csv = new Csv();

            csv.add("rootMeanSquared", "" + sc.status().split("rms: ")[1]);

            //Properties.OUTPUT_INCLUDES_ARRAY.add("gestureFiles");

            csv.add("input", Properties.INPUT[0]);

//            csv.merge(Properties.instance().toCsv());

            csv.add("frameSelectionStrategy", Properties
                    .FRAME_SELECTION_STRATEGY.toString());

            MockSystemMillis.RUNTIME = (int) (time - startTime);

            csv.add("runtime", "" + MockSystemMillis.RUNTIME);

            csv.finalize();
            App.getApp().output(csv);

            String[] gFiles = Properties.INPUT;

            if (gFiles.length > 1) {
                Properties.INPUT = new String[gFiles.length - 1];

                for (int i = 1; i < gFiles.length; i++) {
                    Properties.INPUT[i - 1] = gFiles[i];
                }
            } else {
                break;
            }

            SeededController.getSeededController().cleanUp();
            SeededController.resetSeededController();
        }

        System.exit(0);
    }

    public static void visualise() {
        TrainingDataVisualiser tdv = new TrainingDataVisualiser(Properties.INPUT[0]);
    }

    public static void instrument() {
        App.out.println("- Instrumenting JAR");
        String[] defaultHiddenPackages = new String[]{"com/sheffield/leapmotion/", "com/google/",
                "com/leapmotion/", "java/", "org/json/", "org/apache/commons/cli/",
                "org/junit/", /*"Launcher",*/ "org/apache", "com/garg", "net/sourceforge",
                "com/steady", "com/thought", "com/jogamp", "com/bulletphysics", "com/jme3",
                "com/oracle", "org/objectweb", "javax", "jogamp", "jme3", "org/lwjgl", "net/java"};
        for (String s : defaultHiddenPackages) {
            ClassReplacementTransformer.addForbiddenPackage(s);
        }
        ENABLE_APPLICATION_OUTPUT = true;
        IS_INSTRUMENTING = true;
        Properties.LOG = false;
        ClassAnalyzer.setOut(App.out);


        Properties.INSTRUMENTATION_APPROACH = Properties.InstrumentationApproach.ARRAY;
        try {
            String dir = Properties.JAR_UNDER_TEST.substring(0, Properties.JAR_UNDER_TEST.lastIndexOf("/") + 1);

            Properties.WRITE_CLASS = true;
            Properties.BYTECODE_DIR = dir + "classes";

            LeapMotionApplicationHandler.instrumentJar(Properties.JAR_UNDER_TEST);

            String output = dir + "branches.csv";
            String output2 = dir + "lines.csv";
            App.out.print("+ Writing output to: " + dir + " {branches.csv, lines.csv}");
            ClassAnalyzer.output(output, output2, Properties.UNTRACKED_PACKAGES);
            App.out.println("\r+ Written output to: " + dir + " {branches.csv, lines.csv}");

            HashSet<ClassNode> nodes;
            if (Properties.SLICE_ROOT == null) {
                ArrayList<ClassNode> superNodes = DependencyTree.getDependencyTree().getPackageNodes(DependencyTree.getClassMethodId("com.leapmotion.leap.Controller", "<init>"));

                nodes = new HashSet<ClassNode>();

                nodes.addAll(superNodes);
//
//                for (ClassNode cn : superNodes){
//                    nodes.addAll(cn.getDependencies());
//                }
            } else {
                App.out.println();
                nodes = new HashSet<ClassNode>();
                for (String s : Properties.SLICE_ROOT.split(";")) {
                    s = ClassNameUtils.standardise(s);
                    App.out.print("\r[Getting dependencies for: " + s + "]");
                    nodes.addAll(DependencyTree.getDependencyTree().getDependencies(s));
                }
            }

            Set<String> lines = new HashSet<String>();

//            HashSet<String> lines = new HashSet<String>();
            ArrayList<String> relatedClasses = new ArrayList<String>();
            for (ClassNode cn : nodes) {

                HashSet<String> classes = new HashSet<String>();

                String[] clazzes = cn.toString(relatedClasses).split("\n");

                classes.addAll(Arrays.asList(clazzes));

                String[] link = cn.toNomnoml().split("\n");
                for (String n : link) {
                    lines.add(n);
                }

                for (String s : classes) {


                        if (s.length() > 0) {
                            if (!ClassReplacementTransformer.isForbiddenPackage(s) && !relatedClasses.contains(s)) {
                                relatedClasses.add(s);
                                //App.out.println(s);
                            }
                        }
                }

                //App.out.println(cn.toNomnoml());
            }

//            ArrayList<ClassNode> options = DependencyTree.getDependencyTree().getPackageNodes("JOptionsPane");
//
//            for (ClassNode cn : options) {
//                App.out.println("OPTIONS: " + cn.toNomnoml());
//            }


            for (String s : lines) {
                App.out.println(s);
            }

            String related = dir + "related_classes.csv";

            File relatedFile = new File(related);

            if (!relatedFile.exists()) {
                relatedFile.createNewFile();
            }

            String classes = "";

            String[] forbid = new String[]{};

            if (Properties.UNTRACKED_PACKAGES != null){
                forbid = Properties.UNTRACKED_PACKAGES.split(",");
            }

            for (int i = 0; i < forbid.length; i++){
                forbid[i] = ClassNameUtils.standardise(forbid[i]);
            }

            relatedClasses.sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    for (int i = 0; i < Math.min(o1.length(), o2.length()); i++){
                        if (o1.charAt(i) == o2.charAt(i)){
                            continue;
                        }
                        return o1.charAt(i) - o2.charAt(i);
                    }
                    return 0;
                }
            });

            for (String c : relatedClasses) {

                boolean skip = false;
                for (String f : forbid){
                    if (c.startsWith(f)){
                        skip = true;
                        break;
                    }
                }

                if (c == null || c.length() == 0 || skip || c.startsWith("com/leapmotion/leap")) {
                    continue;
                }

                String cName = DependencyTree.getClassName(c);
                String mName = DependencyTree.getMethodName(c);

                classes += c + "," + ClassAnalyzer.getCoverableLines(cName, mName).size() + "," + ClassAnalyzer.getCoverableBranches(cName, mName).size() + "\n";
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
                    long time = System.currentTimeMillis();
                    int timePassed = (int) (time - lastTime);
                    app.tick(time);
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
                        App.getApp().output(false);
                        lastTimeRecorded = lastTime;
                    }
                    lastTime += timePassed;
                }
                App.out.println("- Gathering Testing Information...");
                ClassAnalyzer.collectHitCounters(false);
                MockSystemMillis.RUNTIME = (int) (System.currentTimeMillis() - startTime);
                App.getApp().output(true);
                System.exit(0);

            }

        });
        mainThread.start();

    }

    public void output(boolean finished) {
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

            File outFldr = new File(Properties.TESTING_OUTPUT + "result_states");
            outFldr.mkdirs();

            File output = new File(outFldr, "RUN-" + Properties.CURRENT_RUN + "-" + System.currentTimeMillis() + "-" + Properties.INPUT[0] + "-" + Properties.RUNTIME + "ms.png");
            try {
                ImageIO.write(bi, "png", output);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (Properties.VISUALISE_DATA)
                App.DISPLAY_WINDOW.dispatchEvent(new WindowEvent(App.DISPLAY_WINDOW, WindowEvent.WINDOW_CLOSING));
        }
        try {
            ClassAnalyzer.setOut(App.out);
            StringBuilder linesHit = new StringBuilder();
            ArrayList<LineHit> linesCovered = ClassAnalyzer.getLinesCovered();
            for (LineHit lh : ClassAnalyzer.getTotalLines()) {

                String className = lh.getLine().getClassName();

                if (classSeen.contains(className)) {
                    className = "" + classSeen.indexOf(className);
                } else {
                    File classes = new File(Properties.TESTING_OUTPUT + "logs/RUN" + Properties.CURRENT_RUN + "-test-results.classes.csv");
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
                    File classes = new File(Properties.TESTING_OUTPUT + "logs/RUN" + Properties.CURRENT_RUN + "-test-results.classes.csv");
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

            int lineHits = 0;
            int branchHits = 0;

            if (relatedClasses.size() > 0) {
                HashMap<String, ArrayList<String>> relClas = new HashMap<String, ArrayList<String>>();
                for (ClassTracker ct : relatedClasses) {
                    String className = DependencyTree.getClassName(ct.className);
                    String methodName = DependencyTree.getMethodName(ct.className);

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
            csv.add("runtime", "" + MockSystemMillis.RUNTIME);

            csv.finalize();
            output(csv);

            File classes = new File(Properties.TESTING_OUTPUT + "logs/RUN" + Properties.CURRENT_RUN + "-test-results.lines_covered.csv");
            if (classes.getParentFile() != null) {
                classes.getParentFile().mkdirs();
            }
            if (!classes.exists()) {
                classes.createNewFile();
            }

            FileHandler.appendToFile(classes, linesHit.toString() + "\n");

            File branches = new File(Properties.TESTING_OUTPUT + "logs/RUN" + Properties.CURRENT_RUN + "-test-results.branches_covered.csv");
            if (branches.getParentFile() != null) {
                branches.getParentFile().mkdirs();
            }
            if (!branches.exists()) {
                branches.createNewFile();
            }

            FileHandler.appendToFile(branches, branchesHit.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace(App.out);
        }
    }

    public void output(Csv csv) {

        File csvFile = new File(Properties.TESTING_OUTPUT + "logs/RUN" + Properties.CURRENT_RUN + "-test-results.csv");
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

    public void start() {
        startTime = System.currentTimeMillis();
    }


    private long lastUpdate = 0;

    public void tick(long time) {
        timeBetweenSwitch = 1000 / Properties.FRAMES_PER_SECOND;
        SeededController sc = SeededController.getSeededController();
        //if (time - lastSwitchTime > timeBetweenSwitch) {
        if (sc.lastTick() < time) {
            sc.tick(time);
            lastSwitchTime = time;
        }
        //}

        if (time - lastStateCheck > STATE_CHECK_TIME) {
            lastStateCheck = time;
            try {
                StateComparator.captureState();
            } catch (Exception e) {
                e.printStackTrace(App.out);
            }
        }

        MockSystemMillis.RUNTIME = System.currentTimeMillis() - startTime;

        String progress = "[";

        final int bars = 10;
        float percent = MockSystemMillis.RUNTIME / (float) Properties.RUNTIME;
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
        out.print("\r" + progress + ". Cov: " + LAST_LINE_COVERAGE + ". " + SeededController.getSeededController().status());

        if (MockSystemMillis.RUNTIME > Properties.RUNTIME) {
            status = AppStatus.FINISHED;
        }
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
