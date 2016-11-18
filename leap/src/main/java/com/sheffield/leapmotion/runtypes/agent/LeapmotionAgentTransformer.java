package com.sheffield.leapmotion.runtypes.agent;

import com.sheffield.instrumenter.instrumentation.InstrumentingClassLoader;
import com.sheffield.util.ClassNameUtils;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * Created by thomas on 18/11/2016.
 */
public class LeapmotionAgentTransformer implements ClassFileTransformer{

    private InstrumentingClassLoader instrumentingClassLoader;

    public LeapmotionAgentTransformer (){
        instrumentingClassLoader = InstrumentingClassLoader.getInstance();
    }

    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer)
            throws IllegalClassFormatException {

        try {
            return instrumentingClassLoader.modifyBytes(ClassNameUtils
                    .replaceSlashes(className), classfileBuffer);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classfileBuffer;
    }
}
