package com.sheffield.leapmotion.sampler;

import com.leapmotion.leap.*;
import com.leapmotion.leap.Frame;
import com.sheffield.imageprocessing.DiscreteCosineTransformer;
import com.sheffield.instrumenter.Properties;
import com.sheffield.instrumenter.states.ScreenGrabber;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.display.DisplayWindow;
import com.sheffield.leapmotion.mocks.HandFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;


/**
 * Hello world!
 */
public class SamplerApp extends Listener {

    private static SamplerApp APP;

    private static final long[] BREAK_TIMES = new long[]{300000, 600000, 1200000};

    public static boolean USE_CONTROLLER = true;
    public static PrintStream out = System.out;

    public static boolean SHOW_GUI = false;

    private DiscreteCosineTransformer dct;

    private static double[] lastImage;

    private AppStatus status;
    private long startTime;
    private DisplayWindow display;
    private boolean startedRecording = false;
    private File currentGestures;
    private File currentGesturesCircle;
    private File currentGesturesSwipe;
    private File currentGesturesScreenTap;
    private File currentGesturesKeyTap;
    private File currentSequence;
    private File currentHands;
    private File currentPosition;
    private File currentRotation;
    private File currentDct;

    private ArrayList<Integer[]> states;

    private int dctFeatures = 512;

    private String filenameStart = "gorogoa";

    private static final boolean REQUEST_NAME = true;

    public AppStatus status() {
        return status;
    }

    public void setStatus(AppStatus status) {
        this.status = status;
    }

    private SamplerApp() {
        super();
        states = new ArrayList<Integer[]>();

        status = AppStatus.DISCONNECTED;
        startTime = System.currentTimeMillis();

        if (REQUEST_NAME){
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
        return APP;
    }

    public static void main(String[] args) {
        SamplerApp app = SamplerApp.getApp();
        app.setStatus(AppStatus.CONNECTING);
        Controller controller = null;
        if (USE_CONTROLLER) {
            controller = new Controller();
            controller.addListener(app);
        }

        while (app.status() != AppStatus.FINISHED) {
            try {
                app.tick();
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        app.finishedWritingSequence();
        if (controller != null) {
            controller.removeListener(app);
        }

        SamplerApp.out.println("- Finished data collection");
        System.exit(0);

    }

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
            new Thread(new Runnable(){
                @Override
                public void run() {
                    frame(frame);
                }


            }).start();
            // super.onFrame(arg0);
        }
    }

    public synchronized void frame(Frame frame) {
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

            if (breakIndex >= 0 && breakIndex < BREAK_TIMES.length) {
                if (time - startTime > BREAK_TIMES[breakIndex]) {
                    breakIndex++;
                    if (breakIndex >= BREAK_TIMES.length) {
                        status = AppStatus.FINISHED;
                        return;
                    }
                    sequenceFile = null;
                    currentHands = null;
                    currentRotation = null;
                    currentPosition = null;
                    currentSequence = null;
                    currentGestures = null;
                    currentDct = null;
                }
            } else {
                status = AppStatus.FINISHED;
                return;
            }

            String addition = "-" + BREAK_TIMES[breakIndex];

            //write gestures out to file
            try {

                if (currentGestures == null) {
                    App.out.println("- New Gestures File: " + filenameStart);
                    currentGestures = new File(FileHandler.generateFileWithName(filenameStart) + addition + "ms.sequence_gesture_data");
                    currentGestures.getParentFile().mkdirs();
                    currentGestures.createNewFile();

                    currentGesturesCircle = new File(FileHandler.generateFileWithName(filenameStart) + addition + "ms.pool_gesture_circle");
                    currentGesturesCircle.getParentFile().mkdirs();
                    currentGesturesCircle.createNewFile();

                    currentGesturesSwipe = new File(FileHandler.generateFileWithName(filenameStart) + addition + "ms.pool_gesture_swipe");
                    currentGesturesSwipe.getParentFile().mkdirs();
                    currentGesturesSwipe.createNewFile();

                    currentGesturesScreenTap = new File(FileHandler.generateFileWithName(filenameStart) + addition + "ms.pool_gesture_screentap");
                    currentGesturesScreenTap.getParentFile().mkdirs();
                    currentGesturesScreenTap.createNewFile();

                    currentGesturesKeyTap = new File(FileHandler.generateFileWithName(filenameStart) + addition + "ms.pool_gesture_keytap");
                    currentGesturesKeyTap.getParentFile().mkdirs();
                    currentGesturesKeyTap.createNewFile();
                }
                String gestureString = "";
                if (frame.gestures().count() > 0) {


                    for (Gesture g : frame.gestures()) {
                        gestureString += g.type() + "+";

                        switch (g.type()) {
                            case TYPE_CIRCLE:
                                CircleGesture cg = new CircleGesture(g);
                                String circleGesture = cg.center().getX() + "," +
                                        cg.center().getY() + "," +
                                        cg.center().getZ() + ",";

                                circleGesture += cg.normal().getX() + "," +
                                        cg.normal().getY() + "," +
                                        cg.normal().getZ() + ",";

                                circleGesture += cg.radius();

                                circleGesture += cg.duration() + "\n";
                                FileHandler.appendToFile(currentGesturesCircle, circleGesture);
                                break;
                            case TYPE_SWIPE:
                                SwipeGesture sg = new SwipeGesture(g);
                                String swipeGesture = sg.startPosition().getX() + "," +
                                        sg.startPosition().getY() + "," +
                                        sg.startPosition().getZ() + ",";

                                swipeGesture += sg.position().getX() + "," +
                                        sg.position().getY() + "," +
                                        sg.position().getZ() + ",";

                                swipeGesture += sg.direction().getX() + "," +
                                        sg.direction().getY() + "," +
                                        sg.direction().getZ() + ",";

                                swipeGesture += sg.speed() + "\n";

                                FileHandler.appendToFile(currentGesturesSwipe, swipeGesture);
                                break;
                            case TYPE_SCREEN_TAP:
                                ScreenTapGesture stg = new ScreenTapGesture(g);

                                String screenTapGesture = stg.position().getX() + "," +
                                        stg.position().getY() + "," +
                                        stg.position().getZ() + ",";

                                screenTapGesture += stg.direction().getX() + "," +
                                        stg.direction().getY() + "," +
                                        stg.direction().getZ() + ",";

                                screenTapGesture += stg.progress();

                                FileHandler.appendToFile(currentGesturesScreenTap, screenTapGesture);
                                break;

                            case TYPE_KEY_TAP:
                                KeyTapGesture ktg = new KeyTapGesture(g);

                                String keyTapGesture = ktg.position().getX() + "," +
                                        ktg.position().getY() + "," +
                                        ktg.position().getZ() + ",";

                                keyTapGesture += ktg.direction().getX() + "," +
                                        ktg.position().getY() + "," +
                                        ktg.position().getZ() + ",";

                                keyTapGesture += ktg.progress() + "\n";

                                FileHandler.appendToFile(currentGesturesKeyTap, keyTapGesture);
                                break;
                        }
                    }
                }
                if (gestureString.length() == 0){
                    gestureString = Gesture.Type.TYPE_INVALID.toString();
                } else {
                    gestureString = gestureString.substring(0, gestureString.length()-1);
                }
                FileHandler.appendToFile(currentGestures, gestureString + " ");
            } catch (IOException e) {
                e.printStackTrace(App.out);
            }

            if (Properties.SEQUENCE) {
                writeFramesInSequence(frame);
            }

            for (Hand h : frame.hands()) {
                //write hands out to file
                if (h.isValid() && h.isRight()) {

                    if (SHOW_GUI) {
                        frame = HandFactory.injectHandIntoFrame(frame,
                                HandFactory.createHand(HandFactory.handToString("hand", h), frame));
                        display.setFrame(frame);
                    }

                    String uniqueId = System.currentTimeMillis() + "@"
                            + ManagementFactory.getRuntimeMXBean().getName();
                    String frameAsString = HandFactory.handToString(uniqueId, h);
                    try {
                        if (currentHands == null) {
                            currentHands = new File(FileHandler.generateFileWithName(filenameStart) + addition + "ms.pool_joint_positions");
                            currentHands.getParentFile().mkdirs();
                            currentHands.createNewFile();
                        }
                        FileHandler.appendToFile(currentHands, frameAsString + "\n");
                        if (currentSequence == null) {
                            currentSequence = new File(FileHandler.generateFileWithName(filenameStart) + addition + "ms.sequence_hand_data");
                            currentSequence.getParentFile().mkdirs();
                            currentSequence.createNewFile();
                        }
                        FileHandler.appendToFile(currentSequence, uniqueId + ",");

                        if (currentPosition == null) {
                            currentPosition = new File(FileHandler.generateFileWithName(filenameStart) + addition + "ms.pool_hand_positions");
                            currentPosition.getParentFile().mkdirs();
                            currentPosition.createNewFile();
                        }
                        String position = uniqueId + "," + h.palmPosition().getX() + ","
                                + h.palmPosition().getY() + "," + h.palmPosition().getZ() + "\n";
                        FileHandler.appendToFile(currentPosition, position);

                        if (currentRotation == null) {
                            currentRotation = new File(FileHandler.generateFileWithName(filenameStart) + addition + "ms.pool_hand_rotations");
                            currentRotation.getParentFile().mkdirs();
                            currentRotation.createNewFile();
                        }

                        String rotation = uniqueId + ",";
                        Vector[] vectors = new Vector[3];
                        vectors[0] = h.basis().getXBasis();
                        vectors[1] = h.basis().getYBasis();
                        vectors[2] = h.basis().getZBasis();
                        for (Vector v : vectors) {
                            rotation += v.getX() + "," + v.getY() + "," + v.getZ() + ",";
                        }
                        rotation.substring(0, rotation.length() - 1);
                        rotation += "\n";
                        FileHandler.appendToFile(currentRotation, rotation);

                        if (currentDct == null) {
                            currentDct = new File(FileHandler.generateFileWithName(filenameStart) + addition + "ms.pool_dct");
                            currentDct.getParentFile().mkdirs();
                            currentDct.createNewFile();
                        }

                        BufferedImage original = ScreenGrabber.captureRobot();

                        final int COMPRESSION = 8;

                        BufferedImage bi = new BufferedImage(original.getWidth()/COMPRESSION, original.getHeight()/COMPRESSION, original.getType());

                        Graphics2D g = bi.createGraphics();

                        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR);


                        g.drawImage(original, 0, 0, original.getWidth()/COMPRESSION, original.getHeight()/COMPRESSION, 0, 0, original.getWidth(), original.getHeight(), null);

                        g.dispose();

                        //
                        int[] data = ((DataBufferInt) bi.getRaster().getDataBuffer()).getData();

                        double[] dImage = new double[data.length];

                        ArrayList<Point> changes = new ArrayList<Point>();

                        int blocks = bi.getWidth() / DiscreteCosineTransformer.BLOCKS;

                        //Change contrast by this amount (0 to disable)
                        final int contrastIterations = 0;

                        ArrayList<Integer> xBlocks = new ArrayList<Integer>();
                        for (int i = 0; i < data.length; i++){
                            int blackAndWhite = data[i];
                            blackAndWhite = (((blackAndWhite >> 16) & 0x0FF) + ((blackAndWhite >> 8) & 0x0FF) + (blackAndWhite & 0x0FF))/3;
                            //blackAndWhite = ((blackAndWhite&0x0ff)<<16)|((blackAndWhite&0x0ff)<<8)|(blackAndWhite&0x0ff);

                            for (int s = 0; s < contrastIterations; s++){
                                blackAndWhite = (int)(255 * (1 + Math.sin((((blackAndWhite) * Math.PI)/255d) - Math.PI/2d)));
                            }

                            dImage[i] = blackAndWhite;
                            if (lastImage != null) {
                                int y =  i / bi.getWidth();
                                int x = i - (y * bi.getWidth());
                                int block = (y / DiscreteCosineTransformer.BLOCKS * blocks) + (x / DiscreteCosineTransformer.BLOCKS);
                                if (!xBlocks.contains(i)) {
                                    int li = (int) lastImage[i];
                                    int di = blackAndWhite;
                                    if (li != di) {
                                        if (!xBlocks.contains(block)) {
                                            xBlocks.add(block);
                                        }
                                    }
                                }
                            }

                        }

                        for (int i : xBlocks){
                            int y = i / blocks;
                            int x = i - (y * blocks);
                            changes.add(new Point(x, y));
                        }


                        if (lastImage == null) {


                            dct = new DiscreteCosineTransformer(dImage, bi.getWidth(), bi.getHeight());

                            dct.calculateDct();


                        } else {

                            dct.updateImage(dImage);

                            dct.calculateDctFromChanges(changes);
                        }

                        lastImage = dImage;


                       double[] transform = dct.inverse(1);




                        int width = bi.getWidth();
                        int height = bi.getHeight();
                        BufferedImage compressed =  new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                        for (int i = 0; i < width - DiscreteCosineTransformer.BLOCKS; i++){
                            for (int j = 0; j < height - DiscreteCosineTransformer.BLOCKS; j++) {
                                int value = (int)(transform[(j*width) + i]);

                                value = 0xFF000000 | ((value&0x0ff)<<16)|((value&0x0ff)<<8)|(value&0x0ff);
                                compressed.setRGB(i, j, value);
                            }
                        }

                        double[] resultData = dct.getInterleavedData();

                        StringBuilder sb = new StringBuilder();
                        Integer[] thisState = new Integer[resultData.length];

                        int differences = 0;

                        int maxDifference = resultData.length;

                        for (int i = 0; i < resultData.length; i++){
                            thisState[i] = (int)(resultData[i] / COMPRESSION);
                            sb.append("," + thisState[i]);
                        }

                        for (Integer[] ss : states){
                            differences = 0;
                            for (int i = 0; i < resultData.length; i++){
                                if (ss[i] != thisState[i]){
                                    differences++;
                                }
                            }

                            if (differences < maxDifference){
                                maxDifference = differences;
                            }

                        }



                        String state = sb.toString();

                        sb.insert(0, uniqueId);

                        bi.flush();

//

                        int stateNumber = states.size();

                        //50% screen difference
                        double difference = maxDifference / (double)thisState.length;
                        App.out.println(maxDifference + " " + difference);
                        if (difference > 0.005 || states.size() == 0) {
                            states.add(thisState);
                            sb.append("\n");
                            FileHandler.appendToFile(currentDct, sb.toString());
                        } else {
                            stateNumber = states.indexOf(state);
                        }

                        ImageIO.write(compressed, "bmp", new File("STATE" + stateNumber + ".bmp"));




                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace(App.out);
                    }

                }
            }
            new Thread(){
                @Override
                public void start(){
                    final int bars = 60;
                    long total = BREAK_TIMES[BREAK_TIMES.length-1];
                    long done = time - startTime;
                    String progress = "[";
                    float percent = done / (float) total;
                    int b1 = (int) (percent * bars);
                    for (int i = 0; i < b1; i++) {
                        progress += "-";
                    }
                    progress += ">";
                    int b2 = bars - b1;
                    for (int i = 0; i < b2; i++) {
                        progress += " ";
                    }
                    progress += "] " + (int) (percent * 100) + "%";
                    out.print("\r" + progress);
                }
            }.start();
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


    private File sequenceFile = null;
    private int breakIndex = 0;

    public void writeFramesInSequence(Frame frame) {
        if (Properties.SEQUENCE) {
            String dir = "/sequences";
            try {
                boolean start = false;

                if (sequenceFile == null) {
                    String addition = "-" + BREAK_TIMES[breakIndex];
                    sequenceFile = new File(FileHandler.generateFileWithName(filenameStart) + addition + "ms.raw_frame_data");
                    sequenceFile.getParentFile().mkdirs();
                    sequenceFile.createNewFile();
                    //com.sheffield.leapmotion.FileHandler.appendToFile(sequenceFile, "[");
                    start = true;
                }
                //SamplerApp.out.println("Writing to file: " + sequenceFile.getAbsolutePath());
                String content = Serializer.sequenceToJson(frame);
                if (!start) {
                    content = "\n" + content;
                }
                FileHandler.appendToFile(sequenceFile, content);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                SamplerApp.out.println("Writing failed!");
                e.printStackTrace();
            }

        }

    }

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
