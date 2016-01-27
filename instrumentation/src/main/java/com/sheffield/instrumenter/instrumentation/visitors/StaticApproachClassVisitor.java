package com.sheffield.instrumenter.instrumentation.visitors;

<<<<<<< HEAD
import com.sheffield.instrumenter.instrumentation.modifiers.StaticBranchVisitor;
import org.objectweb.asm.ClassAdapter;
=======
>>>>>>> abd4c13593b08f6306f1f0890a061c4bc7d98454
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

<<<<<<< HEAD
public class StaticApproachClassVisitor extends ClassAdapter {
	private String className;;
	private ClassVisitor classVisitor;
=======
import com.sheffield.instrumenter.Properties;
import com.sheffield.instrumenter.analysis.ClassAnalyzer;
import com.sheffield.instrumenter.instrumentation.modifiers.StaticBranchVisitor;
import com.sheffield.instrumenter.instrumentation.modifiers.StaticLineVisitor;

public class StaticApproachClassVisitor extends ClassVisitor {
	public static final String ANALYZER_CLASS = Type.getInternalName(ClassAnalyzer.class);
	private String className;
>>>>>>> abd4c13593b08f6306f1f0890a061c4bc7d98454

	public StaticApproachClassVisitor(ClassVisitor mv, String className) {
		super(Opcodes.ASM5, mv);
		this.className = className;
		this.classVisitor = classVisitor;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
<<<<<<< HEAD
		return new StaticBranchVisitor(super.visitMethod(access, name, desc, signature, exceptions),
				className, name);
=======
		// TODO Auto-generated method stub
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		if (Properties.INSTRUMENT_BRANCHES) {
			mv = new StaticBranchVisitor(mv, className, name);
		}
		if (Properties.INSTRUMENT_LINES) {
			mv = new StaticLineVisitor(mv, className);
		}
		return mv;
>>>>>>> abd4c13593b08f6306f1f0890a061c4bc7d98454
	}

}
