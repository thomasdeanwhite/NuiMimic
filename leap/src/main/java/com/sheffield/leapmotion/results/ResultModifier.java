package com.sheffield.leapmotion.results;

import com.sheffield.leapmotion.util.ClassTracker;
import com.sheffield.leapmotion.util.FileHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by thomas on 19/04/2016.
 */
public class ResultModifier {

    private static int relatedLines = 0;
    private static int relatedBranches = 0;

    private static ArrayList<ClassTracker> relatedClasses = new ArrayList<ClassTracker>();

    public static void main(String... args) throws IOException {
        if (args.length < 1) {
            throw new IllegalArgumentException("Please use 1 arguments:\n\t" +
                    "{Results Directory}");
        }

        File dir = new File(args[0]);

        for (String p : dir.list()){
            if (p.endsWith("-test-results.csv")){
                File file = new File(dir + "/" + p);
                String content = FileHandler.readFile(file);
                String[] lines = content.split("\n");
                String newLines = lines[0] + "\n";
                //i = 1 to ignore headers
                for (int i = 1; i < lines.length; i++){
                    String line = lines[i];
                    if (line.endsWith("500")){
                        line = line.substring(0, line.length()-3) + "," + line.substring(line.length()-3, line.length());
                    } else {
                        line = line.substring(0, line.length()-1) + "," + line.substring(line.length()-1, line.length());
                    }

                    int fs = line.indexOf("RANDOM");

                    if (fs == -1){
                        fs = line.indexOf("VQ");
                    }

                    line = line.substring(0, fs) + "," + line.substring(fs, line.length());
                    newLines += line + "\n";
                }

                FileHandler.writeToFile(file, newLines);
            }
        }


    }
}
