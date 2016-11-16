package com.sheffield.leapmotion.instrumentation;

/**
 * Created by thomas on 9/1/2016.
 */
public class MockSystem {

    public static long RUNTIME = 0;

    public static long currentTimeMillis(){
        return RUNTIME;
    }

    public static String setProperty (String p, String v){
        if (p.equals("org.lwjgl.opengl.Window.undecorated")) {
            v = "false";
        }
        return System.setProperty(p, v);
    }
}
