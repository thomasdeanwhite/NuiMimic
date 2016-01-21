package com.sheffield.leapmotion;

import com.sheffield.instrumenter.Properties;
import com.sheffield.instrumenter.instrumentation.ClassReplacementTransformer;
import com.sheffield.leapmotion.instrumentation.visitors.TestingClassAdapter;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassWriter;

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

//import java.lang.management.ManagementFactory;
//import com.sun.tools.attach.VirtualMachine;

public class LeapMotionApplicationHandler {

	private static boolean JAR_LOADED = false;

	private static String[] nonDependancies;

	private static final Class[] parameters = new Class[] { URL.class };

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

			addUrlToSystemClasspath(new URL("file:/" + jarFile.getName().substring(0, lastSlash)));

			Manifest mf = jarFile.getManifest();

			App.out.println("- Loaded jar: " + url.getPath());
			addUrlToSystemClasspath(url);

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

					String location = "jar:file:/" + Properties.CLASS_PATH;

					location = location.replace("\\", "/");

					for (String mfCp : mfCps) {
						String path = location + "/" + mfCp + "!/";
						addUrlToSystemClasspath(new URL(path));
						App.out.println("Added " + path + " to classpath");
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void instrumentJar(String jar) throws MalformedURLException {
		String jarFilePath = "file:/" + jar;

		if (!JAR_LOADED) {
			loadJar(jar);
		}

		URL url = new URL("jar:" + jarFilePath + "!/");

		ClassReplacementTransformer crp = new ClassReplacementTransformer();
		crp.setWriteClasses(true);

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
				ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
				TestingClassAdapter tca = new TestingClassAdapter(cw, className);
				classes.add(className);
				App.out.print("\t Found " + className);
				try {
					InputStream is = jarFile.getInputStream(je);
					if (is == null) {
						App.out.println();
						continue;
					}
					byte[] classBytes = IOUtils.toByteArray(is);
					crp.transform(className, classBytes, tca, cw);
					App.out.print("\r\t ☑ Instrumented " + className);
				} catch (Throwable e1) {
					App.out.print("\r\t ☒ Failed instrumenting " + className);
					App.out.println();
					//e1.printStackTrace(App.out);
					continue;

				}
			}

//			nonDependancies = new String[classes.size()];
//			classes.toArray(nonDependancies);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace(App.out);
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
