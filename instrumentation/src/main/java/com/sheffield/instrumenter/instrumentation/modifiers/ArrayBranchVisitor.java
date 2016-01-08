package com.sheffield.instrumenter.instrumentation.modifiers;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

import com.sheffield.instrumenter.instrumentation.visitors.ArrayApproachClassVisitor;

public class ArrayBranchVisitor extends MethodAdapter {
	private ArrayApproachClassVisitor parent;
	private String className;
	private String methodName;

	public ArrayBranchVisitor(ArrayApproachClassVisitor parent, MethodVisitor mv, String className, String methodName) {
		super(mv);
		this.className = className;
		this.methodName = methodName;
		this.parent = parent;
	}

	@Override
	public void visitJumpInsn(int arg0, Label arg1) {
		// TODO Auto-generated method stub
		super.visitJumpInsn(arg0, arg1);
		int counterId = parent.newCounterId();
	}

	@Override
	public void visitLineNumber(int arg0, Label arg1) {
		super.visitLineNumber(arg0, arg1);
		int counterId = parent.newCounterId();
	}

}
