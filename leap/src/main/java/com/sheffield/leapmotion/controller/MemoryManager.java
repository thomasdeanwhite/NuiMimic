package com.sheffield.leapmotion.controller;

/**
 * Created by thomas on 19/05/17.
 */
public class MemoryManager {

    private static String[] mems = {"b", "Kb", "Mb", "Gb", "Tb", "Pb"};
    private static String presentMemory(long l){

        long m = l;
        int counter = 0;
        while (m >= 1024 && counter < mems.length){
            m = m / 1024;
            counter++;
        }


        float bytes = l / (float)(Math.pow(1024, counter));

        return (Math.round(bytes * 100.0))/100.0 + mems[counter];

    }

    public static String getMemoryUsage(){
        Runtime rt = Runtime.getRuntime();
        return presentMemory(rt.totalMemory()) + "/" +
                presentMemory(rt.maxMemory()) + " [" +
                presentMemory(rt.freeMemory()) + "]";
    }
}
