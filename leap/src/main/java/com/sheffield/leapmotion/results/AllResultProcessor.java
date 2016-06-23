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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by thomas on 19/04/2016.
 */
public class AllResultProcessor {

    private static int relatedLines = 0;
    private static int relatedBranches = 0;

    private static String[] colors = {"#880000", "#008888", "#888800", "#008800"};

    private static ArrayList<ClassTracker> relatedClasses = new ArrayList<ClassTracker>();

    public static void main(String... args) throws IOException {
        try {
            if (args.length < 3) {
                throw new IllegalArgumentException("Please use 3 arguments:\n\t" +
                        "{Lines File} " +
                        "{Related Information File} " +
                        "{Directory} ");
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

            File dir = new File(args[2]);

            HashMap<String, ArrayList<Integer>> overallLinesCovered = new HashMap<String, ArrayList<Integer>>();

            HashMap<String, HashMap<String, HashMap<Integer, Integer>>> methodLinesCovered = new HashMap<String, HashMap<String, HashMap<Integer, Integer>>>();

            String[] methodsUsed = {"VQ", "RANDOM", "SINGLE_MODEL", "STATE_DEPENDANT", "REPRODUCTION", "EMPTY", "USER_PLAYBACK", "UNKNOWN"};

            ArrayList<String> seenMethods = new ArrayList<String>();

            //class:(method:coverage)
            HashMap<String, HashMap<String, ArrayList<Float>>> classCoverage = new HashMap<String, HashMap<String, ArrayList<Float>>>();

            HashMap<String, String> shortenedClasses = new HashMap<String, String>();

            HashMap<String, Integer> methodQuantity = new HashMap<String, Integer>();

            HashMap<String, Float> methodAverageCoverage = new HashMap<String, Float>();


            for (String file : dir.list()) {

                if (!file.endsWith("test-results.csv")) {
                    continue;
                }

                File results = new File(args[2] + "/" + file);

                String resultsString = FileHandler.readFile(results);

                String[] resultsLines = resultsString.split("\n");

                String[] header = resultsString.split(",");

                int relatedLineIndex = 0;

                for (int i = 0; i < header.length; i++){
                    if (header[i].toLowerCase().contains("related_line_coverage")){
                        relatedLineIndex = i;
                        break;
                    }
                }

                String method = "";

                for (String s : methodsUsed) {
                    if (resultsString.contains(s)) {
                        method = s;
                        break;
                    }
                }

                if (method.length() == 0) {
                    method = "UNKNOWN";
                }

                for (int i = resultsLines.length-1; i > 0; i--){
                    if (resultsLines[i].trim().length() == 0){
                        continue;
                    } else {
                        String[] currentRow = resultsLines[i].split(",");
                        float coverage = Float.parseFloat(currentRow[relatedLineIndex]);
                        if (!methodAverageCoverage.containsKey(method)){
                            methodAverageCoverage.put(method, coverage);
                        } else {
                            methodAverageCoverage.put(method, methodAverageCoverage.get(method) + coverage);
                        }
                        break;
                    }
                }

                if (!methodQuantity.containsKey(method)){
                    methodQuantity.put(method, 1);
                } else {
                    methodQuantity.put(method, methodQuantity.get(method) + 1);
                }

                if (!seenMethods.contains(method)) {
                    seenMethods.add(method);
                }

                file = file.substring(0, file.length() - 4);

                File linesCovered = new File(args[2] + "/" + file + ".lines_covered.csv");
                File classMap = new File(args[2] + "/" + file + ".classes.csv");

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
                            new Exception(linesCovered + " is in an invalid format (maybe empty).").printStackTrace();
                            continue;
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


                for (String c : classes) {
                    String cName = c;
                    if (cName.length() > 50) {
                        cName = cName.substring(c.length() - 50, c.length());
                    }
                    if (cName.startsWith("/")) {
                        cName = cName.substring(1);
                    }

                    shortenedClasses.put(cName, c);
                    ArrayList<Integer> l = lines.get(c);
                    float percent = 0;
                    for (Integer i : l) {
                        if (!overallLinesCovered.containsKey(c)) {
                            overallLinesCovered.put(c, new ArrayList<Integer>());
                        }
                        overallLinesCovered.get(c).add(i);
                        if (!methodLinesCovered.containsKey(method)) {
                            methodLinesCovered.put(method, new HashMap<String, HashMap<Integer, Integer>>());
                        }
                        if (!methodLinesCovered.get(method).containsKey(c)) {
                            methodLinesCovered.get(method).put(c, new HashMap<Integer, Integer>());
                        }
                        if (!methodLinesCovered.get(method).get(c).containsKey(i)) {
                            methodLinesCovered.get(method).get(c).put(i, 0);
                        }
                        methodLinesCovered.get(method).get(c).put(i, methodLinesCovered.get(method).get(c).get(i) + 1);
                        percent++;
                    }
                    percent /= ClassAnalyzer.getCoverableLines(c).size();
                    if (!classCoverage.containsKey(cName)) {
                        classCoverage.put(cName, new HashMap<String, ArrayList<Float>>());
                    }

                    if (!classCoverage.get(cName).containsKey(method)) {
                        classCoverage.get(cName).put(method, new ArrayList<Float>());
                    }

                    classCoverage.get(cName).get(method).add(percent);


                }


            }

            String input = "<html><head><style>" +
                    "div { margin: 0px 3px; padding: 0px 3px; font-size:10px; } " +
                    "body { font-family: Arial; }" +
                    "</style></head><body>" +
                    "<div style='width: 80%; margin-left: auto;" +
                    "margin-right: auto;'>";

            input += "<div style='width:100%;float:left; border-bottom: 1px solid #000000;'>" +
                    "<div style='width:30%;text-align:right;float:left;padding:5px; font-weight: bold;'>Class Name</div><div style='width:67%;text-align:left;float:right;'>";

            int headerWidth = 95;
            int counter = 0;

            float avg = 0f;

            for (String s : seenMethods){
                float quant = (float)methodQuantity.get(s);
                float coverage = methodAverageCoverage.get(s);
                methodAverageCoverage.put(s, coverage/quant);
            }

            for (Float f : methodAverageCoverage.values()) {
                avg += f;
            }

            float mult = 1f / avg;


            for (String m : seenMethods) {
                input += "<span style='float:left; width:calc(" + Math.round(headerWidth*methodAverageCoverage.get(m)*mult) + "% - 10px); font-weight: bold; color:#ffffff; background-color: " + colors[counter++ % colors.length] + "; overflow:hidden;padding:5px;'>" +
                        m + " (" + (Math.round(methodAverageCoverage.get(m)*1000f)/10f) + "%)</span>";
            }

            input += "</div></div>";

            String outDir = System.currentTimeMillis() + "";
            for (String s : classCoverage.keySet()) {
                String className = shortenedClasses.get(s);
                System.out.print("\rProcessing " + className);
                File classIn = new File(args[2] + "/results/classes/" + className + ".java");
                File classout = new File(args[2] + "/results/" + outDir + "/" + className + ".html");

                int slashes = className.split("/").length;

                String backLink = "index.html";

                for (; slashes > 1; slashes--) {
                    backLink = "../" + backLink;
                }

                String classOut = "<html><head><style>" +
                        "div { margin: 0px 3px; padding: 0px 3px; font-size:10px; } " +
                        "body { font-family: Arial; }" +
                        "</style></head><body>" +
                        "<h1 style='width:100%; font-weight: bold; text-align: center;'>" + className + "</h3>" +
                        "<a href='" + backLink + "'><h1 style='width:100%; font-weight: bold; text-align: center;'>&lt; Back</h3></a>" +
                        "<div style='margin-left: auto; margin-right: auto; border: 1px solid #000; border-radius: 10px; padding: 30px;'>";

                if (classIn.exists()) {
                    //-4 for JD-GUI lines
                    String classContent = FileHandler.readFile(classIn);

                    String[] classLines = classContent.split("\n");

                    int startImports = 0;

                    int currentLine = 0;
                    for (int i = 0; i < classLines.length - 4; i++) {
                        String line = classLines[i];

                        line = line.replace("<", "&lt;").replace(">", "&gt;");

//                        String tempLine = "";
//                        while (line.length() > 80){
//                            tempLine += line.substring(0, 80) + "<br />";
//                            line = line.substring(80, line.length());
//                        }
//                        line = tempLine;

                        if (line.contains("import ") && startImports == 0) {
                            startImports = i;
                        }
                        if (line.contains("import ")) {
                            continue;
                        }
                        if (startImports != 0) {
                            classOut += "<div style='clear: both'>";
                            String newline = "<div style='background-color: inherit; font-align:center; width: 30px; text-align:right; border-right: 1px solid #000; float:left;'>" +
                                    startImports + "-" + (i - 1) + "</div><span style='margin-left: 5px'>&nbsp;</span>import *;";
                            startImports = 0;
                            classOut += newline;
                            classOut += "</div>";
                        }
                        classOut += "<div style='clear: both'>";


                        boolean covered = true;
                        if (line.trim().length() > 0) {

                            int lineIndex = line.indexOf("/*");

                            int endIndex = line.indexOf("*/");

                            if (lineIndex >= 0 && endIndex > 0) {
                                String lineNumString = line.substring(lineIndex + 2, endIndex).trim();
                                endIndex += 2;
                                int whiteSpace = endIndex + 1;
                                do {
                                    if (whiteSpace >= line.length()) {
                                        break;
                                    }
                                    whiteSpace++;
                                }
                                while (line.substring(endIndex, whiteSpace).trim().length() == 0);

                                if (whiteSpace >= line.length()) {
                                    classOut += "</div>";
                                    continue;
                                }

                                line = line.substring(0, endIndex) + line.substring(endIndex, whiteSpace).replace(" ", "<span style='margin-left: 5px'>&nbsp;</span>") +
                                        line.substring(whiteSpace, line.length());
                                line = line.substring(0, endIndex) + line.substring(endIndex, whiteSpace).replace("\t", "<span style'margin-left: 10px'>&nbsp;</span>") +
                                        line.substring(whiteSpace, line.length());

                                line = line.replace("/*", "<div style='background-color: inherit; font-align:center; width: 30px; text-align:right; border-right: 1px solid #000; float:left;'>").replace("*/", "</div>");

                                if (lineNumString.length() > 0) {
                                    int lineNumber = Integer.parseInt(lineNumString);
                                    covered = false;
                                    if (overallLinesCovered.containsKey(className) && overallLinesCovered.get(className).contains(lineNumber)) {
                                        line = "<span style='background-color: #00FF00;'>" + line + "</span>";
                                    } else {
                                        line = "<span style='background-color: #FF0000;'>" + line + "</span>";
                                    }

                                    for (int m = 0; m < seenMethods.size(); m++) {
                                        String method = seenMethods.get(m);
                                        if (methodLinesCovered.get(method).containsKey(className) && methodLinesCovered.get(method).get(className).containsKey(lineNumber)) {
                                            int timesCovered = methodLinesCovered.get(method).get(className).get(lineNumber);
                                            float percentCovered = Math.round(1000*timesCovered / (float)methodQuantity.get(method))/10f;
                                            line += "<span style='float: right; color: #ffffff; background-color: " + colors[m % colors.length] + ";'>" + method +
                                                    " (" + percentCovered + "%)" + "</span>";
                                        }
                                    }

                                } else {
                                    line = "<div style='font-align:center; width: 30px; text-align:right; border-right: 1px solid #000; float:left;'>" + (i + 1) + "</div>" + line;
                                }
                            } else {
                                line = "<div style='font-align:center; width: 30px; text-align:right; border-right: 1px solid #000; float:left;'>" + (i + 1) + "</div>" + line;
                            }


                        } else {
                            line = "<div style='font-align:center; width: 30px; text-align:right; border-right: 1px solid #000; float:left;'>" + (i + 1) + "</div>" + line;
                        }

                        if (covered) {
                            line = "<span style='background-color: #FFFF00;'>" + line + "</span>";
                        }

                        currentLine++;
                        classOut += line;
                        classOut += "</div>";

                    }
                } else {
                    classOut += "Cannot find class file " + className;

                }

                classOut += "</div></body></html>";

                if (classout.getParentFile() != null && !classout.getParentFile().exists()) {
                    classout.getParentFile().mkdirs();
                }

                if (!classout.exists()) {
                    classout.createNewFile();
                }

                FileHandler.writeToFile(classout, classOut);

                input += "<div style='width:100%;float:left'>" +
                        "<div style='width:30%;text-align:right;float:left; padding: 5px;'><a href='" + className + ".html'>" +
                        s + "</a></div><div style='width:67%;text-align:left;float:right;'>";

                ArrayList<Float> averages = new ArrayList<Float>();
                ArrayList<String> methods = new ArrayList<String>();

                for (String m : seenMethods) {
                    if (classCoverage.get(s).containsKey(m)) {
                        float average = 0;
                        counter = 0;
                        for (Float f : classCoverage.get(s).get(m)) {
                            average += f;
                            counter++;
                        }
                        if (counter == 0) {
                            counter = 1;
                        }
                        average /= counter;
                        averages.add(average);
                        methods.add(m);
                    }
                }

                float total = 0;

                for (Float f : averages) {
                    total += f;
                }

                float modifier = 1f / total;
                final float WIDTH = 95f;

                for (int i = 0; i < methods.size(); i++) {
                    float average = averages.get(i);
                    float percent = average * modifier;
                    int width = Math.round(WIDTH * percent);
                    input += "<span style='float:left; width:calc(" + width + "% - 10px); color:#ffffff; background-color: " + colors[i % colors.length] + "; overflow:hidden; padding: 5px;'>" +
                            (Math.round(average * 1000) / 10f) + "%</span>";
                }

                input += "</div></div>";
            }
            input += "</div></body></html>";

            File output = new File(args[2] + "/results/" + outDir + "/index.html");

            if (output.getParentFile() != null && !output.getParentFile().exists()) {
                output.getParentFile().mkdirs();
            }


            if (!output.exists()) {
                output.createNewFile();
            }
            FileHandler.writeToFile(output, input);
        } catch (Throwable e) {
            e.printStackTrace();
        }


    }
}
