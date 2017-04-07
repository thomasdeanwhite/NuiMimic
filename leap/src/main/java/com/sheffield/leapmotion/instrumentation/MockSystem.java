package com.sheffield.leapmotion.instrumentation;

/**
 * Created by thomas on 9/1/2016.
 */
public class MockSystem {

    public static long MILLIS = 0;
    public static long NANOS = 0;

    public static long currentTimeMillis(){
        return MILLIS;
    }

    public static long nanoTime(){
        return NANOS;
    }

    public static String setProperty (String p, String v){
        if (p.equals("org.lwjgl.opengl.Window.undecorated")) {
            v = "false";
        }

        return System.setProperty(p, v);
    }

    public static String getProperty(String s){
        if (s.equals("os.name")){
            return "mac";
        }
        return System.getProperty(s);
    }

    public static String getProperty(String s, String d){
        if (s.equals("os.name")){
            return "Windows 8.1";
        }
        return System.getProperty(s, d);
    }
}
