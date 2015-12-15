package com.sheffield.instrumenter.instrumentation;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.IllegalClassFormatException;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;

public class TestingClassLoader extends ClassLoader {

	private static TestingClassLoader TESTING_CLASS_LOADER;

	private ClassLoader cl = ClassLoader.getSystemClassLoader();

	// public static

	private HashMap<String, Class<?>> classes;

	public static TestingClassLoader getTestingClassLoader() {
		if (TESTING_CLASS_LOADER == null) {
			TESTING_CLASS_LOADER = new TestingClassLoader();
		}
		return TESTING_CLASS_LOADER;
	}

	private TestingClassLoader() {
		super(null);
		classes = new HashMap<String, Class<?>>();
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		return loadClass(name);
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {

		// App.out.print(name + " ");
		// new Exception().printStackTrace(App.out);

		String lname = name.replace("/", ".");
		if (classes.containsKey(lname)) {
			return classes.get(lname);
		}
		Class<?> c = findLoadedClass(name);
		if (c != null) {
			return c;
		}
		try {
			String res = lname.replace(".", "/") + ".class";
			InputStream is = getSystemClassLoader().getResourceAsStream(res);
			if (is == null) {
				return getSystemClassLoader().loadClass(name);
			}
			byte[] bytes = IOUtils.toByteArray(is);
			ClassReplacementTransformer ctr = new ClassReplacementTransformer();
			if (!ctr.shouldInstrumentClass(lname)) {
				return cl.loadClass(name);
			}
			try {
				bytes = ctr.transform(this, name, null, null, bytes);
				return loadClass(name, bytes);
			} catch (IllegalClassFormatException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Class<?> clazz = cl.loadClass(name);
		classes.put(lname, clazz);
		return clazz;
	}

	public Class<?> loadClass(String name, byte[] bytes) {
		name = name.replace("/", ".");
		Class<?> c = findLoadedClass(name);
		if (c != null) {
			return c;
		}
		if (!classes.containsKey(name)) {
			classes.put(name, defineClass(name, bytes, 0, bytes.length));
		}

		return classes.get(name);
	}

}
