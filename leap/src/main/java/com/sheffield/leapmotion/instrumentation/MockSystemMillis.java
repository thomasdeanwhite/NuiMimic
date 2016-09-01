package com.sheffield.leapmotion.instrumentation;

/**
 * Created by thomas on 9/1/2016.
 */
public class MockSystemMillis {

    public static long RUNTIME = 0;

    public static long currentTimeMillis(){
        return RUNTIME;
    }
}
