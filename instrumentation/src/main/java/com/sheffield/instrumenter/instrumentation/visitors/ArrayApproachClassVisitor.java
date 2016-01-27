package com.sheffield.instrumenter.instrumentation.visitors;

import com.sheffield.instrumenter.analysis.ClassAnalyzer;
import com.sheffield.instrumenter.instrumentation.modifiers.ArrayBranchVisitor;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.BranchHit;
import com.sheffield.instrumenter.instrumentation.objectrepresentation.LineHit;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ArrayApproachClassVisitor extends ClassAdapter {
	private String className;
	public static final String COUNTER_VARIABLE_NAME = "__hitCounters";
	public static final String COUNTER_VARIABLE_DESC = "[I";
	public static final String COUNTER_METHOD_NAME = "__getHitCounters";
	public static final String COUNTER_METHOD_DESC = "()[I";
	public static final String RESET_COUNTER_METHOD_NAME = "__resetCounters";
	public static final String RESET_COUNTER_METHOD_DESC = "()V";
	public static final String INIT_METHOD_NAME = "__instrumentationInit";
	public static final String INIT_METHOD_DESC = "()V";
	private int access;
	private AtomicInteger counter = new AtomicInteger(0);
	private List<BranchHit> branchHitCounterIds = new ArrayList<BranchHit>();
	private List<LineHit> lineHitCounterIds = new ArrayList<LineHit>();

	public int newCounterId() {
		return counter.getAndIncrement();
	}

	public void addBranchHit(BranchHit branch) {
		branchHitCounterIds.add(branch);
	}

	public void addLineHit(LineHit line) {
		lineHitCounterIds.add(line);
	}

	public ArrayApproachClassVisitor(ClassVisitor mv, String className) {
		super(mv);
		this.className = className.replace('.', '/');
	}

	@Override
	public void visit(int arg0, int access, String arg2, String arg3, String arg4, String[] arg5) {
		super.visit(arg0, access, arg2, arg3, arg4, arg5);
		this.access = access;
		if ((access & Opcodes.ACC_INTERFACE) == 0) {
			FieldVisitor fv = cv.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, COUNTER_VARIABLE_NAME,
					COUNTER_VARIABLE_DESC, null, null);
			fv.visitEnd();
		}
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		if((access & Opcodes.ACC_STATIC) != 0 || name.equals("<init>")){
			mv.visitMethodInsn(Opcodes.INVOKESTATIC,className,INIT_METHOD_NAME,INIT_METHOD_DESC);
		}
		ArrayBranchVisitor abv = new ArrayBranchVisitor(this, mv, className, name, desc, access);
		return abv;
	}

	@Override
	public void visitEnd() {
		// create visits to our own methods to collect hits
		if ((access & Opcodes.ACC_INTERFACE) == 0) {
			addGetCounterMethod(cv);
			addResetCounterMethod(cv);
			addInitMethod(cv);
			ClassAnalyzer.classAnalyzed(className, branchHitCounterIds, lineHitCounterIds);
		}
		super.visitEnd();
	}

	private void addGetCounterMethod(ClassVisitor cv) {
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, COUNTER_METHOD_NAME,
				COUNTER_METHOD_DESC, null, null);
		mv.visitCode();
		mv.visitFieldInsn(Opcodes.GETSTATIC, className, COUNTER_VARIABLE_NAME, COUNTER_VARIABLE_DESC);
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	private void addResetCounterMethod(ClassVisitor cv) {
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, RESET_COUNTER_METHOD_NAME,
				RESET_COUNTER_METHOD_DESC, null, null);
		mv.visitCode();
		mv.visitFieldInsn(Opcodes.GETSTATIC, className, COUNTER_VARIABLE_NAME, COUNTER_VARIABLE_DESC);
		mv.visitInsn(Opcodes.ARRAYLENGTH);
		mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
		mv.visitFieldInsn(Opcodes.PUTSTATIC, className, COUNTER_VARIABLE_NAME, COUNTER_VARIABLE_DESC);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

	}

	private void addInitMethod(ClassVisitor cv) {
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, INIT_METHOD_NAME, INIT_METHOD_DESC,
				null, null);
		mv.visitCode();
		Label l = new Label();
		mv.visitFieldInsn(Opcodes.GETSTATIC, className, COUNTER_VARIABLE_NAME, COUNTER_VARIABLE_DESC);
		mv.visitJumpInsn(Opcodes.IFNONNULL, l);
		int count = counter.get();
		mv.visitLdcInsn(count);
		mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
		mv.visitFieldInsn(Opcodes.PUTSTATIC, className, COUNTER_VARIABLE_NAME, COUNTER_VARIABLE_DESC);
		mv.visitLabel(l);

		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

}
