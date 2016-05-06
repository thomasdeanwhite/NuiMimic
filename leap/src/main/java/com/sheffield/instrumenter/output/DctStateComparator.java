package com.sheffield.instrumenter.output;

import com.sheffield.imageprocessing.DiscreteCosineTransformer;
import com.sheffield.instrumenter.states.ScreenGrabber;
import com.sheffield.leapmotion.Properties;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by thomas on 15/03/2016.
 */
public class DctStateComparator {
    private static double[] lastImage;
    private static DiscreteCosineTransformer dct;

    public static int statesFound = 0;

    private static ArrayList<Integer[]> states;

    private static final boolean ONLY_WRITE_SCREENSHOT = false;

    private static final boolean WRITE_SCREENSHOTS_TO_FILE = true;

    private static int currentState = -1;

    public static String SCREENSHOT_DIRECTORY = "testing_output/screenshots";

    public static HashMap<Integer, Integer> statesVisited = new HashMap<Integer, Integer>();

    private static float threshold = 0.1f;

    private static ArrayList<Integer> statesActuallyVisited = new ArrayList<Integer>();

    static {
        states = new ArrayList<Integer[]>();
    }

    public static int getCurrentState (){
        return currentState;
    }

    public void setThreshold(float th){
        threshold = th;
    }

    public static int addState(Integer[] state){
        if (states.contains(state)){
            return -1;
        }
        states.add(state);
        int index = states.lastIndexOf(state);
        statesVisited.put(index, 0);
        return index;
    }

    public static String captureState(){
        BufferedImage original = ScreenGrabber.captureRobot();

        final int COMPRESSION = 4;

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

        int width = bi.getWidth();
        int height = bi.getHeight();

        bi.flush();

        bi = null;

        System.gc();

        double[] dImage = new double[data.length];

        ArrayList<Point> changes = new ArrayList<Point>();

        int blocks = width / DiscreteCosineTransformer.BLOCKS;

        //Change contrast by this amount (0 to disable)
        final int contrastIterations = 0;

        ArrayList<Integer> xBlocks = new ArrayList<Integer>();
        for (int i = 0; i < data.length; i++) {
            int blackAndWhite = data[i];
            blackAndWhite = (int)((0.3*((blackAndWhite >> 16) & 0x0FF) + 0.59*((blackAndWhite >> 8) & 0x0FF) + 0.11*(blackAndWhite & 0x0FF)));

            for (int s = 0; s < contrastIterations; s++) {
                blackAndWhite = (int) (255 * (1 + Math.sin((((blackAndWhite) * Math.PI) / 255d) - Math.PI / 2d)));
            }

            dImage[i] = blackAndWhite;
            if (lastImage != null) {
                int y = i / width;
                int x = i - (y * width);
                int block = (y / DiscreteCosineTransformer.BLOCKS * blocks) + (x / DiscreteCosineTransformer.BLOCKS);
                if (!xBlocks.contains(block)) {
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


            dct = new DiscreteCosineTransformer(dImage, width, height);

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

        long differences = 0;

        long maxDifference = resultData.length * 2048;

        for (int i = 0; i < resultData.length; i++) {
            thisState[i] = (int) resultData[i];
            sb.append("," + thisState[i]);
        }


        int closestState = -1;

        for (int i = 0; i < states.size(); i++) {
            Integer[] ss = states.get(i);
            differences = 0;
            int limit = Math.min(resultData.length, ss.length);
            for (int j = 0; j < limit; j++) {
                int result = ss[j];
                int state = thisState[j];
                differences += Math.abs(result - state);
            }

            ///App.out.println(i + ":" + ((float)differences/(float)resultData.length));

            if (differences < maxDifference) {
                maxDifference = differences;
                closestState = i;
            }

        }

//

        int stateNumber = states.size();

        //10% screen difference
        double difference = maxDifference / (double) (255 * thisState.length);

        if ((difference > threshold || states.size() == 0) || ONLY_WRITE_SCREENSHOT) {
            BufferedImage compressed = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    int value = (int) (transform[(j * width) + i]);

                    value = 0xFF000000 | ((value & 0x0ff) << 16) | ((value & 0x0ff) << 8) | (value & 0x0ff);
                    compressed.setRGB(i, j, value);
                }
            }

            if (WRITE_SCREENSHOTS_TO_FILE) {
                try {
                    File f = new File(SCREENSHOT_DIRECTORY + "/" + Properties.CURRENT_RUN + "/" + "STATE" + stateNumber + ".png");
                    if (f.getParentFile() != null)
                        f.getParentFile().mkdirs();
                    ImageIO.write(compressed, "png", f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            compressed.flush();

            compressed = null;
            currentState = states.size();
            addState(thisState);
            statesVisited.put(currentState, 1);
            statesActuallyVisited.add(currentState);
            statesFound++;
            return sb.toString().substring(1);
        } else if (difference <= threshold) {
            if (currentState != closestState) {
                currentState = closestState;
            }
            statesVisited.put(currentState, statesVisited.get(currentState)+1);
            if (!statesActuallyVisited.contains(currentState)){
                statesActuallyVisited.add(currentState);
            }
        }
        return null;
    }

    public static ArrayList<Integer> getStatesVisited (){
        return statesActuallyVisited;
    }

}
