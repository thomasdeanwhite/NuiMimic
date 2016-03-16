package com.sheffield.leapmotion.sampler.com.sheffield.leapmotion.sampler.output;

import com.sheffield.imageprocessing.DiscreteCosineTransformer;
import com.sheffield.instrumenter.states.ScreenGrabber;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by thomas on 15/03/2016.
 */
public class DctStateComparator {
    private static double[] lastImage;
    private static DiscreteCosineTransformer dct;

    private static ArrayList<Integer[]> states;

    private static final boolean WRITE_SCREENSHOTS_TO_FILE = true;

    static {
        states = new ArrayList<Integer[]>();
    }


    public static void addState(Integer[] state){
        states.add(state);
    }

    public static String captureState(){
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

        long maxDifference = resultData.length * 255;

        for (int i = 0; i < resultData.length; i++) {
            thisState[i] = (int) resultData[i];
            sb.append("," + thisState[i]);
        }


        int closestState = -1;

        for (int i = 0; i < states.size(); i++) {
            Integer[] ss = states.get(i);
            differences = 0;
            for (int j = 0; j < resultData.length; j++) {
                differences += Math.abs(ss[j] - thisState[j]);
            }

            if (differences < maxDifference) {
                maxDifference = differences;
                closestState = i;
            }

        }

//

        int stateNumber = states.size();

        //50% screen difference
        double difference = maxDifference / (double) (255 * thisState.length);
        if (difference > 0.1 || states.size() == 0) {
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
                    ImageIO.write(compressed, "png", new File("STATE" + stateNumber + ".png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            compressed.flush();

            compressed = null;

            states.add(thisState);
            return sb.toString().substring(1);
        }
        return null;
    }

}
