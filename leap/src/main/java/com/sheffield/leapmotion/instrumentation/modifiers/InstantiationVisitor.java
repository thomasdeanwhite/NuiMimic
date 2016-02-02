package com.sheffield.leapmotion.instrumentation.modifiers;

import com.leapmotion.leap.*;
import com.sheffield.instrumenter.Properties;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.mocks.SeededGesture;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

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
    private static Method METHOD_TO_CALL;
    private static Method APP_METHOD_TO_CALL;
    private static Method CIRCLE_METHOD_TO_CALL;

    private final static boolean DEBUG_MODE = false;

    static {
        try {
            METHOD_TO_CALL = SeededController.class.getMethod("getController", new Class[]{});
            APP_METHOD_TO_CALL = App.class.getMethod("setTesting", new Class[]{});
            CIRCLE_METHOD_TO_CALL = SeededGesture.class.getMethod("getCircleGesture", new Class[]{Gesture.class});
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
        if (DEBUG_MODE){
            methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            methodVisitor.visitLdcInsn(className + "(" + owner + "::" + name + ")");
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", itf);
        }
        boolean shouldCall = true;
        if (owner.equals(CONTROLLER_CLASS) && opcode == Opcodes.INVOKESPECIAL && name.equals("<init>")) {
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
        } else if (Properties.REPLACE_FINGERS_METHOD && owner.equals(HAND_CLASS) && name.equals("fingers") ){
            super.visitMethodInsn(opcode, owner, name, desc, false);
            owner = FINGER_LIST_CLASS;
            name = "extended";
        }

        if (shouldCall) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }

}
