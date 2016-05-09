package com.sheffield.leapmotion.results;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sheffield.instrumenter.analysis.ClassAnalyzer;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.LineHit;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.ClassTracker;
import com.sheffield.leapmotion.FileHandler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by thomas on 19/04/2016.
 */
public class ResultProcessor {

    private static int relatedLines = 0;
    private static int relatedBranches = 0;

    private static ArrayList<ClassTracker> relatedClasses = new ArrayList<ClassTracker>();

    public static void main(String... args) throws IOException {
        try {
            if (args.length < 3) {
                throw new IllegalArgumentException("Please use 3 arguments:\n\t" +
                        "{Lines File} " +
                        "{Related Information File} " +
                        "{Covered Lines File} ");
            }

            Gson g = new Gson();

            File linesFile = new File(args[0]);
            if (linesFile.getAbsoluteFile().exists()) {
                try {
                    String linesString = FileHandler.readFile(linesFile);

                    Type mapType = new TypeToken<Map<Integer, Map<Integer, LineHit>>>() {
                    }.getType();
                    ClassAnalyzer.setLines((Map<Integer, Map<Integer, LineHit>>) g.fromJson(linesString, mapType));

                    App.out.println("- Found lines file at: " + linesFile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            File relatedFile = new File(args[1]);
            if (relatedFile.getAbsoluteFile().exists()) {
                try {
                    String[] classes = FileHandler.readFile(relatedFile).split("\n");
                    ArrayList<ClassTracker> clas = new ArrayList<ClassTracker>(classes.length - 1);
                    for (int i = 1; i < classes.length; i++) {
                        if (classes[i].length() > 0) {
                            String[] clInfo = classes[i].split(",");
                            int lines = Integer.parseInt(clInfo[1]);
                            relatedLines += lines;
                            int brans = Integer.parseInt(clInfo[2]);
                            relatedBranches += brans;
                            clas.add(new ClassTracker(clInfo[0], lines, brans));
                        }
                    }
                    relatedClasses = clas;

                    App.out.println("- Found related classes file at: " + linesFile.getAbsolutePath());
                    App.out.println("[" + relatedLines + " related lines, " + relatedBranches + " related branches]");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            File linesCovered = new File(args[2] + ".lines_covered.csv");
            File classMap = new File(args[2] + ".classes.csv");

            HashMap<String, ArrayList<Integer>> lines = new HashMap<String, ArrayList<Integer>>();
            if (linesCovered.getAbsoluteFile().exists() &&
                    classMap.getAbsoluteFile().exists()) {

                String[] classes = FileHandler.readFile(classMap).split("\n");

                HashMap<Integer, String> cMap = new HashMap<Integer, String>(classes.length);

                for (String s : classes) {
                    if (s.length() > 0) {
                        String[] i = s.split(":");
                        cMap.put(Integer.parseInt(i[1]), i[0]);
                        lines.put(i[0], new ArrayList<Integer>());
                    }
                }

                String[] l = FileHandler.readFile(linesCovered).split("\n");

                //read last value
                int trim = 1;
                String lineInfo = "";

                while (lineInfo.length() == 0) {
                    lineInfo = l[l.length - trim];
                    trim++;
                    if (trim > l.length) {
                        throw new Exception(linesCovered + " is in an invalid format (maybe empty).");
                    }
                }

                String[] lin = lineInfo.split(";");

                for (String s : lin) {
                    String[] i = s.split("#");
                    String clazz = cMap.get(Integer.parseInt(i[0]));

                    lines.get(clazz).add(Integer.parseInt(i[1]));
                }
            }

            Iterator s = lines.keySet().iterator();

            ArrayList<String> classes = new ArrayList<String>();

            while (s.hasNext()) classes.add((String) s.next());

            Collections.sort(classes);

            String input = "<html><head><style>" +
                    "div { margin: 0px 3px; padding: 0px 3px; font-size:10px; } " +
                    "body { font-family: Arial; }" +
                    "</style></head><body>" +
                    "<div style='width: 80%; margin-left: auto;" +
                    "margin-right: auto;'>";

            for (String c : classes) {
                String cName = c;
                if (cName.length() > 30){
                    cName = cName.substring(c.length()-30, c.length());
                }
                input += "<div style='width:100%;float:left'>" +
                        "<div style='width:30%;text-align:right;float:left;'>" +
                        cName + "</div><div style='width:67%;text-align:left;float:right;'>";
                ArrayList<Integer> l = lines.get(c);
                float percent = 0;
                for (Integer i : l) {
                    percent++;
                }
                percent /= ClassAnalyzer.getCoverableLines(c).size();
                final float WIDTH = 70f;
                int width = (int) Math.floor(WIDTH * percent);
                input += "<span style='float:left; width:" + width + "%; background-color: #00ff00; overflow:hidden;'>" +
                        "&nbsp;</span>" +
                        "<span style='float:left; width:" + (int) (WIDTH - width) + "%; background-color:#ff0000; overflow:hidden;'>" +
                        "&nbsp;</span>" + (Math.round(percent * 1000) / 10f) + "%" +
                        "</div></div>";
            }

            input += "</div></body></html>";

            File output = new File(classMap.getParentFile(), "processed_results-" + System.currentTimeMillis() + ".html");

            if (!output.exists()) {
                output.createNewFile();
            }

            FileHandler.writeToFile(output, input);
        } catch (Throwable e){
            e.printStackTrace();
        }


    }
}
