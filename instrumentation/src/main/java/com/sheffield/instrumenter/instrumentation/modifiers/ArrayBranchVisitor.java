package com.sheffield.instrumenter.instrumentation.modifiers;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.sheffield.instrumenter.instrumentation.objectrepresentation.Branch;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.BranchHit;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.Line;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.LineHit;
import com.sheffield.instrumenter.instrumentation.visitors.ArrayApproachClassVisitor;

public class ArrayBranchVisitor extends MethodAdapter {
	private ArrayApproachClassVisitor parent;
	private String className;
	private String methodName;
	private int currentLine;

	public ArrayBranchVisitor(ArrayApproachClassVisitor parent, MethodVisitor mv, String className, String methodName,
			String desc, int access) {
		super(mv);
		this.className = className;
		this.methodName = methodName;
		this.parent = parent;
	}

	@Override
	public void visitCode() {
		super.visitCode();
	}

	@Override
	public void visitJumpInsn(int opcode, Label label) {
		// TODO Auto-generated method stub
		int trueCounter = parent.newCounterId();
		int falseCounter = parent.newCounterId();
		parent.addBranchHit(new BranchHit(new Branch(className, currentLine), trueCounter, falseCounter));
		switch (opcode) {
			case Opcodes.IF_ICMPEQ:
			case Opcodes.IF_ICMPGE:
			case Opcodes.IF_ICMPLE:
			case Opcodes.IF_ICMPLT:
			case Opcodes.IF_ICMPNE:
			case Opcodes.IF_ICMPGT:
			case Opcodes.IFGE:
			case Opcodes.IFGT:
			case Opcodes.IFLE:
			case Opcodes.IFLT:
			case Opcodes.IFEQ:
			case Opcodes.IFNE:
			case Opcodes.IF_ACMPEQ:
			case Opcodes.IF_ACMPNE:
			case Opcodes.IFNONNULL:
			case Opcodes.IFNULL:
				Label l = new Label();
				Label l2 = new Label();
				mv.visitJumpInsn(opcode, l);
				visitFieldInsn(Opcodes.GETSTATIC, className, ArrayApproachClassVisitor.COUNTER_VARIABLE_NAME,
						ArrayApproachClassVisitor.COUNTER_VARIABLE_DESC);
				visitLdcInsn(trueCounter);
				visitInsn(Opcodes.DUP2);
				visitInsn(Opcodes.IALOAD);
				visitLdcInsn(1);
				visitInsn(Opcodes.IADD);
				visitInsn(Opcodes.IASTORE);
				mv.visitJumpInsn(Opcodes.GOTO, l2);
				visitLabel(l);
				visitFieldInsn(Opcodes.GETSTATIC, className, ArrayApproachClassVisitor.COUNTER_VARIABLE_NAME,
						ArrayApproachClassVisitor.COUNTER_VARIABLE_DESC);
				visitLdcInsn(falseCounter);
				visitInsn(Opcodes.DUP2);
				visitInsn(Opcodes.IALOAD);
				visitLdcInsn(1);
				visitInsn(Opcodes.IADD);
				visitInsn(Opcodes.IASTORE);
				mv.visitJumpInsn(Opcodes.GOTO, label);
				visitLabel(l2);
				break;
			default:
				super.visitJumpInsn(opcode, label);
		}

	}

	@Override
	public void visitLineNumber(int lineNumber, Label label) {
		currentLine = lineNumber;
		int counterId = parent.newCounterId();
		parent.addLineHit(new LineHit(new Line(className, currentLine), counterId));
		visitFieldInsn(Opcodes.GETSTATIC, className, ArrayApproachClassVisitor.COUNTER_VARIABLE_NAME,
				ArrayApproachClassVisitor.COUNTER_VARIABLE_DESC);
		visitLdcInsn(counterId);
		visitInsn(Opcodes.DUP2);
		visitInsn(Opcodes.IALOAD);
		visitLdcInsn(1);
		visitInsn(Opcodes.IADD);
		visitInsn(Opcodes.IASTORE);
		super.visitLineNumber(lineNumber, label);
	}

}
