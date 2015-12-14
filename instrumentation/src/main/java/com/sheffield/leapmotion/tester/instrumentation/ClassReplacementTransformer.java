package com.sheffield.leapmotion.tester.instrumentation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import com.sheffield.leapmotion.tester.Properties;

public class ClassReplacementTransformer implements ClassFileTransformer {

	public static final String CONTROLLER_CLASS = "com.leapmotion.leap.Controller";

	private static ArrayList<String> seenClasses = new ArrayList<String>();
	private ClassVisitor cv;

	public ClassReplacementTransformer() {

	}

	public ClassReplacementTransformer(ClassVisitor cv) {
		this.cv = cv;
	}

	public void setClassVisitor(ClassVisitor cv) {
		this.cv = cv;
	}

	@Override
	public byte[] transform(ClassLoader cLoader, String cName, Class<?> iClass, ProtectionDomain pDomain, byte[] cBytes)
			throws IllegalClassFormatException {
		// if (TestingClassLoader.getClassLoader().isClassFinalized(cName)) {
		// throw new IllegalClassFormatException();
		// }

		if (seenClasses.contains(cName)) {
			throw new IllegalClassFormatException("Class already loaded!");
		}
		seenClasses.add(cName);

		if (Properties.EXILED_CLASSES != null) {
			for (String s : Properties.EXILED_CLASSES) {
				if (cName.equals(s)) {
					// App.out.println("Not loaded class " + cName);
					throw new IllegalClassFormatException();
				}
			}
		}

		// App.out.println("Loaded class " + cName);
		try {
			if (!shouldInstrumentClass(cName)) {
				return cBytes;
			}

			// if (iClass == null) {
			// iClass = TestingClassLoader.getClassLoader().loadClass(cName,
			// cBytes);
			// }

			// iClass.

			InputStream ins = new ByteArrayInputStream(cBytes);
			byte[] newClass = cBytes;
			try {
				ClassReader cr = new ClassReader(ins);
				ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
				if (cv == null) {
					cv = cw;
				}
				try {
					cr.accept(cv, 0);
				} catch (Throwable t) {
					return cBytes;
				}

				newClass = cw.toByteArray();
			} catch (IOException e) {
			}
			File file = new File("classes/" + cName + ".class");
			file.getParentFile().mkdirs();
			file.createNewFile();
			// App.out.println("- Writing new class " + file.getAbsolutePath());
			FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
			fos.write(newClass);
			fos.close();
			return newClass;
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace(/* App.out */);
		}
		return cBytes;

	}

	private static final String[] forbiddenPackages = new String[] { "com/sheffield/leapmotion", "com/google/gson",
			"com/sun", "java/", "sun/", "com/leapmotion", "jdk/", "javax/", "org/json", "org/apache/commons/cli" };

	public boolean shouldInstrumentClass(String className) {
		if (className == null) {
			return false;
		}
		if (className.contains(".")) {
			className = className.replace(".", "/");
		}
		if (isForbiddenPackage(className)) {
			return false;
		}
		if (className.contains("/")) {
			className = className.replace("/", ".");
		}
		if (Properties.INSTRUMENTED_PACKAGES == null) {
			return true;
		}
		for (String s : Properties.INSTRUMENTED_PACKAGES) {
			if (className.startsWith(s)) {
				return true;
			}
		}

		return false;
	}

	public static boolean isForbiddenPackage(String clazz) {
		for (String s : forbiddenPackages) {
			if (clazz.startsWith(s)) {
				return true;
			}
		}
		return false;
	}
}
