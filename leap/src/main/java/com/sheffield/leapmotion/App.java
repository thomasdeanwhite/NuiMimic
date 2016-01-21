package com.sheffield.leapmotion;

import com.sheffield.instrumenter.Display;
import com.sheffield.instrumenter.Properties;
import com.sheffield.instrumenter.analysis.ClassAnalyzer;
import com.sheffield.instrumenter.analysis.ThrowableListener;
import com.sheffield.instrumenter.instrumentation.ClassReplacementTransformer;
import com.sheffield.instrumenter.states.StateTracker;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.display.DisplayWindow;
import org.apache.commons.cli.*;

import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.security.Permission;
import java.util.Random;

public class App implements ThrowableListener {
    public static Random random = new Random();
    public static App APP;
    public static boolean CLOSING = false;
    public static boolean RECORDING_STARTED = false;
    private static boolean ENABLE_APPLICATION_OUTPUT = true;
    private static boolean IS_INSTRUMENTING = false;

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

            Display.getDisplay().addCommand(strs[0]);
            originalOut.println(strs[0]);
        }

        @Override
        public void print(String s) {
            Display.getDisplay().addCommand(s);
            originalOut.print(s);
        }
    };

    private AppStatus status;
    private long startTime;
    private long lastSwitchTime;
    private long timeBetweenSwitch;
    private StateTracker stateTracker;

    @Override
    public void throwableThrown(Throwable t) {
        App.out.println("Throwable thrown! " + t.getLocalizedMessage());
        output();
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
        if (getApp() == null) {
            App.getApp().setStatus(AppStatus.TESTING);
            background(null);
        }
        getApp().setStatus(AppStatus.TESTING);
    }

    public static void startTesting() {
        App.out.println("- Testing Entry Point Triggered.");
        App.getApp().setStatus(AppStatus.TESTING);
        background(null);
    }

    public void setup() {
        if (Properties.SHOW_HAND) {
            DISPLAY_WINDOW = new DisplayWindow();
        }
        SeededController.getController();
        startTime = System.currentTimeMillis();
        lastSwitchTime = startTime;
        timeBetweenSwitch = 1000 / Properties.SWITCH_RATE;
        stateTracker = new StateTracker();
        ClassAnalyzer.addStateChangeListener(stateTracker);
        if (!ENABLE_APPLICATION_OUTPUT) {
            PrintStream dummyStream = new PrintStream(new OutputStream() {

                @Override
                public void write(int b) throws IOException {
                    // TODO Auto-generated method stub
                    // App.out.write(b);
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

    @SuppressWarnings("static-access")
    public static void setOptions(String[] args) {
        CommandLineParser parser = new GnuParser();
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("jar").withDescription("Jar file to run").hasArg()
                .withArgName("jar").create("jar"));

        options.addOption(OptionBuilder.withLongOpt("ngramType").withDescription("ngramType to run").hasArg()
                .withArgName("ngramType").create("ngramType"));

        options.addOption(OptionBuilder.withLongOpt("runtime").withDescription("Time for testing to run").hasArg()
                .withArgName("runtime").create("runtime"));

        options.addOption(OptionBuilder.withLongOpt("library").withDescription("path to leapmotion natives").hasArg()
                .withArgName("lib").create("lib"));

        options.addOption(OptionBuilder.withLongOpt("cp").withDescription("ClassPath containing dependant JARs")
                .hasArg().withArgName("cp").create("cp"));

        options.addOption(OptionBuilder.withLongOpt("switch_rate").withDescription("times per second to change frames")
                .hasArg().withArgName("switch_rate").create("switch_rate"));

        options.addOption(OptionBuilder.withLongOpt("agentJar").withDescription("path to agent jar for instrumentation")
                .hasArg().withArgName("agentJar").create("agentJar"));

        options.addOption(OptionBuilder.withLongOpt("frameSelectionStrategy")
                .withDescription("strategy for selecting new frames to seed").hasArg()
                .withArgName("frameSelectionStrategy").create("frameSelectionStrategy"));

        options.addOption(OptionBuilder.withLongOpt("maxLoadedFrames")
                .withDescription("max frames to retain in memory for Controller.frame(i)").hasArg()
                .withArgName("maxLoadedFrames").create("maxLoadedFrames"));

        options.addOption(OptionBuilder.withLongOpt("delayTime")
                .withDescription("Time to wait before seeding (ms)").hasArg()
                .withArgName("delayTime").create("delayTime"));

        options.addOption(OptionBuilder.withLongOpt("backgroundFrames")
                .withDescription("max frames loaded in memory to use for selecting the next frame").hasArg()
                .withArgName("backgroundFrames").create("backgroundFrames"));

        options.addOption(OptionBuilder.withLongOpt("playback")
                .withDescription("The file to use for Playback before generation begins").hasArg()
                .withArgName("playback").create("playback"));

        options.addOption(OptionBuilder.withLongOpt("recording")
                .withDescription("Used when a pre-sequence of data should be recorded and played back.")
                .create("recording"));

        options.addOption(OptionBuilder.withLongOpt("replace_fingers_method")
                .withDescription("If using an older API, the Hand.fingers() method returns only extended fingers. If so use this flag.")
                .create("replace_fingers_method"));

        options.addOption(OptionBuilder.withLongOpt("delayLibrary")
                .withDescription("Use if getting a Library Already Loaded error.")
                .create("delayLibrary"));

        options.addOption(OptionBuilder.withLongOpt("branches").withDescription("The branches to use as goals to cover")
                .hasArg().withArgName("branches").create("branches"));

        options.addOption(OptionBuilder.withLongOpt("exiledClasses")
                .withDescription(
                        "The classes which shouldn't be loaded by the class loader (could be loaded by reflection later)")
                .hasArg().withArgName("exiledClasses").create("exiledClasses"));


        options.addOption(OptionBuilder.withLongOpt("packages")
                .withDescription(
                        "Packages that should be instrumented")
                .hasArg().withArgName("packages").create("packages"));

        options.addOption(OptionBuilder.withLongOpt("gestureFiles")
                .withDescription(
                        "Gesture Files to use in NGram")
                .hasArg().withArgName("gestureFiles").create("gestureFiles"));

        try {
            CommandLine cmd = parser.parse(options, args);
            for (Option o : cmd.getOptions()) {
                if (o.getArgName().equals("jar")) {
                    Properties.SUT = o.getValue().replace("\\", "/");
                } else if (o.getArgName().equals("ngramType")) {
                    Properties.NGRAM_TYPE= o.getValue();
                } else if (o.getArgName().equals("runtime")) {
                    Properties.RUNTIME = Long.parseLong(o.getValue());
                } else if (o.getArgName().equals("lib")) {
                    Properties.LIBRARY = o.getValue();
                } else if (o.getArgName().equals("cp")) {
                    Properties.CLASS_PATH = o.getValue();
                } else if (o.getArgName().equals("switch_rate")) {
                    Properties.SWITCH_RATE = Long.parseLong(o.getValue());
                } else if (o.getArgName().equals("agentJar")) {
                    Properties.LM_AGENT_JAR = o.getValue();
                } else if (o.getArgName().equals("frameSelectionStrategy")) {
                    Properties.FRAME_SELECTION_STRATEGY = Properties.FRAME_SELECTION_STRATEGY.valueOf(o.getValue());
                } else if (o.getArgName().equals("maxLoadedFrames")) {
                    Properties.MAX_LOADED_FRAMES = Integer.parseInt(o.getValue());
                } else if (o.getArgName().equals("backgroundFrames")) {
                    Properties.BACKGROUND_FRAMES = Long.parseLong(o.getValue());
                } else if (o.getArgName().equals("delayTime")) {
                        Properties.DELAY_TIME = Long.parseLong(o.getValue());
                } else if (o.getArgName().equals("playback")) {
                    Properties.PLAYBACK_FILE = o.getValue();
                } else if (o.getArgName().equals("branches")) {
                    Properties.BRANCHES_TO_COVER = o.getValue();
                } else if (o.getArgName().equals("exiledClasses")) {
                    Properties.EXILED_CLASSES = o.getValue().split(";");
                } else if (o.getArgName().equals("packages")) {
                    App.out.println("- Only instrumenting " + o.getValue());
                    Properties.INSTRUMENTED_PACKAGES = o.getValue().split(";");
                } else if (o.getArgName().equals("gestureFiles")) {
                    App.out.println("- Using gesture files: " + o.getValue());
                    Properties.GESTURE_FILES = o.getValue().split(";");
                }

            }

            if (Properties.PLAYBACK_FILE == null) {
                File playback = new File("playback.sequence");
                if (playback.exists()) {
                    Properties.PLAYBACK_FILE = playback.getAbsolutePath();
                    App.out.println("- Found sequence file at: " + Properties.PLAYBACK_FILE);
                }
            }
            if (!IS_INSTRUMENTING) {
                File playback = new File("branches.csv");
                if (playback.getAbsoluteFile().exists()) {
                    try {
                        String branchesString = FileHandler.readFile(playback);

                        String[] branches = branchesString.split(",");

                        for (String b : branches) {
                            String[] info = b.split("#");
                            ClassAnalyzer.branchFound(info[0].trim(), Integer.parseInt(info[1]));
                        }

                        App.out.println("- Found branches file (" + branches.length + " branches) at: " + playback.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            for (int i = 0; i < Properties.GESTURE_FILES.length; i++){
                Properties.GESTURE_FILES[i] = Properties.GESTURE_FILES[i] + "-" + Properties.NGRAM_TYPE;
            }

            if (Properties.INSTRUMENTED_PACKAGES == null) {
                App.out.println("- Instrumenting All Classes");
            }

            if (cmd.hasOption("recording")) {
                App.out.println("- Recording mode activated.");
                Properties.RECORDING = true;
            }

            if (cmd.hasOption("replace_fingers_method")) {
                App.out.println("- Replacing calls to Hand.fingers() with Hand.fingers().extended()");
                Properties.REPLACE_FINGERS_METHOD = true;
            }

            if (cmd.hasOption("delayLibrary")) {
                App.out.println("- Delaying Library Load.");
                Properties.DELAY_LIBRARY = true;
            }

        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace(App.out);
        }

        if (Properties.BRANCHES_TO_COVER != null) {
            // we have target branches to cover!
            String[] branchesToCover = Properties.BRANCHES_TO_COVER.split("\\|");
            for (String s : branchesToCover) {
                ClassAnalyzer.addBranchToCover(s);
            }
            App.out.println("Added " + branchesToCover.length + " branch goals.");
        }

    }

    public static void main(String[] args) {
        App.out.print("- Instrumenting JAR with options: ");
        String[] defaultHiddenPackages = new String[]{"com/sheffield/leapmotion", "com/google/gson",
                "com/leapmotion", "javax/", "org/json", "org/apache/commons/cli",
                "org/junit"};
        for (String s : defaultHiddenPackages){
            ClassReplacementTransformer.forbiddenPackages.add(s);
        }
        ENABLE_APPLICATION_OUTPUT = true;
        IS_INSTRUMENTING = true;
        ClassAnalyzer.setOut(App.out);
        for (String s : args){
            App.out.print(s + " ");
        }
        App.out.println(".");
        setOptions(args);

        Properties.INSTRUMENTATION_APPROACH = Properties.InstrumentationApproach.STATIC;

        try {
            LeapMotionApplicationHandler.instrumentJar(Properties.SUT);
            String output = Properties.SUT.substring(0, Properties.SUT.lastIndexOf("/") + 1) + "branches.csv";
            App.out.print("\r\tWriting output to: " + output);
            ClassAnalyzer.output(output);
            App.out.println("\r\tWritten output to " + output);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static void runAgent() {
        String command = "java -javaagent:lm-agent.jar -jar " + Properties.SUT;
        App.out.print("Executing command: \n\t" + command);
        try {
            Process p = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void background(String[] args) {
        if (mainThread != null) {
            App.out.println("Found thread already running!");
            return;
        }
        //Properties.INSTRUMENTATION_APPROACH = Properties.InstrumentationApproach.ARRAY;
        if (args != null && args.length > 0) {
            setOptions(args);
            App.out.println("Options setup");
        } else {
            File options = new File("testing-options");
            if (options.getAbsoluteFile().exists()) {
                try {
                    String opts = FileHandler.readFile(options).trim();

                    App.out.println("- Found options: " + opts);

                    setOptions(opts.split(" "));

                } catch (IOException e) {
                    setOptions(new String[]{});
                    e.printStackTrace(App.out);
                }
            } else {
                setOptions(new String[]{});
            }
        }
        App.out.println("- Starting background testing thread.");
        mainThread = new Thread(new Runnable() {

            public void run() {
                // TODO Auto-generated method stub

                App app = App.getApp();
                app.setup();
                app.start();
                App.out.println("- Starting Frame Seeding");
                int delay = (int) (1000f / Properties.SWITCH_RATE);

                try {
                    Thread.sleep(Properties.DELAY_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                long lastTime = System.currentTimeMillis();
                while (app.status() != AppStatus.FINISHED) {
                    int timePassed = (int) (System.currentTimeMillis() - lastTime);
                    try {
                        app.tick();
                        int d = delay - timePassed;
                        if (d >= 0) {
                            Thread.sleep(d);
                        }
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    lastTime += timePassed;
                }
                if (Properties.RECORDING) {
                    com.sheffield.leapmotion.sampler.SamplerApp.getApp().appFinished();
                }

                App.getApp().output();


            }

        });
        mainThread.start();

    }

    public void output(){
        App.out.println("- Finished testing: ");
        App.out.println("@ Coverage Report: ");
        App.out.println(ClassAnalyzer.getReport());
        File csv = new File("test-results.csv");
        if (csv.getParentFile() != null){
            csv.getParentFile().mkdirs();
        }
        try {
            boolean newFile = !csv.getAbsoluteFile().exists();
            if (newFile){
                App.out.println("Creating file " + csv.getAbsolutePath());
                csv.createNewFile();
            }
            ClassAnalyzer.setOut(App.out);
            FileHandler.appendToFile(csv, ClassAnalyzer.toCsv(newFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        App.DISPLAY_WINDOW.dispatchEvent(new WindowEvent(App.DISPLAY_WINDOW, WindowEvent.WINDOW_CLOSING));
        System.exit(0);
    }

    public void start() {
        startTime = System.currentTimeMillis();
    }

    public void tick() {
        long time = System.currentTimeMillis();

        if (time - lastSwitchTime > timeBetweenSwitch) {
            SeededController sc = SeededController.getSeededController();
            sc.tick();
            lastSwitchTime = time;
        }

        if (System.currentTimeMillis() - startTime > Properties.RUNTIME) {
            status = AppStatus.FINISHED;
        }
    }

    public void end() {
        status = AppStatus.FINISHED;
    }

    public void close() {
        status = AppStatus.CLOSING;
    }

    public StateTracker getStateTracker() {
        return stateTracker;
    }
}
