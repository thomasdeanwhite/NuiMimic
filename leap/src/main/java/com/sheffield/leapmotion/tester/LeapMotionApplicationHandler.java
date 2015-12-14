package com.sheffield.leapmotion.tester;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassWriter;

import com.sheffield.leapmotion.tester.instrumentation.ClassReplacementTransformer;
import com.sheffield.leapmotion.tester.instrumentation.TestingClassLoader;
import com.sheffield.leapmotion.tester.instrumentation.visitors.TestingClassAdapter;

//import java.lang.management.ManagementFactory;
//import com.sun.tools.attach.VirtualMachine;

public class LeapMotionApplicationHandler {

	private static boolean JAR_LOADED = false;

	private static String[] nonDependancies;

	private static final Class[] parameters = new Class[] { URL.class };

	// private static VirtualMachine virtualMachine;

	public static String loadJar(String jar) throws MalformedURLException {
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

			Manifest mf = jarFile.getManifest();

			App.out.println("- Loaded jar file: " + url.getPath());

			String mainClass = mf.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);

			String mfClassPath = "";

			if (Properties.CLASS_PATH.length() > 0) {
				File folder = new File(Properties.CLASS_PATH);

				File[] files = folder.listFiles();

				for (File f : files) {
					mfClassPath += f.getName() + " ";
				}

				if (mfClassPath != null) {
					String[] mfCps = mfClassPath.split(" ");

					int lastSlash = jarFile.getName().lastIndexOf("/");

					if (lastSlash <= 0) {
						lastSlash = jarFile.getName().lastIndexOf("\\");
					}

					String location = "jar:file:" + jarFile.getName().substring(0, lastSlash);

					location = location.replace("\\", "/");

					for (String mfCp : mfCps) {
						String path = location + "/" + mfCp + "!/";
						addUrlToSystemClasspath(new URL(path));
						App.out.println("Added " + path + " to classpath");
					}
				}
			}

			Class mClass = TestingClassLoader.getTestingClassLoader().loadClass(mainClass);

			App.out.println("- Found main class " + mClass.getName());

			return mClass.getName();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

	public static void instrumentJar(String jar) throws MalformedURLException {
		String jarFilePath = "file:/" + jar;

		if (!JAR_LOADED) {
			loadJar(jar);
		}

		URL url = new URL("jar:" + jarFilePath + "!/");

		ClassLoader cl = ClassLoader.getSystemClassLoader();
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		ClassReplacementTransformer crp = new ClassReplacementTransformer();

		try {
			JarURLConnection uc = (JarURLConnection) url.openConnection();
			JarFile jarFile = uc.getJarFile();

			Enumeration e = jarFile.entries();

			ArrayList<String> classes = new ArrayList<String>();
			while (e.hasMoreElements()) {
				JarEntry je = (JarEntry) e.nextElement();
				if (je.isDirectory() || !je.getName().endsWith(".class")) {
					continue;
				}

				String className = je.getName().substring(0, je.getName().length() - 6);

				TestingClassAdapter tca = new TestingClassAdapter(cw, className);
				crp.setClassVisitor(tca);
				classes.add(className);
				App.out.print("\t ☒ Found " + className);
				try {
					InputStream is = jarFile.getInputStream(je);
					if (is == null) {
						App.out.println();
						continue;
					}
					byte[] classBytes = IOUtils.toByteArray(is);
					crp.transform(TestingClassLoader.getTestingClassLoader(), className, null, null, classBytes);
					App.out.print("\r\t ☑ Instrumented " + className + "\n");
				} catch (Exception e1) {
					App.out.println();
					continue;

				}
			}

			nonDependancies = new String[classes.size()];
			classes.toArray(nonDependancies);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public static void loadAgent(String agentJar) {
		// String runningVm = ManagementFactory.getRuntimeMXBean().getName();
		// int endOfPId = runningVm.indexOf("@");
		// String pId = runningVm.substring(0, endOfPId);
		//
		// try {
		// virtualMachine = VirtualMachine.attach(pId);
		//
		// virtualMachine.loadAgent(agentJar);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	public static void cleanUp() {
		// try {
		// virtualMachine.detach();
		// } catch (Throwable t) {
		// // TODO Auto-generated catch block
		// t.printStackTrace();
		// }
	}

	private static Method classpathMethod = null;

	public static void addUrlToSystemClasspath(URL url) {
		try {
			URLClassLoader sloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
			if (classpathMethod == null) {
				Class sclass = URLClassLoader.class;
				Method method = sclass.getDeclaredMethod("addURL", parameters);
				method.setAccessible(true);
				classpathMethod = method;
			}
			classpathMethod.invoke(sloader, new Object[] { url });
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}

}
