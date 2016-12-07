package com.sheffield.leapmotion.util;

import com.sheffield.instrumenter.analysis.ClassAnalyzer;
import com.sheffield.instrumenter.instrumentation.ClassReplacementTransformer;
import com.sheffield.instrumenter.instrumentation.InstrumentingClassLoader;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.instrumentation.visitors.TestingClassAdapter;
import com.sheffield.util.ClassNameUtils;
import org.objectweb.asm.ClassVisitor;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

//import java.lang.management.ManagementFactory;
//import com.sun.tools.attach.VirtualMachine;

public class LeapMotionApplicationHandler {

	private static boolean JAR_LOADED = false;

	private static ArrayList<String> nonDependancies;

	private static final Class[] parameters = new Class[] { URL.class };

	private static final InstrumentingClassLoader INSTRUMENTING_CLASS_LOADER = InstrumentingClassLoader.getInstance();

	static {
		nonDependancies = new ArrayList<String>();

		INSTRUMENTING_CLASS_LOADER.getClassReplacementTransformer().addShouldInstrumentChecker(new ClassReplacementTransformer.ShouldInstrumentChecker() {
			@Override
			public boolean shouldInstrument(String className) {
				for (String s : nonDependancies){
					if (className.startsWith(s)){
						return true;
					}
				}
				return false;
			}
		});

		INSTRUMENTING_CLASS_LOADER.setBuildDependencyTree(true);

		INSTRUMENTING_CLASS_LOADER.getClassReplacementTransformer().setWriteClasses(true);



		INSTRUMENTING_CLASS_LOADER.setShouldInstrument(true);
	}

	// private static VirtualMachine virtualMachine;

	public static void loadJar(String jar) throws MalformedURLException {
		JAR_LOADED = true;
		String jarFilePath = "file:/" + jar;

		URL url = new URL("jar:" + jarFilePath + "!/");

		// if (Properties.LIBRARY.length() > 0) {
		// // library has been set
		// System.load(Properties.LIBRARY + "/LeapJava.dll");
		// }

		try {
			addUrlToSystemClasspath(url);


			JarURLConnection uc = (JarURLConnection) url.openConnection();

			JarFile jarFile = uc.getJarFile();

			int lastSlash = jarFile.getName().lastIndexOf("/");

			if (lastSlash <= 0) {
				lastSlash = jarFile.getName().lastIndexOf("\\");
			}

			Manifest mf = jarFile.getManifest();

			App.out.println(">> Added to ClassPath: " + url);

			String mainClass = mf.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);

			String mfClassPath = "";

			if (Properties.CLASS_PATH.length() > 0) {
				File folder = new File(Properties.CLASS_PATH);

				File[] files = folder.listFiles();

				for (File f : files) {
					mfClassPath += f.getName() + " ";
					String path = "jar:" + f.toURI().toURL() + "!/";
					addUrlToSystemClasspath(new URL(path));
					App.out.println(">> Added to ClassPath: " + path);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void instrumentJar(String jar) throws MalformedURLException {
		String jarFilePath = "file:/" + jar;

		INSTRUMENTING_CLASS_LOADER.addClassInstrumentingInterceptor(new InstrumentingClassLoader.ClassInstrumentingInterceptor() {
			@Override
			public ClassVisitor intercept(ClassVisitor parent, String name) {
				return new TestingClassAdapter(parent, name);
			}
		});


		if (!JAR_LOADED) {
			loadJar(jar);
		}

		URL url = new URL("jar:" + jarFilePath + "!/");


		try {
			JarURLConnection uc = (JarURLConnection) url.openConnection();
			JarFile jarFile = uc.getJarFile();

			Enumeration e = jarFile.entries();
			ArrayList<JarEntry> entries = new ArrayList<JarEntry>();

			//Loop through JAR, seeing what classes it contains.
			while (e.hasMoreElements()) {
				JarEntry je = (JarEntry) e.nextElement();

				//is entry a class file?
				if (je.isDirectory() || !je.getName().endsWith(".class")) {
					continue;
				}

				entries.add(je);

				String className = je.getName().substring(0, je.getName().length() - ".class".length());
				nonDependancies.add(ClassNameUtils.standardise(className));
			}

			ArrayList<String> classes = new ArrayList<String>();

			ClassAnalyzer.setOut(App.out);

			Properties.WRITE_CLASS = true;
			int index = jar.lastIndexOf("/");
			if (index == -1){
				index = jar.lastIndexOf("\\");
			}

			String jarPath = jar.substring(0, index).replace("\\", "/");

			Properties.BYTECODE_DIR = jarPath + "/classes";

			for (JarEntry je : entries){

				String className = je.getName().substring(0, je.getName().length() - ".class".length());

//				if (!(className.startsWith("com/zf") || className.startsWith("com/jme3"))){
//					continue;
//				}
				classes.add(className);
				App.out.print("\t Found " + className);
				try {
					Class c = INSTRUMENTING_CLASS_LOADER.loadClass(className.replace("/", "."));
					App.out.print("\r\t ☑ Instrumented " + className);
				} catch (Throwable e1) {
					App.out.print("\r\t ☒ Failed instrumenting " + className);
					App.out.println();
					e1.printStackTrace(App.out);
					continue;

				}
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace(App.out);
		}

		App.out.println("\r!! Instrumentation finished!");

	}

	public static void addUrlToSystemClasspath(URL url) {
		INSTRUMENTING_CLASS_LOADER.addURL(url);
	}

}
