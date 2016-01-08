package com.sheffield.instrumenter.instrumentation.visitors;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import com.sheffield.instrumenter.instrumentation.modifiers.StaticBranchVisitor;

public class StaticApproachClassVisitor extends ClassAdapter {
	private String className;;

	public StaticApproachClassVisitor(ClassVisitor mv, String className) {
		super(mv);
		this.className = className;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		// TODO Auto-generated method stub
		return new StaticBranchVisitor(super.visitMethod(access, name, desc, signature, exceptions), className, name);
	}

}
