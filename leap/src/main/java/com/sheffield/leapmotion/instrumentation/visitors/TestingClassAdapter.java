package com.sheffield.leapmotion.instrumentation.visitors;

import com.sheffield.leapmotion.instrumentation.modifiers.InstantiationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TestingClassAdapter extends ClassVisitor {

	private String className;
	private ClassVisitor parent;

	public TestingClassAdapter(ClassVisitor parent, String name) {
		super(Opcodes.ASM5, parent);
		className = name;
		this.parent = parent;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
			String[] exceptions) {
		return new InstantiationVisitor(parent.visitMethod(access, name, descriptor, signature, exceptions), className, name);
	}

}
