package com.sheffield.leapmotion.runtypes.agent;

import com.sheffield.instrumenter.analysis.ClassAnalyzer;
import com.sheffield.instrumenter.instrumentation.InstrumentingClassLoader;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.instrumentation.visitors.TestingClassAdapter;
import com.sheffield.util.ClassNameUtils;
import org.objectweb.asm.ClassVisitor;

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

        instrumentingClassLoader.setBuildDependencyTree(true);

        instrumentingClassLoader.getClassReplacementTransformer().setWriteClasses(true);

        instrumentingClassLoader.setShouldInstrument(true);

        App.ENABLE_APPLICATION_OUTPUT = true;
        App.IS_INSTRUMENTING = true;
        Properties.LOG = false;
        ClassAnalyzer.setOut(App.out);

        instrumentingClassLoader.addClassInstrumentingInterceptor(new InstrumentingClassLoader.ClassInstrumentingInterceptor() {
            @Override
            public ClassVisitor intercept(ClassVisitor parent, String name) {
                return new TestingClassAdapter(parent, name);
            }
        });

        Properties.BYTECODE_DIR = "./testing_output/instrumented_classes";


    }

    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer)
            throws IllegalClassFormatException {


        String fileName = "./testing_output/instrumented_classes/" +
                ClassNameUtils.replaceDots(className);

        try {
            if (instrumentingClassLoader.shouldInstrumentClass(className)) {
                return instrumentingClassLoader.modifyBytes(ClassNameUtils
                        .replaceSlashes(className), classfileBuffer);
            }
            
        } catch (ClassNotFoundException e) {
            e.printStackTrace(App.out);
        } catch (IOException e) {
            e.printStackTrace(App.out);
        }

        return classfileBuffer;
    }
}
