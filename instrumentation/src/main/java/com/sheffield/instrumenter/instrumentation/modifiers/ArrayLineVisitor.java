package com.sheffield.instrumenter.instrumentation.modifiers;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.sheffield.instrumenter.instrumentation.objectrepresentation.Line;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.LineHit;
import com.sheffield.instrumenter.instrumentation.visitors.ArrayApproachClassVisitor;

public class ArrayLineVisitor extends MethodVisitor {
	private ArrayApproachClassVisitor parent;
	private String className;

	public ArrayLineVisitor(ArrayApproachClassVisitor parent, MethodVisitor mv, String className) {
		super(Opcodes.ASM5, mv);
		this.className = className;
		this.parent = parent;
	}

	@Override
	public void visitLineNumber(int lineNumber, Label label) {
		int counterId = parent.newCounterId();
		parent.addLineHit(new LineHit(new Line(className, lineNumber), counterId));
		visitFieldInsn(Opcodes.GETSTATIC, className, ArrayApproachClassVisitor.COUNTER_VARIABLE_NAME,
				ArrayApproachClassVisitor.COUNTER_VARIABLE_DESC);
		visitLdcInsn(counterId);
		visitInsn(Opcodes.DUP2);
		visitInsn(Opcodes.IALOAD);
		visitLdcInsn(1);
		visitInsn(Opcodes.IADD);
		visitInsn(Opcodes.IASTORE);
		mv.visitLineNumber(lineNumber, label);
	}
}
