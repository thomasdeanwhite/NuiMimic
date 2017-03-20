package com.sheffield.leapmotion.instrumentation;

import com.sheffield.instrumenter.instrumentation.InstrumentingClassLoader;

import java.net.URL;

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
