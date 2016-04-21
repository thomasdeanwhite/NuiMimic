package com.sheffield.leapmotion;

/**
 * Created by thomas on 18/04/2016.
 */
public class ClassTracker {
    String className = "";
    int lines = 0;
    int branches = 0;

    public ClassTracker (String cl, int lines, int branches){
        className = cl;
        this.lines = lines;
        this.branches = branches;
    }
}