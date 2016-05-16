package com.sheffield.leapmotion.results;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.ClassTracker;
import com.sheffield.leapmotion.FileHandler;
import com.sheffield.leapmotion.Quaternion;
import com.sheffield.leapmotion.QuaternionHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by thomas on 19/04/2016.
 */
public class QuaternionConverter {

    private static int relatedLines = 0;
    private static int relatedBranches = 0;

    private static String[] colors = {"#880000", "#008888", "#888800", "#008800"};

    private static ArrayList<ClassTracker> relatedClasses = new ArrayList<ClassTracker>();

    public static void main(String... args) throws IOException {
        if (args.length < 1) {
            throw new IllegalArgumentException("Please use 1 arguments:\n\t" + "{Directory} ");
        }


        File dir = new File(args[0]);

        Controller c = new Controller();

        System.out.println("Starting conversion");

        String[] files = dir.list();

        for (String file : files) {
            try {
                if (file.endsWith(".pool_hand_rotations")) {
                    String content = FileHandler.readFile(new File(dir, file));

                    String[] lines = content.split("\n");

                    String output = "";

                    boolean write = true;


                    int counter = 0;

                    for (String s : lines) {
                        System.out.print("\rCurrent file: " + file + " ");
                        final int MAX_BARS = 40;
                        float percentage = (float) counter / (float) lines.length;
                        int bars = Math.round(MAX_BARS * percentage);

                        int negBars = MAX_BARS - bars;

                        System.out.print("[");

                        for (int i = 0; i < bars-1; i++){
                            System.out.print("=");
                        }
                        System.out.print(">");
                        for (int i = 0; i < negBars; i++){
                            System.out.print(" ");
                        }
                        System.out.print("] " + (Math.round(percentage * 1000)/10f));

                        String[] vect = s.split(",");

                        if (vect.length < 7){
                            write = false;
                            break;
                        }

                        String label = vect[0];
                        Vector[] vs = new Vector[3];
                        for (int i = 0; i < 3; i++) {
                            Vector v = new Vector();
                            int index = (i * 3) + 1;
                            v.setX(Float.parseFloat(vect[index]));
                            v.setY(Float.parseFloat(vect[index + 1]));
                            v.setZ(Float.parseFloat(vect[index + 2]));
                            vs[i] = v;
                        }
                        counter++;

                        Quaternion q = QuaternionHelper.toQuaternion(vs);

                        output += label + "," + q.toString() + "\n";
                    }

                    if (write) {
                        FileHandler.writeToFile(new File(dir, file + ".rotation_matrix"), content);
                        FileHandler.writeToFile(new File(dir, file), output);
                    }
                }
            } catch (IOException e) {

            }
        }


    }
}
