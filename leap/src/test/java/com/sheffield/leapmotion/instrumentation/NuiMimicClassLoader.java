package com.sheffield.leapmotion.instrumentation;

/**
 * Created by thomas on 20/03/17.
 */
public class NuiMimicClassLoader extends ClassLoader {
    public static NuiMimicClassLoader instance;

    public static NuiMimicClassLoader getInstance(){
        if (instance == null){
            instance = new NuiMimicClassLoader();
        }
        return instance;
    }
}
