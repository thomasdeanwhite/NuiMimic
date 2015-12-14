package com.sheffield.leapmotion.tester.instrumentation.modifiers;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.HandList;
import com.sheffield.leapmotion.tester.App;
import com.sheffield.leapmotion.tester.controller.SeededController;
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
	private static final String HAND_CLASS = Type.getInternalName(HandList.class);
	private static Method METHOD_TO_CALL;
	private static Method APP_METHOD_TO_CALL;

	static {
		try {
			METHOD_TO_CALL = SeededController.class.getMethod("getController", new Class[] {});
			APP_METHOD_TO_CALL = App.class.getMethod("setTesting", new Class[] {});
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				super.visitMethodInsn(Opcodes.INVOKESTATIC, APP_CLASS, "setTesting", Type.getMethodDescriptor(APP_METHOD_TO_CALL));
				App.out.println("Replaced Controller instantiation in " + className + " with method call to "
						+ METHOD_TO_CALL.toGenericString());
			} catch (SecurityException e) {
				e.printStackTrace();
			}
			shouldCall = false;
		} else if (owner.equals(HAND_CLASS) && name.equals("empty")){
			//Convert from old API version to new one.
			super.visitMethodInsn(opcode, owner, "isEmpty", desc);
			shouldCall = false;
		}

		if (shouldCall) {
			super.visitMethodInsn(opcode, owner, name, desc);
		}
	}

}
