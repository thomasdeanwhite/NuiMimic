package com.sheffield.leapmotion.sampler.com.sheffield.leapmotion.sampler.output;

import com.leapmotion.leap.*;
import com.leapmotion.leap.Frame;
import com.sheffield.imageprocessing.DiscreteCosineTransformer;
import com.sheffield.instrumenter.Properties;
import com.sheffield.instrumenter.states.ScreenGrabber;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.sampler.FileHandler;
import com.sheffield.leapmotion.sampler.SamplerApp;
import com.sheffield.leapmotion.sampler.Serializer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by thomas on 11/03/2016.
 */
public class FrameDeconstructor {

    private String uniqueId = "";
    private String filenameStart = "";
    private String addition = "";


    private File currentDct;
    private File currentSequence;
    private File currentHands;
    private File currentPosition;
    private File currentRotation;
    private File currentGestures;
    private File currentGesturesCircle;
    private File currentGesturesSwipe;
    private File currentGesturesScreenTap;
    private File currentGesturesKeyTap;
    private File sequenceFile = null;

    private int breakIndex = 0;

    private ArrayList<Integer[]> states;

    private static double[] lastImage;
    private DiscreteCosineTransformer dct;

    //1 state capture/second
    private static final int stateCaptureTime = 5000;
    private long lastStateCapture = 0;

    public FrameDeconstructor (){
        states = new ArrayList<Integer[]>();
    }

    public void setUniqueId(String uId){
        uniqueId = uId;
    }

    public void setFilenameStart(String fns){
        filenameStart = fns;
    }

    public void setAddition(String add){
        addition = add;
    }

    public void resetFiles(int breakIndex){
        sequenceFile = null;
        currentHands = null;
        currentRotation = null;
        currentPosition = null;
        currentSequence = null;
        currentGestures = null;
        currentDct = null;

        this.breakIndex = breakIndex;
    }

    public void outputRawFrameData(Frame frame){
        if (Properties.SEQUENCE) {
            String dir = "/sequences";
            try {
                boolean start = false;

                if (sequenceFile == null) {
                    String addition = "-" + SamplerApp.BREAK_TIMES[breakIndex];
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

    public void outputSequence() throws IOException {
        if (currentSequence == null) {
            currentSequence = new File(FileHandler.generateFileWithName(filenameStart) + addition + "ms.sequence_hand_data");
            currentSequence.getParentFile().mkdirs();
            currentSequence.createNewFile();
        }
        FileHandler.appendToFile(currentSequence, uniqueId + ",");
    }

    public void outputJointPositionModel(String frameAsString) throws IOException {
        if (currentHands == null) {
            currentHands = new File(FileHandler.generateFileWithName(filenameStart) + addition + "ms.pool_joint_positions");
            currentHands.getParentFile().mkdirs();
            currentHands.createNewFile();
        }
        FileHandler.appendToFile(currentHands, frameAsString + "\n");
    }

    public void outputHandPositionModel(Hand h) throws IOException {
        if (currentPosition == null) {
            currentPosition = new File(FileHandler.generateFileWithName(filenameStart) + addition + "ms.pool_hand_positions");
            currentPosition.getParentFile().mkdirs();
            currentPosition.createNewFile();
        }
        String position = uniqueId + "," + h.palmPosition().getX() + ","
                + h.palmPosition().getY() + "," + h.palmPosition().getZ() + "\n";
        FileHandler.appendToFile(currentPosition, position);
    }

    public void outputHandRotationModel(Hand h) throws IOException {

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
    }

    public void outputCurrentState() throws IOException {
        if (lastStateCapture + stateCaptureTime < System.currentTimeMillis()) {
            App.out.print(" Capturing State");
            lastStateCapture = System.currentTimeMillis();
            if (currentDct == null) {
                currentDct = new File(FileHandler.generateFileWithName(filenameStart) + addition + "ms.pool_dct");
                currentDct.getParentFile().mkdirs();
                currentDct.createNewFile();
            }

            BufferedImage original = ScreenGrabber.captureRobot();

            final int COMPRESSION = 2;

            BufferedImage bi = new BufferedImage(original.getWidth() / COMPRESSION, original.getHeight() / COMPRESSION, original.getType());

            Graphics2D g = bi.createGraphics();

            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);


            g.drawImage(original, 0, 0, original.getWidth() / COMPRESSION, original.getHeight() / COMPRESSION, 0, 0, original.getWidth(), original.getHeight(), null);

            g.dispose();

            original.flush();

            original = null;

            //
            int[] data = ((DataBufferInt) bi.getRaster().getDataBuffer()).getData();

            double[] dImage = new double[data.length];

            ArrayList<Point> changes = new ArrayList<Point>();

            int blocks = bi.getWidth() / DiscreteCosineTransformer.BLOCKS;

            //Change contrast by this amount (0 to disable)
            final int contrastIterations = 0;

            ArrayList<Integer> xBlocks = new ArrayList<Integer>();
            for (int i = 0; i < data.length; i++) {
                int blackAndWhite = data[i];
                blackAndWhite = (((blackAndWhite >> 16) & 0x0FF) + ((blackAndWhite >> 8) & 0x0FF) + (blackAndWhite & 0x0FF)) / 3;
                //blackAndWhite = ((blackAndWhite&0x0ff)<<16)|((blackAndWhite&0x0ff)<<8)|(blackAndWhite&0x0ff);

                for (int s = 0; s < contrastIterations; s++) {
                    blackAndWhite = (int) (255 * (1 + Math.sin((((blackAndWhite) * Math.PI) / 255d) - Math.PI / 2d)));
                }

                dImage[i] = blackAndWhite;
                if (lastImage != null) {
                    int y = i / bi.getWidth();
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

            for (int i : xBlocks) {
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

            double[] resultData = dct.getInterleavedData();

            StringBuilder sb = new StringBuilder();
            Integer[] thisState = new Integer[resultData.length];

            int differences = 0;

            int maxDifference = resultData.length;

            for (int i = 0; i < resultData.length; i++) {
                thisState[i] = (int) resultData[i];
                sb.append("," + thisState[i]);
            }


            int closestState = -1;

            for (int i = 0; i < states.size(); i++) {
                Integer[] ss = states.get(i);
                differences = 0;
                for (int j = 0; j < resultData.length; j++) {
                    if (ss[j] != thisState[j]) {
                        differences++;
                    }
                }

                if (differences < maxDifference) {
                    maxDifference = differences;
                    closestState = i;
                }

            }


            String state = sb.toString();

            sb.insert(0, uniqueId);

//

            int stateNumber = states.size();

            //50% screen difference
            double difference = maxDifference / (double) thisState.length;
            //App.out.println(maxDifference + " " + difference);
            if (difference > 0.1 || states.size() == 0) {
                int width = bi.getWidth();
                int height = bi.getHeight();
                BufferedImage compressed = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                for (int i = 0; i < width - DiscreteCosineTransformer.BLOCKS; i++) {
                    for (int j = 0; j < height - DiscreteCosineTransformer.BLOCKS; j++) {
                        int value = (int) (transform[(j * width) + i]);

                        value = 0xFF000000 | ((value & 0x0ff) << 16) | ((value & 0x0ff) << 8) | (value & 0x0ff);
                        compressed.setRGB(i, j, value);
                    }
                }

                ImageIO.write(compressed, "bmp", new File("STATE" + stateNumber + ".bmp"));

                compressed.flush();

                compressed = null;

                states.add(thisState);
                sb.append("\n");
                FileHandler.appendToFile(currentDct, sb.toString());
            } else {
                stateNumber = closestState;
            }

            bi.flush();

            bi = null;

            System.gc();
        }
    }

    public void outputGestureModel(Frame frame) throws IOException {
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
    }


}
