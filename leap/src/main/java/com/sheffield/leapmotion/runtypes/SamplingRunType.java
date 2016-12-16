package com.sheffield.leapmotion.runtypes;

import com.sheffield.instrumenter.analysis.ClassAnalyzer;
import com.sheffield.instrumenter.analysis.ClassNode;
import com.sheffield.instrumenter.analysis.DependencyTree;
import com.sheffield.instrumenter.instrumentation.ClassReplacementTransformer;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.sampler.SamplerApp;
import com.sheffield.leapmotion.util.FileHandler;
import com.sheffield.leapmotion.util.LeapMotionApplicationHandler;
import com.sheffield.util.ClassNameUtils;

import java.io.File;
import java.util.*;

/**
 * Created by thomas on 18/11/2016.
 */
public class SamplingRunType implements RunType {
    @Override
    public int run() {
        App.out.println("- Sampling data");
        SamplerApp.main(new String[]{});
        return 0;
    }
}
