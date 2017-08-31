package com.sheffield.leapmotion.runtypes.agent;

import com.leapmotion.leap.Controller;
import com.scythe.instrumenter.InstrumentationProperties;
import com.scythe.instrumenter.analysis.ClassAnalyzer;
import com.scythe.instrumenter.instrumentation.InstrumentingClassLoader;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.instrumentation.visitors.TestingClassAdapter;
import com.scythe.util.ClassNameUtils;
import org.objectweb.asm.ClassVisitor;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;

/**
 * Created by thomas on 18/11/2016.
 */
public class LeapmotionAgentTransformer implements ClassFileTransformer{

    private InstrumentingClassLoader instrumentingClassLoader;

    public LeapmotionAgentTransformer (){
        instrumentingClassLoader = InstrumentingClassLoader.getInstance();

        //instrumentingClassLoader.setBuildDependencyTree(true);

        instrumentingClassLoader.getClassReplacementTransformer().setWriteClasses(true);

        instrumentingClassLoader.setShouldInstrument(true);

        if (Properties.CONTROLLER_SUPER_CLASS) {
            instrumentingClassLoader.addSuperClassReplacement
                    (Controller.class.getCanonicalName(),
                            SeededController.class.getCanonicalName
                                    ());
        }

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

        InstrumentationProperties.BYTECODE_DIR = Properties
                .TESTING_OUTPUT + "/instrumented_classes/";

        InstrumentationProperties.WRITE_CLASS = true;


    }

    private ArrayList<String> instrumentedClasses = new ArrayList<>();

    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer)
            throws IllegalClassFormatException {

        InstrumentationProperties.BYTECODE_DIR = Properties
                .TESTING_OUTPUT + "/instrumented_classes/";


        String fileName = InstrumentationProperties.BYTECODE_DIR +
                ClassNameUtils.replaceDots(className) + ".class";

//        File classFile = new File(fileName);
//
//        if (classFile.exists()){
//            try {
//                FileInputStream fis = new FileInputStream(classFile);
//
//                byte[] oldClass = new byte[fis.available()];
//
//                fis.read(oldClass);
//
//                return oldClass;
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        try {
            if (instrumentingClassLoader.shouldInstrumentClass(className)) {
                byte[] cla = instrumentingClassLoader.modifyBytes(ClassNameUtils
                        .replaceSlashes(className), classfileBuffer);

                instrumentedClasses.add(className);

                return cla;
            }
            
        } catch (ClassNotFoundException e) {
            e.printStackTrace(App.out);
        } catch (IOException e) {
            e.printStackTrace(App.out);
        } catch (Throwable t){
            t.printStackTrace(App.out);
        }

        return classfileBuffer;
    }

    public static ArrayList<String> registeredClasses(){
        return registeredClasses();
    }
}
