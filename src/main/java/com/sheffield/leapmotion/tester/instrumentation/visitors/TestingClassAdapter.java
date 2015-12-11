package com.sheffield.leapmotion.tester.instrumentation.visitors;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import com.sheffield.leapmotion.tester.instrumentation.modifiers.BranchVisitor;
import com.sheffield.leapmotion.tester.instrumentation.modifiers.InstantiationVisitor;

public class TestingClassAdapter extends ClassAdapter implements ClassVisitor {

	private final String className;

	public TestingClassAdapter(ClassVisitor arg0, String clazz) {
		super(arg0);
		className = clazz;
	}

	@Override
	public void visit(int arg0, int arg1, String arg2, String arg3, String superName, String[] arg5) {
		super.visit(arg0, arg1, arg2, arg3, superName, arg5);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
			String[] exceptions) {
		// MethodNode mn = new MethodNode(access, name, descriptor, signature,
		// exceptions);
		return new BranchVisitor(
				new InstantiationVisitor(super.visitMethod(access, name, descriptor, signature, exceptions), className),
				className, name + descriptor);
	}

}
