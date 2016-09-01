package com.sheffield.leapmotion.instrumentation.modifiers;

import com.leapmotion.leap.CircleGesture;
import com.leapmotion.leap.Controller;
import com.leapmotion.leap.FingerList;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.HandList;
import com.sheffield.instrumenter.analysis.DependencyTree;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.instrumentation.MockGraphicsDevice;
import com.sheffield.leapmotion.instrumentation.MockJOptionPane;
import com.sheffield.leapmotion.instrumentation.MockRandom;
import com.sheffield.leapmotion.instrumentation.MockSystemMillis;
import com.sheffield.leapmotion.mocks.SeededGesture;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.Random;

public class InstantiationVisitor extends MethodVisitor {

    private String className;
    private MethodVisitor methodVisitor;
    private static final String CONTROLLER_CLASS = Type.getInternalName(Controller.class);
    private static final String APP_CLASS = Type.getInternalName(App.class);
    private static final String NEW_CONTROLLER = Type.getInternalName(SeededController.class);
    private static final String HAND_LIST_CLASS = Type.getInternalName(HandList.class);
    private static final String FINGER_LIST_CLASS = Type.getInternalName(FingerList.class);
    private static final String CIRCLE_GESTURE_CLASS = Type.getInternalName(CircleGesture.class);
    private static final String GESTURE_CLASS = Type.getInternalName(SeededGesture.class);
    private static final String HAND_CLASS = Type.getInternalName(Hand.class);
    private static final String JOPTIONS_CLASS = Type.getInternalName(JOptionPane.class);
    private static final String MOCK_JOPTIONS_CLASS = Type.getInternalName(MockJOptionPane.class);

    private static final String RANDOM_CLASS = Type.getInternalName(Random.class);
    private static final String MATH_CLASS = Type.getInternalName(Math.class);
    private static final String MOCK_RANDOM_CLASS = Type.getInternalName(MockRandom.class);

    private static final String SYSTEM_CLASS = Type.getInternalName(System.class);
    private static final String MOCK_SYSTEM_MILLIES = Type.getInternalName(MockSystemMillis.class);

    private static final String GD_CLASS = Type.getInternalName(GraphicsDevice.class);
    private static final String GE_CLASS = Type.getInternalName(GraphicsEnvironment.class);
    private static final String MOCK_GD_CLASS = Type.getInternalName(MockGraphicsDevice.class);

    private static Method METHOD_TO_CALL;
    private static Method APP_METHOD_TO_CALL;
    private static Method CIRCLE_METHOD_TO_CALL;
    private static Method RANDOM_METHOD_TO_CALL;
    private static Method RANDOM_CONSTRUCTOR_METHOD_TO_CALL;


    private static Method JOPTIONS_METHOD_TO_CALL;
    private static Method FRAME_METHOD_TO_CALL;

    private final static boolean DEBUG_MODE = false;

    static {
        try {
            METHOD_TO_CALL = SeededController.class.getMethod("getController", new Class[]{});
            APP_METHOD_TO_CALL = App.class.getMethod("setTesting", new Class[]{});
            CIRCLE_METHOD_TO_CALL = SeededGesture.class.getMethod("getCircleGesture", new Class[]{Gesture.class});
            RANDOM_METHOD_TO_CALL = MockRandom.class.getMethod("random", new Class[]{String.class});
            RANDOM_CONSTRUCTOR_METHOD_TO_CALL = MockRandom.class.getMethod("getRandom", new Class[]{String.class});

            //JOPTIONS_METHOD_TO_CALL = JOptionPane.class.getMethod("getRootFrame", new Class[]{});
            //FRAME_METHOD_TO_CALL = java.awt.Frame.class.getMethod("dispose", new Class[]{});
        } catch (NoSuchMethodException e) {
            e.printStackTrace(App.out);
        } catch (SecurityException e) {
            e.printStackTrace(App.out);
        }
    }

    public InstantiationVisitor(MethodVisitor mv, String cName) {
        super(Opcodes.ASM5, mv);
        methodVisitor = mv;
        className = cName;

    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        boolean shouldCall = true;
        if (!Properties.LEAVE_LEAPMOTION_ALONE) {
            if (owner.equals(CONTROLLER_CLASS)) {
                DependencyTree.getDependencyTree().addDependency("com.leapmotion.leap.Controller", className);
                if (opcode == Opcodes.INVOKESPECIAL && name.equals("<init>")) {
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                    try {
                        super.visitInsn(Opcodes.POP);
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, NEW_CONTROLLER, "getController",
                                Type.getMethodDescriptor(METHOD_TO_CALL), itf);
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, APP_CLASS, "setTesting",
                                Type.getMethodDescriptor(APP_METHOD_TO_CALL), itf);

                        //App.out.println("Replaced Controller instantiation in " + className + " with method call to " + METHOD_TO_CALL.toGenericString());

                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                    shouldCall = false;
                }
            } else if (owner.equals(HAND_LIST_CLASS) && name.equals("empty")) {
                // Convert from old API version to new one.
                super.visitMethodInsn(opcode, owner, "isEmpty", desc, false);
                shouldCall = false;
            } else if (owner.equals(CIRCLE_GESTURE_CLASS) && name.contains("<init>")) {
                super.visitInsn(Opcodes.DUP_X2);
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                super.visitInsn(Opcodes.POP);
                super.visitMethodInsn(Opcodes.INVOKESTATIC, GESTURE_CLASS, "getCircleGesture",
                        Type.getMethodDescriptor(CIRCLE_METHOD_TO_CALL), false);
                shouldCall = false;
            } else if (Properties.REPLACE_FINGERS_METHOD && owner.equals(HAND_CLASS) && name.equals("fingers")) {
                super.visitMethodInsn(opcode, owner, name, desc, false);
                owner = FINGER_LIST_CLASS;
                name = "extended";
            }
        } if (owner.equalsIgnoreCase(JOPTIONS_CLASS)){
            shouldCall = false;
            super.visitMethodInsn(opcode, MOCK_JOPTIONS_CLASS, name, desc, itf);
        } else if (owner.equalsIgnoreCase(GD_CLASS)){
            //shouldCall = false;
            //super.visitMethodInsn(opcode, MOCK_GD_CLASS, name, desc, itf);
        } else if (owner.equalsIgnoreCase(RANDOM_CLASS) && opcode == Opcodes.INVOKESPECIAL && name.equals("<init>")){
            shouldCall = false;
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            super.visitInsn(Opcodes.POP);
            methodVisitor.visitLdcInsn(className);
            App.out.println(className + " is calling Random!");
            super.visitMethodInsn(Opcodes.INVOKESTATIC, MOCK_RANDOM_CLASS, name, Type.getMethodDescriptor(RANDOM_CONSTRUCTOR_METHOD_TO_CALL), itf);
        } else if (owner.equalsIgnoreCase(MATH_CLASS) && name.equals("random")){
            shouldCall = false;
            methodVisitor.visitLdcInsn(className);
            App.out.println(className + " is calling Random!");
            super.visitMethodInsn(opcode, MOCK_RANDOM_CLASS, name, Type.getMethodDescriptor(RANDOM_METHOD_TO_CALL), itf);
        } else if (owner.equalsIgnoreCase(SYSTEM_CLASS) && name.equals("currentTimeMillis")){
            shouldCall = false;
            App.out.println(className + " is calling currentTimeMillis!");
            super.visitMethodInsn(opcode, MOCK_SYSTEM_MILLIES, name, desc, itf);
        }

        if (shouldCall) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }

        if (DEBUG_MODE){
            methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            methodVisitor.visitLdcInsn(className + "(" + owner + "::" + name + ")");
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", itf);
        }
    }

}
