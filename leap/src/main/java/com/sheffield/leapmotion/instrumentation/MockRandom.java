package com.sheffield.leapmotion.instrumentation;

import com.sheffield.util.ClassNameUtils;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by thomas on 30/08/2016.
 */
public class MockRandom extends Random {

    public static HashMap<String, Random> randoms;

    static {
        randoms = new HashMap<String, Random>();
    }

    public static double random(String className){
        return getRandom(className).nextDouble();
    }

    public static Random getRandom(String className){
        className = ClassNameUtils.standardise(className);
        if (!randoms.containsKey(className)){
            long index = 0;
            for (char c : className.toCharArray()){
                index += (int) c;
            }
            randoms.put(className, new Random(index));
        }
        return randoms.get(className);
    }
}
