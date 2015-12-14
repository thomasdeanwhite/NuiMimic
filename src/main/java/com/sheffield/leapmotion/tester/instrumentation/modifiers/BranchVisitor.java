package com.sheffield.leapmotion.tester.instrumentation.modifiers;

import com.sheffield.leapmotion.tester.analysis.BranchType;
import com.sheffield.leapmotion.tester.analysis.ClassAnalyzer;
import org.objectweb.asm.*;

import java.lang.reflect.Method;
import java.util.HashMap;

public class BranchVisitor extends MethodAdapter {
	private int branch = 0;
	private int lastBranchDistance = 0;
	private boolean lookNext = false;
	private String className;
	private String methodName;
	private static final String ANALYZER_CLASS = Type.getInternalName(ClassAnalyzer.class);
	private static Method BRANCH_METHOD;
	private static Method BRANCH_DISTANCE_METHOD_I;
	private static Method BRANCH_DISTANCE_METHOD_F;
	private static Method BRANCH_DISTANCE_METHOD_D;
	private static Method BRANCH_DISTANCE_METHOD_L;

	private HashMap<String, String> labelBranches;

	static {
		try {
			BRANCH_METHOD = ClassAnalyzer.class.getMethod("branchExecuted", new Class[] { String.class });
			BRANCH_DISTANCE_METHOD_I = ClassAnalyzer.class.getMethod("branchExecutedDistance",
					new Class[] { int.class, int.class, String.class });
			BRANCH_DISTANCE_METHOD_F = ClassAnalyzer.class.getMethod("branchExecutedDistance",
					new Class[] { float.class, float.class, String.class });
			BRANCH_DISTANCE_METHOD_D = ClassAnalyzer.class.getMethod("branchExecutedDistance",
					new Class[] { double.class, double.class, String.class });
			BRANCH_DISTANCE_METHOD_L = ClassAnalyzer.class.getMethod("branchExecutedDistance",
					new Class[] { long.class, long.class, String.class });
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public BranchVisitor(MethodVisitor mv, String className, String methodName) {
		super(mv);
		this.className = className;
		this.methodName = methodName;
		labelBranches = new HashMap<String, String>();

	}

	@Override
	public void visitLabel(Label label) {
		mv.visitLabel(label);
		String key = label.toString();
		if (labelBranches.containsKey(key)) {
			visitLdcInsn(labelBranches.get(key) + "[false]");
			visitMethodInsn(Opcodes.INVOKESTATIC, ANALYZER_CLASS, "branchExecuted",
					Type.getMethodDescriptor(BRANCH_METHOD));
		}
	}

	private String getBranchName(int branch) {
		return className + "::" + methodName + "#" + branch;
	}

	@Override
	public void visitJumpInsn(int opcode, Label label) {
		mv.visitJumpInsn(opcode, label);
		BranchType bt = null;
		String branchName = getBranchName(branch);
		switch (opcode) {
		case Opcodes.IF_ICMPEQ:
		case Opcodes.IF_ICMPGE:
		case Opcodes.IF_ICMPLE:
		case Opcodes.IF_ICMPLT:
		case Opcodes.IF_ICMPNE:
		case Opcodes.IF_ICMPGT:
			visitInsn(Opcodes.DUP2);
			visitLdcInsn(branchName);
			visitMethodInsn(Opcodes.INVOKESTATIC, ANALYZER_CLASS, "branchExecutedDistance",
					Type.getMethodDescriptor(BRANCH_DISTANCE_METHOD_I));
			lastBranchDistance = branch;
			lookNext = true;
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
			//mv.visitLdcInsn(new Boolean(false));

			if (lookNext) {
				if (opcode == Opcodes.IF_ICMPEQ || opcode == Opcodes.IFEQ) {
					bt = BranchType.BRANCH_E;
				}

				if (opcode == Opcodes.IF_ICMPGE || opcode == Opcodes.IFGE) {
					bt = BranchType.BRANCH_GE;
				}

				if (opcode == Opcodes.IF_ICMPGT || opcode == Opcodes.IFGT) {
					bt = BranchType.BRANCH_GT;
				}

				if (opcode == Opcodes.IF_ICMPLE || opcode == Opcodes.IFLE) {
					bt = BranchType.BRANCH_LE;
				}

				if (opcode == Opcodes.IF_ICMPLT || opcode == Opcodes.IFLT) {
					bt = BranchType.BRANCH_LT;
				}

				if (bt != null) {
					lookNext = false;
					branchName = getBranchName(lastBranchDistance);
					ClassAnalyzer.branchDistanceFound(branchName, bt);
				}
			}

			labelBranches.put(label.toString(), branchName);
			visitLdcInsn(branchName + "[true]");
			visitMethodInsn(Opcodes.INVOKESTATIC, ANALYZER_CLASS, "branchExecuted",
					Type.getMethodDescriptor(BRANCH_METHOD));
			ClassAnalyzer.branchFound(branchName);

		}

	}

	@Override
	public void visitInsn(int opcode) {
		switch (opcode) {
		case Opcodes.FCMPG:
		case Opcodes.FCMPL: {
			String branchName = getBranchName(branch);
			switch (opcode) {
			case Opcodes.FCMPG:
			case Opcodes.FCMPL:
				visitInsn(Opcodes.DUP2);
				visitLdcInsn(branchName);
				visitMethodInsn(Opcodes.INVOKESTATIC, ANALYZER_CLASS, "branchExecutedDistance",
						Type.getMethodDescriptor(BRANCH_DISTANCE_METHOD_F));
				lookNext = true;
				lastBranchDistance = branch;
				break;
			}

		}

		case Opcodes.LCMP:
		case Opcodes.DCMPG:
		case Opcodes.DCMPL: {

			branch++;
		}
			break;

		}
		mv.visitInsn(opcode);
	}

}
