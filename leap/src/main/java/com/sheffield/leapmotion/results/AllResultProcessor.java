package com.sheffield.leapmotion.results;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sheffield.instrumenter.analysis.ClassAnalyzer;
import com.sheffield.instrumenter.analysis.DependencyTree;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.Line;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by thomas on 19/04/2016.
 */
public class AllResultProcessor {

    private static int relatedLines = 0;
    private static int relatedBranches = 0;

    private static int LINES_COVERED = 0;

    private static int LINES_UNCOVERED = 0;

    private static String[] colors = {"#880000", "#008888", "#888800", "#008800", "#000088", "#888888", "#000000"};

    private static ArrayList<ClassTracker> relatedClassMethods = new ArrayList<ClassTracker>();

    private static ArrayList<String> relatedClasses = new ArrayList<String>();

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
                            relatedClasses.add(DependencyTree.getClassName(clInfo[0]));
                        }
                    }
                    relatedClassMethods = clas;

                    App.out.println("- Found related classes file at: " + linesFile.getAbsolutePath());
                    App.out.println("[" + relatedLines + " related lines, " + relatedBranches + " related branches, " + relatedClassMethods.size() + " related classes]");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            File dir = new File(args[2]);

            HashMap<String, ArrayList<Integer>> coverableLines = new HashMap<String, ArrayList<Integer>>();

            HashMap<String, ArrayList<Integer>> overallLinesCovered = new HashMap<String, ArrayList<Integer>>();

            HashMap<String, HashMap<String, HashMap<Integer, Integer>>> methodLinesCovered = new HashMap<String, HashMap<String, HashMap<Integer, Integer>>>();

            String[] methodsUsed = {"VQ", "RANDOM", "SINGLE_MODEL", "STATE_DEPENDENT", "STATE_ISOLATED", "RECONSTRUCTION", "EMPTY", "USER_PLAYBACK", "UNKNOWN"};

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

                for (int i = 0; i < header.length; i++) {
                    if (header[i].contains("relatedLineCoverage")) {
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

                for (int i = resultsLines.length - 1; i > 0; i--) {
                    if (resultsLines[i].trim().length() == 0) {
                        continue;
                    } else {
                        String[] currentRow = resultsLines[i].split(",");
                        float coverage = Float.parseFloat(currentRow[relatedLineIndex]);
                        if (!methodAverageCoverage.containsKey(method)) {
                            methodAverageCoverage.put(method, coverage);
                        } else {
                            methodAverageCoverage.put(method, methodAverageCoverage.get(method) + coverage);
                        }
                        break;
                    }
                }

                if (!methodQuantity.containsKey(method)) {
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

                            i[0] = DependencyTree.getClassName(i[0]);

                            if (i[1].length() == 0 || !relatedClasses.contains(i[0])){
                                continue;
                            }

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
                        String clazz = DependencyTree.getClassName(cMap.get(Integer.parseInt(i[0])));

                        if (lines.containsKey(clazz)) {
                            lines.get(clazz).add(Integer.parseInt(i[1]));
                        }
                    }
                }


                Iterator s = lines.keySet().iterator();

                ArrayList<String> classes = new ArrayList<String>();

                while (s.hasNext()) classes.add((String) s.next());

                Collections.sort(classes);
                HashMap<String, Integer> linesForAnonClasses = new HashMap<String, Integer>();
                HashMap<String, Integer> linesCoveredForAnonClasses = new HashMap<String, Integer>();


                ArrayList<String> subclasses = new ArrayList<String>();


                for (String c : classes) {
                    String cName = c;
//                    if (cName.length() > 50) {
//                        cName = cName.substring(c.length() - 50, c.length());
//                    }
                    if (cName.startsWith("/")) {
                        cName = cName.substring(1);
                    }

                    String originalClassName = c;

                    if (c.contains("$")){
                        c = c.substring(0, c.indexOf("$"));
                    }

                    if (cName.contains("$")){
                        cName = cName.substring(0, cName.indexOf("$"));
                    }

                    if (!cName.contains("/")){
                        cName = "default/" + cName;
                    }

                    shortenedClasses.put(cName, c);
                    ArrayList<Integer> l = lines.get(originalClassName);
                    int percent = 0;

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




                    if (!linesForAnonClasses.containsKey(c)){
                        linesForAnonClasses.put(c, 0);
                    }

                    List<Line> coverableLinesForClass = ClassAnalyzer.getCoverableLines(originalClassName);


                    if (!coverableLines.containsKey(c)){
                        coverableLines.put(c, new ArrayList<Integer>());
                    }

                    for (Line line : coverableLinesForClass){
                        if (!coverableLines.get(c).contains(line.getLineNumber())) {
                            coverableLines.get(c).add(line.getLineNumber());
                        }
                    }

                    int totalLines = linesForAnonClasses.get(c) + coverableLinesForClass.size();

                    linesForAnonClasses.put(c, totalLines);

                    if (!linesCoveredForAnonClasses.containsKey(c)){
                        linesCoveredForAnonClasses.put(c, 0);
                    }

                    int totalCovered = linesCoveredForAnonClasses.get(c) + percent;

                    linesCoveredForAnonClasses.put(c, totalCovered);

                    if (!classCoverage.containsKey(cName)) {
                        classCoverage.put(cName, new HashMap<String, ArrayList<Float>>());
                    }

                    if (!classCoverage.get(cName).containsKey(method)) {
                        classCoverage.get(cName).put(method, new ArrayList<Float>());
                    }

                    float classCov = linesCoveredForAnonClasses.get(c) / (float)linesForAnonClasses.get(c);

                    assert(classCov <= 1);

                    if (subclasses.contains(c)){
                        classCoverage.get(cName).get(method).remove(classCoverage.get(cName).get(method).size()-1);
                    } else {
                        subclasses.add(c);
                    }


                    classCoverage.get(cName).get(method).add(classCov);

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



            for (String s : seenMethods) {
                float quant = (float) methodQuantity.get(s);
                float coverage = methodAverageCoverage.get(s);
                methodAverageCoverage.put(s, coverage / quant);
            }

            for (Float f : methodAverageCoverage.values()) {
                avg += f;
            }

            float mult = 1f / avg;


            for (String m : seenMethods) {
                input += "<span style='float:left; width:calc(" + Math.round(headerWidth * methodAverageCoverage.get(m) * mult) + "% - 10px); font-weight: bold; color:#ffffff; background-color: " + colors[counter++ % colors.length] + "; overflow:hidden;padding:5px;'>" +
                        m + " (" + (Math.round(methodAverageCoverage.get(m) * 1000f) / 10f) + "%)</span>";
            }

            input += "</div></div>";

            String outDir = System.currentTimeMillis() + "";
            Set<String> classCov = classCoverage.keySet();

            HashMap<String, ArrayList<String>> packages = new HashMap<String, ArrayList<String>>();

            for (String s : classCov) {
                int lastSlash = s.lastIndexOf("/");
                String pack = "default";
                if (lastSlash >= 0) {
                    pack = s.substring(0, lastSlash);
                }
                String classN = s.substring(lastSlash + 1);
                if (!packages.containsKey(pack)) {
                    packages.put(pack, new ArrayList<String>());
                }

                packages.get(pack).add(classN);
            }

            ArrayList<String> packs = new ArrayList<String>();

            for (String pack : packages.keySet()) {
                packs.add(pack);
            }

            Collections.sort(packs);

            for (String pack : packs) {
                input += "<div style='width:30%;text-align:right;float:left; padding: 5px;'><h3>" + pack + "</h3></div>";
                Collections.sort(packages.get(pack));

                String packInput = "";

                float packCoverage = 0;

                int packTotal = 0;

                for (String cn : packages.get(pack)) {
                    String s = pack + "/" + cn;
                    String className = shortenedClasses.get(s);

                    System.out.print("\rProcessing " + className);

                    String classDir = args[2] + "/results/classes";

                    if (args.length > 3) {
                        classDir = args[3];
                    }

                    File classIn = new File(classDir + "/" + className + ".java");
                    File classout = new File(args[2] + "/results/" + outDir + "/" + className + ".html");

                    int slashes = className.split("/").length;

                    String backLink = "index.html";

                    for (; slashes > 1; slashes--) {
                        backLink = "../" + backLink;
                    }

                    String classOut = "";

                    int linesCovered = 0;
                    int linesUncovered = 0;

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
                                String newline = "<div style='background-color: #FFFF00; font-align:center; width: 30px; text-align:right; border-right: 1px solid #000; float:left;'>" +
                                        startImports + "-" + (i - 1) + "</div><span style='margin-left: 5px'>&nbsp;</span>import *;";
                                startImports = 0;
                                classOut += newline;
                                classOut += "</div>";
                            }
                            classOut += "<div style='clear: both'>";


                            boolean assumedCovered = false;
                            if (line.trim().length() > 0) {

                                if (!line.trim().startsWith("/*")){
                                    line = "/* " + (i + 1) + " */" + line;
                                }

                                int lineIndex = line.indexOf("/*");

                                int endIndex = line.indexOf("*/");
                                boolean lineNumberFound = true;

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

                                    //collapse white space?
//                                    if (whiteSpace >= line.length()) {
//                                        classOut += "</div>";
//                                        continue;
//                                    }

                                    line = line.substring(0, endIndex) + line.substring(endIndex, whiteSpace).replace(" ", "<span style='margin-left: 5px'>&nbsp;</span>") +
                                            line.substring(whiteSpace, line.length());
                                    line = line.substring(0, endIndex) + line.substring(endIndex, whiteSpace).replace("\t", "<span style'margin-left: 10px'>&nbsp;</span>") +
                                            line.substring(whiteSpace, line.length());


                                    int lineNumber = 0;


                                    if (lineNumString.length() > 0) {
                                        try {
                                            lineNumber = Integer.parseInt(lineNumString);
                                        } catch (NumberFormatException e) {
                                        }
                                    }

                                    if (lineNumString.length() > 0) {
                                        lineNumberFound = false;
                                        lineNumber = (i + 1);

                                        //line = line.replace("/*", "/* " + lineNumber);
                                    }

                                    line = line.replace("/*", "<div style='background-color: inherit; font-align:center; width: 30px; text-align:right; border-right: 1px solid #000; float:left;'>").replace("*/", "</div>");


                                    if (overallLinesCovered.containsKey(className) && overallLinesCovered.get(className).contains(lineNumber)) {
                                        line = "<span style='background-color: #00FF00;'>" + line + "</span>";
                                        LINES_COVERED++;
                                        linesCovered++;
                                    } else if (coverableLines.containsKey(className) && coverableLines.get(className).contains(lineNumber)) {

                                        line = "<span style='background-color: #FF0000;'>" + line + "</span>";
                                        LINES_UNCOVERED++;
                                        linesUncovered++;
                                    } else {
                                        assumedCovered = true;
                                    }

                                    if (!assumedCovered) {
                                        for (int m = 0; m < seenMethods.size(); m++) {
                                            String method = seenMethods.get(m);
                                            if (methodLinesCovered.get(method).containsKey(className) && methodLinesCovered.get(method).get(className).containsKey(lineNumber)) {
                                                int timesCovered = methodLinesCovered.get(method).get(className).get(lineNumber);
                                                float percentCovered = Math.round(1000 * timesCovered / (float) methodQuantity.get(method)) / 10f;
                                                line += "<span style='float: right; color: #ffffff; background-color: " + colors[m % colors.length] + ";'>" + method +
                                                        " (" + percentCovered + "%)" + "</span>";
                                            }
                                        }
                                    }
                                } else {
                                    line = "<div style='font-align:center; width: 30px; text-align:right; border-right: 1px solid #000; float:left;'>" + (i + 1) + "</div>" + line;
                                }


                            } else {
                                line = "<div style='font-align:center; width: 30px; text-align:right; border-right: 1px solid #000; float:left;'>" + (i + 1) + "</div>" + line;
                            }

                            if (assumedCovered) {
                                line = "<span style='background-color: #FFFF00;'>" + line + "</span>";
                            }

                            currentLine++;
                            classOut += line;
                            classOut += "</div>";

                        }
                    } else {
                        classOut += "Cannot find class file " + className;

                    }

                    if (classout.getParentFile() != null && !classout.getParentFile().exists()) {
                        classout.getParentFile().mkdirs();
                    }

                    if (!classout.exists()) {
                        classout.createNewFile();
                    }

                    if (linesCovered + linesUncovered == 0){
                        linesUncovered = 1;
                    }

                    classOut = "<html><head><style>" +
                            "div { margin: 0px 3px; padding: 0px 3px; font-size:10px; } " +
                            "body { font-family: Arial; }" +
                            "</style></head><body>" +
                            "<h1 style='width:100%; font-weight: bold; text-align: center;'>" + className + "</h3>" +
                            "<a href='" + backLink + "'><h1 style='width:100%; font-weight: bold; text-align: center;'>&lt; Back</h3></a>"
                            + "<h1 style='width:100%; font-weight: bold; text-align: center;'>Lines Covered: " + linesCovered + ":" + linesUncovered + " (" + (Math.round(1000f * (linesCovered / (float)(linesCovered + linesUncovered)))/10f) +"%)</h1>"
                            + "<div style='margin-left: auto; margin-right: auto; border: 1px solid #000; border-radius: 10px; padding: 30px;'>"
                            + classOut;

                    classOut += "</div></body></html>";

                    FileHandler.writeToFile(classout, classOut);

                    packInput += "<div style='width:100%;float:left'>" +
                            "<div style='width:30%;text-align:right;float:left; padding: 5px;'><a href='" + className + ".html'>" +
                            cn + "</a></div><div style='width:67%;text-align:left;float:right;'>";

                    ArrayList<Float> averages = new ArrayList<Float>();
                    ArrayList<String> methods = new ArrayList<String>();

                    for (String m : seenMethods) {
                        if (classCoverage.get(s).containsKey(m)) {
                            float average = 0;
                            counter = 0;
                            for (Float f : classCoverage.get(s).get(m)) {
                                assert(f <= 1);
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
                    final float WIDTH = 95f / methods.size();

                    float maxCov = 0;

                    for (int i = 0; i < methods.size(); i++) {
                        float average = averages.get(i);
                        float percent = average * modifier;
                        int width = Math.round(WIDTH *  average);

                        width = Math.max(3, width);

                        if (average > maxCov){
                            maxCov = average;
                        }

                        packInput += "<span style='float:left; width:calc(" + width + "% - 10px); color:#ffffff; background-color: " + colors[i % colors.length] + "; overflow:hidden; padding: 5px;'>" +
                                (Math.round(average * 1000) / 10f) + "%</span>";
                        if (width < WIDTH)
                                packInput += "<span style='float:left; width:calc(" + (WIDTH - width) + "% - 10px); background-color: #000000; overflow:hidden; padding: 5px;'>&nbsp;</span>";
                    }

                    packCoverage += maxCov;
                    packTotal++;

                    packInput += "</div></div>";
                }

                float cov = (Math.round(1000 * packCoverage / packTotal) / 10f) ;

                input += "<div style='width:67%;text-align:left;float:right;'><h3><span style='float:left; width:calc(" + (cov) + "% - 10px); color:#ffffff; background-color: #008800; overflow:hidden; padding: 5px;'>" +
                        cov + "%</span>" +
                        "<span style='float:left; width:calc(" + (100 - cov) + "% - 10px); color:#ffffff; background-color: #880000; overflow:hidden; padding: 5px;'>" +
                        "&nbsp;</span></h3></div>";

                input += packInput;
            }

            input += "<div style='width:30%;text-align:right;float:left; padding: 5px;'><h3>Summary</h3></div>";


            float coverage = LINES_COVERED / (float)(LINES_UNCOVERED + LINES_COVERED);

            coverage = Math.round(coverage * 1000) / 10f;

            input += "<div style='width:67%;text-align:left;float:right;'><h3><span style='float:left; width:calc(" + (Math.round(coverage)) + "% - 10px); color:#ffffff; background-color: #008800; overflow:hidden; padding: 5px;'>" +
                    coverage + "% (" + LINES_COVERED + ":" + LINES_UNCOVERED + ")</span>" +
                    "<span style='float:left; width:calc(" + Math.round(100 - coverage) + "% - 10px); color:#ffffff; background-color: #880000; overflow:hidden; padding: 5px;'>" +
                    "&nbsp;</span></h3></div>";

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
