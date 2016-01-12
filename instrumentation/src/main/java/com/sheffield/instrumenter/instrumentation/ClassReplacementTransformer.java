package com.sheffield.instrumenter.instrumentation;

import com.sheffield.instrumenter.Properties;
import com.sheffield.instrumenter.analysis.ClassAnalyzer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.*;
import java.lang.instrument.IllegalClassFormatException;
import java.util.ArrayList;

public class ClassReplacementTransformer {

	private static ArrayList<String> seenClasses = new ArrayList<String>();

	public ClassReplacementTransformer() {

	}

	public byte[] transform(String cName, byte[] cBytes, ClassVisitor cv, ClassWriter cw)
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

				cr.accept(cv, ClassReader.EXPAND_FRAMES);

				newClass = cw.toByteArray();
			} catch (IOException e) {
				e.printStackTrace();
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
			e.printStackTrace(ClassAnalyzer.out);
			System.exit(2);
		}
		return cBytes;

	}

	private static final String[] forbiddenPackages = new String[] { "com/sheffield/leapmotion", "com/google/gson",
			"com/sun", "java/", "sun/", "com/leapmotion", "jdk/", "javax/", "org/json", "org/apache/commons/cli",
			"com/sheffield/instrumenter", "com/dpaterson", "org/junit" };

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
