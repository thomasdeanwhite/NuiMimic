package com.sheffield.leapmotion.sampler;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Listener;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.controller.mocks.HandFactory;
import com.sheffield.leapmotion.display.DisplayWindow;
import com.sheffield.leapmotion.output.FrameDeconstructor;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;


/**
 * Hello world!
 */
public class SamplerApp extends Listener {

    private static SamplerApp APP;

    public static boolean RECORDING_USERS = true;

    public static boolean LOOP = true;

    private float FRAMES_PER_SECOND = 0;
    private long FRAMES = 0;

    public static final long[] BREAK_TIMES = FrameDeconstructor.BREAK_TIMES;

    public static boolean USE_CONTROLLER = true;
    public static PrintStream out = System.out;

    public static boolean SHOW_GUI = false;

    private FrameDeconstructor frameDeconstructor;

    private AppStatus status;
    private long startTime;
    private DisplayWindow display;
    private boolean startedRecording = false;

    private String filenameStart = "gorogoa";

    private static boolean REQUEST_NAME = true;

    public AppStatus status() {
        return status;
    }

    public void setStatus(AppStatus status) {
        this.status = status;
    }

    private SamplerApp(String filenameStart) {
        this();
        this.filenameStart = filenameStart;
    }

    private SamplerApp() {
        super();

        frameDeconstructor = new FrameDeconstructor();
        status = AppStatus.DISCONNECTED;
        startTime = System.currentTimeMillis();

        if (REQUEST_NAME) {
            filenameStart = JOptionPane.showInputDialog(null, "Please enter your identifier", "Leap Motion Sampler", JOptionPane.INFORMATION_MESSAGE);
        }

        if (SHOW_GUI) {
            display = new DisplayWindow();
        }
    }

    public static SamplerApp getApp() {
        if (APP == null) {
            APP = new SamplerApp();
        }

        if (Properties.DIRECTORY.trim().toLowerCase().endsWith("/processed")) {
            Properties.DIRECTORY = Properties.DIRECTORY.substring(0, Properties.DIRECTORY.toLowerCase().indexOf("/processed"));
        }

        return APP;
    }

    public static SamplerApp getApp(String filenameStart) {
        if (APP == null) {
            APP = new SamplerApp(filenameStart);
        }

        if (Properties.DIRECTORY.trim().toLowerCase().endsWith("/processed")) {
            Properties.DIRECTORY = Properties.DIRECTORY.substring(0, Properties.DIRECTORY.toLowerCase().indexOf("/processed"));
        }

        return APP;
    }

    public static void main(String[] args) {
        if (args != null && args.length >= 1) {
            REQUEST_NAME = false;
            SamplerApp.getApp(args[0]);
        }

        SamplerApp app = SamplerApp.getApp();
        app.setStatus(AppStatus.CONNECTING);
        Controller controller = null;
        if (USE_CONTROLLER) {
            controller = new Controller();
            controller.addListener(app);
        }

        while (LOOP && app.status() != AppStatus.FINISHED) {
            try {
                app.tick();
                Thread.sleep(5);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        app.finishedWritingSequence();
        if (controller != null) {
            controller.removeListener(app);
        }

        if (LOOP) {
            SamplerApp.out.println("- Finished data collection");
            System.exit(0);
        }

    }

    private final ArrayList<Frame> frames = new ArrayList();

    @Override
    public void onConnect(Controller controller) {
        status = AppStatus.CONNECTED;

        // Policy hack so app always receives data
        com.leapmotion.leap.LeapJNI.Controller_setPolicy(Controller.getCPtr(controller), controller, 1);
        com.leapmotion.leap.LeapJNI.Controller_setPolicy(Controller.getCPtr(controller), controller, 1 << 15);

        //enable gestures for gesture model to work
        controller.enableGesture(Gesture.Type.TYPE_SWIPE);
        controller.enableGesture(Gesture.Type.TYPE_CIRCLE);
        controller.enableGesture(Gesture.Type.TYPE_SCREEN_TAP);
        controller.enableGesture(Gesture.Type.TYPE_KEY_TAP);

        SamplerApp.out.println("- Connected to LeapMotion");
    }

    @Override
    public void onFrame(Controller arg0) {
        final Frame frame = arg0.frame();
        if (frame.isValid()) {
            frames.add(frame);
            frame(frame);

        }
    }

    private boolean framesProcessing = false;

    private final String UNIQUE_MACHINE_NAME = "pepper";

    public synchronized void frame(Frame f) {

        Properties.HISTOGRAM_THRESHOLD = 0;
        Properties.HISTOGRAM_BINS = 256;

        Frame frame = f;
        FRAMES++;

        try {
            final long time = System.currentTimeMillis();

            if (!startedRecording) {
                boolean validHand = false;

                for (Hand h : frame.hands()) {
                    if (h.isValid()) {
                        validHand = true;
                    }
                }

                if (validHand) {
                    startedRecording = true;
                    startTime = System.currentTimeMillis();
                }
                if (SHOW_GUI) {
                    display.setFrame(frame);
                }
            } else {
                frameDeconstructor.setFilenameStart(filenameStart);

                if (RECORDING_USERS) {
                    frameDeconstructor.setAddition("");
                    frameDeconstructor.outputRawFrameData(frame);
                } else {
                    if (breakIndex >= 0 && breakIndex < BREAK_TIMES.length) {
                        if (time - startTime > BREAK_TIMES[breakIndex]) {
                            breakIndex++;
                            if (breakIndex >= BREAK_TIMES.length) {
                                status = AppStatus.FINISHED;
                                return;
                            }
                            frameDeconstructor.resetFiles(breakIndex);
                        }
                    } else {
                        status = AppStatus.FINISHED;
                        return;
                    }

                    String addition = "-" + BREAK_TIMES[breakIndex];

                    frameDeconstructor.setAddition(addition);

                    //write gestures out to file
                    try {
                        frameDeconstructor.outputGestureModel(frame);
                    } catch (IOException e) {
                        e.printStackTrace(App.out);
                    }

                    for (Hand h : frame.hands()) {
                        //write hands out to file
                        if (h.isValid() && h.isRight()) {

                            String uniqueId = frame.timestamp() + "@"
                                    + UNIQUE_MACHINE_NAME;

                            frameDeconstructor.setUniqueId(uniqueId);

                            if (frame.gestures().count() > 0) {
                                for (Gesture g : frame.gestures()) {
                                    frameDeconstructor.setCurrentGesture(g.type().name());
                                }
                            }
                            try {

                                if (Properties.PROCESS_PLAYBACK) {


                                    String frameAsString = HandFactory.handToString(uniqueId, h);

                                    frameDeconstructor.outputJointPositionModel(frameAsString);
                                    frameDeconstructor.outputSequence();
                                    frameDeconstructor.outputHandPositionModel(h);
                                    frameDeconstructor.outputHandRotationModel(h);
                                }

                                if (Properties.PROCESS_SCREENSHOTS) {

                                    if (!frameDeconstructor.isCalculating()) {
                                        frameDeconstructor.outputCurrentState();
                                    }
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }
                    }
                }
            }
            if (LOOP) {
                long done = time - startTime;
                FRAMES_PER_SECOND = 1000f * (FRAMES / (float) done);

                final int bars = 60;
                long total = BREAK_TIMES[BREAK_TIMES.length - 1];

                String progress = "[";
                float percent = done / (float) total;
                int b1 = (int) (percent * bars);
                for (int j = 0; j < b1; j++) {
                    progress += "-";
                }
                progress += ">";
                int b2 = bars - b1;
                for (int j = 0; j < b2; j++) {
                    progress += " ";
                }
                progress += "] " + (int) (percent * 100) + "% (" +
                        (int) FRAMES_PER_SECOND + ")";

                out.print("\r" + progress);

                if (percent >= 1) {
                    status = AppStatus.FINISHED;
                }
            }
        } catch (Exception e) {
            e.printStackTrace(App.out);
        }

    }


    public void tick() {
        if (SHOW_GUI) {
            if (!display.isVisible()) {
                appFinished();
            }
        }
    }

    public void appFinished() {
        status = AppStatus.FINISHED;
    }


    private int breakIndex = 0;

    public void finishedWritingSequence() {
//        if (sequenceFile != null) {
//            try {
//                FileHandler.appendToFile(sequenceFile, "]");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }
}
