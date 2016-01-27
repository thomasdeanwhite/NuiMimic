package com.sheffield.leapmotion.instrumentation.visitors;

import com.sheffield.leapmotion.instrumentation.modifiers.InstantiationVisitor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class TestingClassAdapter extends ClassAdapter implements ClassVisitor {

	private String className;
	private ClassVisitor parent;

	public TestingClassAdapter(ClassVisitor parent, String name) {
		super(parent);
		className = name;
		this.parent = parent;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
			String[] exceptions) {
		// MethodNode mn = new MethodNode(access, name, descriptor, signature,
		// exceptions);
		return new InstantiationVisitor(parent.visitMethod(access, name, descriptor, signature, exceptions), className);
	}

}
