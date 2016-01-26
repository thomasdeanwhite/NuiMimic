package com.sheffield.instrumenter.instrumentation.modifiers;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.sheffield.instrumenter.analysis.ClassAnalyzer;
import com.sheffield.instrumenter.instrumentation.visitors.StaticApproachClassVisitor;

public class StaticLineVisitor extends MethodVisitor {
	private String className;

	public StaticLineVisitor(MethodVisitor arg0, String className) {
		super(Opcodes.ASM5, arg0);
		this.className = className;
	}

	@Override
	public void visitLineNumber(int lineNumber, Label label) {
		ClassAnalyzer.lineFound(className, lineNumber);
		visitLdcInsn(className);
		visitLdcInsn(lineNumber);
		visitMethodInsn(Opcodes.INVOKESTATIC, StaticApproachClassVisitor.ANALYZER_CLASS, "lineExecuted",
				"(Ljava/lang/String;I)V", false);
		mv.visitLineNumber(lineNumber, label);
	}

}
