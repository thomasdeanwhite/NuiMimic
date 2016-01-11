package com.sheffield.instrumenter.instrumentation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import com.sheffield.instrumenter.Properties;
import com.sheffield.instrumenter.Properties.InstrumentationApproach;
import com.sheffield.instrumenter.instrumentation.visitors.ArrayApproachClassVisitor;
import com.sheffield.instrumenter.instrumentation.visitors.StaticApproachClassVisitor;

public class InstrumentingClassLoader extends URLClassLoader {

	private static InstrumentingClassLoader instance;
	private ClassLoader classLoader;
	ClassReplacementTransformer crt = new ClassReplacementTransformer();
	private final Map<String, Class<?>> instances = new HashMap<String, Class<?>>();
	private boolean shouldInstrument;

	public void setShouldInstrument(boolean shouldInstrument) {
		this.shouldInstrument = shouldInstrument;
	}

	private InstrumentingClassLoader(URL[] urls) {
		super(urls);
		this.classLoader = getClass().getClassLoader();
	}

	public static void init(InstrumentingClassLoader instance) {
		try {
			Field scl = ClassLoader.class.getDeclaredField("scl");
			scl.setAccessible(true);
			URLClassLoader loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
			scl.set(loader, instance);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * * Add a inmemory representation of a class. * @param name * name of the class * @param bytes * class definition
	 */

	@Override
	public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if (instances.containsKey(name)) {
			return instances.get(name);
		}
		if ("".equals(name)) {
			throw new ClassNotFoundException();
		}

		if (!crt.shouldInstrumentClass(name.replace(".", "/")) || !shouldInstrument) {
			Class<?> cl = findLoadedClass(name);
			if (cl != null) {
				return cl;
			}
			return super.loadClass(name, resolve);
		}
		InputStream stream = null;
		ByteArrayOutputStream out = null;
		try {
			stream = getInputStreamForClass(name);
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
			ClassVisitor cv = Properties.INSTRUMENTATION_APPROACH == InstrumentationApproach.STATIC
					? new StaticApproachClassVisitor(cw, name) : new ArrayApproachClassVisitor(cw, name);
			byte[] bytes = crt.transform(name, IOUtils.toByteArray(stream), cv, cw);

			Class<?> cl = defineClass(name, bytes, 0, bytes.length);
			if (Properties.WRITE_CLASS) {
				File folder = new File(Properties.BYTECODE_DIR);
				folder.mkdirs();
				File output = new File(folder.getAbsolutePath() + "/" + name + ".class");
				output.createNewFile();
				FileOutputStream outFile = new FileOutputStream(output);
				outFile.write(bytes);
				outFile.flush();
				outFile.close();
			}

			instances.put(name, cl);
			if (resolve) {
				resolveClass(cl);
			}
			return cl;
		} catch (final IOException e) {
			throw new ClassNotFoundException("Couldn't instrument class");
		} catch (final IllegalClassFormatException e) {
			throw new ClassNotFoundException("Couldn't instrument class");
		} catch (final Exception e) {
			e.printStackTrace();
			throw new ClassNotFoundException("Couldn't instrument class");
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (stream != null) {
					stream.close();
				}
			} catch (final IOException e) {
				// not much we can do here
			}
		}
	}

	private InputStream getInputStreamForClass(String name) throws ClassNotFoundException {
		String path = name.replace(".", File.separator) + ".class";
		InputStream stream = getResourceAsStream(path);
		if (stream != null) {
			return stream;
		}
		throw new ClassNotFoundException("Could not find class on classpath");
	}

	public static InstrumentingClassLoader getInstance() {
		if (instance == null) {
			URLClassLoader loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
			instance = new InstrumentingClassLoader(loader.getURLs());
			InstrumentingClassLoader.init(instance);
		}
		return instance;
	}

}
