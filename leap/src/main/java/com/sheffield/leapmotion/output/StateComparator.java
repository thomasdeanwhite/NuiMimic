package com.sheffield.leapmotion.output;

import static com.sheffield.leapmotion.Properties.*;

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
public class StateComparator {

    public static int statesFound = 0;

    private static ArrayList<Integer[]> states;

    private static final boolean ONLY_WRITE_SCREENSHOT = false;

    private static final boolean WRITE_SCREENSHOTS_TO_FILE = true;

    private static int currentState = -1;

    public static String SCREENSHOT_DIRECTORY = "testing_output/screenshots";

    public static HashMap<Integer, Integer> statesVisited =
            new HashMap<Integer, Integer>();


    private static ArrayList<Integer> statesActuallyVisited =
            new ArrayList<Integer>();

    static {
        states = new ArrayList<Integer[]>();
    }

    public static int getCurrentState() {
        return currentState;
    }

    public Integer[] getState(int state) {
        return states.get(state);
    }

    /**
     * Returns if s1 == s2. Note for efficienc, percentage difference is
     * calculated off
     * s1 only, so sum(s1) != sum(s2), isSameState(s1, s2) may not be equals to
     * isSameState(s2, s1).
     *
     * @param s1
     * @param s2
     * @return
     */
    public boolean isSameState(Integer[] s1, Integer[] s2) {
        int difference = calculateStateDifference(s1, s2);

        return isSameState(difference, sum(s1));
    }

    /**
     * returns candidates in s
     *
     * @param s
     * @return
     */
    public static int sum(Integer[] s) {
        int total = 0;
        for (int i : s) {
            total += i;
        }
        return total;
    }


    private static boolean isSameState(int difference, int size) {
        double diffPercentage = difference / (double) size;

        //App.out.println(difference);

        return diffPercentage < HISTOGRAM_THRESHOLD;
    }

    public static int calculateStateDifference(Integer[] s1, Integer[] s2) {
        int differences = 0;
        int limit = Math.min(s1.length, s2.length);
        for (int j = 0; j < limit; j++) {
            int result = s2[j];
            int s = s1[j];
            differences += Math.abs(result - s);
        }
        return differences;
    }

    public static int addState(Integer[] state) {
        if (HISTOGRAM_BINS < state.length) {
            Integer[] newState = new Integer[HISTOGRAM_BINS];

            for (int i = 0; i < newState.length; i++) {
                newState[i] = 0;
            }

            float mod = (float) (HISTOGRAM_BINS) / (float) state.length;

            for (int i = 0; i < state.length; i++) {
                int index = (int) (i * mod);
                newState[index] += state[i];
            }

            state = newState;
        }
        int closestState = -1;

        int maxDifference = Integer.MAX_VALUE;

        int totalValues = sum(state);

        for (int i = 0; i < states.size(); i++) {
            Integer[] ss = states.get(i);
            int differences = 0;
            int limit = Math.min(state.length, ss.length);
            for (int j = 0; j < limit; j++) {
                int result = ss[j];
                int s = state[j];
                differences += Math.abs(result - s);
                totalValues += ss[j];
            }

            ///App.out.println(i + ":" + ((float)differences/(float)
            // resultData.length));

            if (differences < maxDifference) {
                maxDifference = differences;
                closestState = i;
            }

        }

//

        int stateNumber = states.size();

        if (isSameState(maxDifference, totalValues) || states.size() == 0) {
            statesVisited.put(stateNumber, 0);
            states.add(state);
        } else {
            stateNumber = closestState;
        }
        return stateNumber;
    }

    public static int changeContrast(int blackAndWhite, int iterations) {
        for (int s = 0; s < iterations; s++) {
            blackAndWhite = (int) (255 * (1 + Math.sin(
                    (((blackAndWhite) * Math.PI) / 255d) - Math.PI / 2d)));
        }
        return blackAndWhite;
    }

    /**
     * Captures the current screen and returns it as a JSON Integer Array
     *
     * @return
     */
    public static String captureState() {
        BufferedImage screenShot = null;
        try {
            Robot robot = new Robot();
            screenShot = robot.createScreenCapture(
                    new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        } catch (AWTException e) {
            e.printStackTrace();
        }
        return captureState(screenShot);
    }

    public static String captureState(BufferedImage newState) {

        BufferedImage bi =
                new BufferedImage(newState.getWidth() / SCREENSHOT_COMPRESSION,
                        newState.getHeight() / SCREENSHOT_COMPRESSION,
                        newState.getType());

        Graphics2D g = bi.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);


        g.drawImage(newState, 0, 0,
                newState.getWidth() / SCREENSHOT_COMPRESSION,
                newState.getHeight() / SCREENSHOT_COMPRESSION, 0, 0,
                newState.getWidth(), newState.getHeight(), null);

        g.dispose();

        newState.flush();

        newState = null;

        //
        int[] data = ((DataBufferInt) bi.getRaster().getDataBuffer()).getData();

        int width = bi.getWidth();
        int height = bi.getHeight();

        bi.flush();

        bi = null;

        System.gc();

        double[] dImage = new double[data.length];

        ArrayList<Point> changes = new ArrayList<Point>();

        ArrayList<Integer> xBlocks = new ArrayList<Integer>();
        for (int i = 0; i < data.length; i++) {
            int blackAndWhite = data[i];
            blackAndWhite = (int) ((0.3 * ((blackAndWhite >> 16) & 0x0FF) +
                    0.59 * ((blackAndWhite >> 8) & 0x0FF) +
                    0.11 * (blackAndWhite & 0x0FF)));

            dImage[i] = blackAndWhite;

        }


        Integer[] bins = new Integer[HISTOGRAM_BINS];
        for (int i = 0; i < bins.length; i++) {
            bins[i] = 0;
        }

        float mod = ((float) (HISTOGRAM_BINS - 1) / 51f);
        for (int i = 0; i < dImage.length; i++) {
            bins[(int) (dImage[i] * mod)]++;
        }

        int closestState = -1;

        int maxDifference = Integer.MAX_VALUE;

        int totalValues = 0;


        for (int i = 0; i < bins.length; i++) {
            totalValues += bins[i];
        }

        for (int i = 0; i < states.size(); i++) {
            Integer[] ss = states.get(i);
            int differences = calculateStateDifference(bins, ss);

            ///App.out.println(i + ":" + ((float)differences/(float)
            // resultData.length));

            if (differences < maxDifference) {
                maxDifference = differences;
                closestState = i;
            }

        }
        //

        int stateNumber = states.size();

        double difference =
                maxDifference / (double) (totalValues / (states.size() + 1));

        //App.out.println(difference);

        if ((difference > HISTOGRAM_THRESHOLD || states.size() == 0) ||
                ONLY_WRITE_SCREENSHOT) {
            BufferedImage compressed = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_RGB);
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    int value = (int) (data[(j * width) + i]);

                    value = 0xFF000000 | ((value & 0x0ff) << 16) |
                            ((value & 0x0ff) << 8) | (value & 0x0ff);
                    compressed.setRGB(i, j, value);
                }
            }

            if (WRITE_SCREENSHOTS_TO_FILE) {
                try {
                    File f = new File(
                            SCREENSHOT_DIRECTORY + "/" + CURRENT_RUN + "/" +
                                    "STATE" + stateNumber + ".png");
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
            addState(bins);
            statesVisited.put(currentState, 1);
            statesActuallyVisited.add(currentState);
            statesFound++;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bins.length; i++) {
                sb.append(bins[i] + ",");
            }
            String output = sb.toString();
            return output.substring(0, output.length() - 1);
        } else if (difference <= HISTOGRAM_THRESHOLD) {
            if (currentState != closestState) {
                currentState = closestState;
            }
            statesVisited
                    .put(currentState, statesVisited.get(currentState) + 1);
            if (!statesActuallyVisited.contains(currentState)) {
                statesActuallyVisited.add(currentState);
            }
        }
        return null;
    }

    public static ArrayList<Integer> getStatesVisited() {
        return statesActuallyVisited;
    }

}
