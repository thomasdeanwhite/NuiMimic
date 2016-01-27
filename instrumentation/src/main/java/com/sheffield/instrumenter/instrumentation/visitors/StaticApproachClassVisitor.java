package com.sheffield.instrumenter.instrumentation.visitors;

import com.sheffield.instrumenter.instrumentation.modifiers.StaticBranchVisitor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class StaticApproachClassVisitor extends ClassAdapter {
	private String className;;
	private ClassVisitor classVisitor;

	public StaticApproachClassVisitor(ClassVisitor mv, String className) {
		super(mv);
		this.className = className;
		this.classVisitor = classVisitor;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		return new StaticBranchVisitor(super.visitMethod(access, name, desc, signature, exceptions),
				className, name);
	}

}
