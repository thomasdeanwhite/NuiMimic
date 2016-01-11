package com.sheffield.leapmotion.instrumentation.modifiers;

import com.leapmotion.leap.*;
import com.sheffield.instrumenter.Properties;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.mocks.SeededGesture;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

public class InstantiationVisitor extends MethodAdapter {

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
        super(mv);
        methodVisitor = mv;
        className = cName;

    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        boolean shouldCall = true;
        if (owner.equals(CONTROLLER_CLASS) && opcode == Opcodes.INVOKESPECIAL && name.equals("<init>")) {
            super.visitMethodInsn(opcode, owner, name, desc);
            try {
                super.visitInsn(Opcodes.POP);
                super.visitMethodInsn(Opcodes.INVOKESTATIC, NEW_CONTROLLER, "getController",
                        Type.getMethodDescriptor(METHOD_TO_CALL));
                super.visitMethodInsn(Opcodes.INVOKESTATIC, APP_CLASS, "setTesting",
                        Type.getMethodDescriptor(APP_METHOD_TO_CALL));
                /*
				 * App.out.println("Replaced Controller instantiation in " + className + " with method call to " + METHOD_TO_CALL.toGenericString());
				 */
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            shouldCall = false;
        } else if (owner.equals(HAND_LIST_CLASS) && name.equals("empty")) {
            // Convert from old API version to new one.
            super.visitMethodInsn(opcode, owner, "isEmpty", desc);
            shouldCall = false;
        } else if (owner.equals(CIRCLE_GESTURE_CLASS) && name.contains("<init>")) {
            super.visitInsn(Opcodes.DUP_X2);
            super.visitMethodInsn(opcode, owner, name, desc);
            super.visitInsn(Opcodes.POP);
            super.visitMethodInsn(Opcodes.INVOKESTATIC, GESTURE_CLASS, "getCircleGesture",
                    Type.getMethodDescriptor(CIRCLE_METHOD_TO_CALL));
            shouldCall = false;
        } else if (Properties.REPLACE_FINGERS_METHOD && owner.equals(HAND_CLASS) && name.equals("fingers") ){
            super.visitMethodInsn(opcode, owner, name, desc);
            owner = FINGER_LIST_CLASS;
            name = "extended";
        }

        if (shouldCall) {
            super.visitMethodInsn(opcode, owner, name, desc);
        }
    }

}
