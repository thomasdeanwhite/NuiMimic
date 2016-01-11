package com.sheffield.instrumenter.instrumentation.visitors;

import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.sheffield.instrumenter.instrumentation.modifiers.ArrayBranchVisitor;

public class ArrayApproachClassVisitor extends ClassAdapter {
	private String className;
	public static final String COUNTER_VARIABLE_NAME = "__hitCounters";
	public static final String COUNTER_VARIABLE_DESC = "[I";
	public static final String COUNTER_METHOD_NAME = "__getHitCounters";
	public static final String COUNTER_METHOD_DESC = "()[I";
	public static final String RESET_COUNTER_METHOD_NAME = "__resetCounters";
	public static final String RESET_COUNTER_METHOD_DESC = "()V";
	private AtomicInteger counter = new AtomicInteger(0);

	public int newCounterId() {
		return counter.incrementAndGet();
	}

	public ArrayApproachClassVisitor(ClassVisitor mv, String className) {
		super(mv);
		this.className = className;
	}

	@Override
	public void visit(int arg0, int arg1, String arg2, String arg3, String arg4, String[] arg5) {
		super.visit(arg0, arg1, arg2, arg3, arg4, arg5);
		FieldVisitor fv = cv.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, COUNTER_VARIABLE_NAME,
				COUNTER_VARIABLE_DESC, null, null);
		fv.visitEnd();
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		// TODO Auto-generated method stub
		return new ArrayBranchVisitor(this, super.visitMethod(access, name, desc, signature, exceptions), className,
				name);
	}

	@Override
	public void visitEnd() {
		// create visits to our own methods to collect hits
		MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, COUNTER_METHOD_NAME,
				COUNTER_METHOD_DESC, null, null);
		mv.visitCode();
		mv.visitFieldInsn(Opcodes.GETSTATIC, className, COUNTER_VARIABLE_NAME, COUNTER_VARIABLE_DESC);
		mv.visitInsn(Opcodes.ARETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		mv = cv.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, RESET_COUNTER_METHOD_NAME,
				RESET_COUNTER_METHOD_DESC, null, null);
		mv.visitCode();
		mv.visitFieldInsn(Opcodes.GETSTATIC, className, COUNTER_VARIABLE_NAME, COUNTER_VARIABLE_DESC);
		mv.visitInsn(Opcodes.ARRAYLENGTH);
		mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
		mv.visitFieldInsn(Opcodes.PUTSTATIC, className, COUNTER_VARIABLE_NAME, COUNTER_VARIABLE_DESC);
		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		super.visitEnd();
	}

}
