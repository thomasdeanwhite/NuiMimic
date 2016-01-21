package com.sheffield.instrumenter.instrumentation;

import com.sheffield.instrumenter.Properties;
import com.sheffield.instrumenter.Properties.InstrumentationApproach;
import com.sheffield.instrumenter.analysis.ClassAnalyzer;
import com.sheffield.instrumenter.instrumentation.visitors.ArrayApproachClassVisitor;
import com.sheffield.instrumenter.instrumentation.visitors.StaticApproachClassVisitor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;

public class InstrumentingClassLoader extends URLClassLoader {

	private static InstrumentingClassLoader instance;
	private ClassLoader classLoader;
	ClassReplacementTransformer crt = new ClassReplacementTransformer();
	private boolean shouldInstrument;
	private MockClassLoader loader;

	public void setShouldInstrument(boolean shouldInstrument) {
		this.shouldInstrument = shouldInstrument;
	}

	private InstrumentingClassLoader(URL[] urls) {
		super(urls);
		loader = new MockClassLoader(urls);
		this.classLoader = getClass().getClassLoader();
	}

	@Override
	public void addURL(URL u) {
		super.addURL(u);
		loader.addURL(u);
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

	public ClassReplacementTransformer getClassReplacementTransformer(){
		return crt;
	}

	/**
	 * * Add a inmemory representation of a class. * @param name * name of the class * @param bytes * class definition
	 */

	@Override
	public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if (ClassStore.get(name) != null) {
			return ClassStore.get(name);
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
			ClassWriter cw = new CustomLoaderClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, this);
			ClassVisitor cv = Properties.INSTRUMENTATION_APPROACH == InstrumentationApproach.STATIC
					? new StaticApproachClassVisitor(cw, name) : new ArrayApproachClassVisitor(cw, name);
			byte[] bytes = crt.transform(name, IOUtils.toByteArray(stream), cv, cw);
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
			Class<?> cl = null;
			try {
				cl = defineClass(name, bytes, 0, bytes.length);
			} catch (final Throwable e) {
				e.printStackTrace(ClassAnalyzer.out);
			}

			ClassStore.put(name, cl);
			if (resolve) {
				resolveClass(cl);
			}
			return cl;
		} catch (final IOException e) {
			throw new ClassNotFoundException("Couldn't instrument class" + e.getLocalizedMessage());
		} catch (final IllegalClassFormatException e) {
			throw new ClassNotFoundException("Couldn't instrument class" + e.getLocalizedMessage());
		} catch (final Exception e) {
			e.printStackTrace();
			throw new ClassNotFoundException("Couldn't instrument class " + e.getLocalizedMessage());
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

	public Class<?> loadOriginalClass(String name) throws ClassNotFoundException {
		name = name.replace("/", ".");
		try {
			if (!crt.shouldInstrumentClass(name.replace(".", "/")) || !shouldInstrument) {
				Class<?> cl = findLoadedClass(name);
				if (cl != null) {
					return cl;
				}
				URL[] urls = getURLs();
				return classLoader.loadClass(name);
			}
			Class<?> cl = loader.loadOriginalClass(name);
			return cl;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new ClassNotFoundException();

	}

	private InputStream getInputStreamForClass(String name) throws ClassNotFoundException {
		String path = name.replace(".", "/") + ".class";
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

	private class MockClassLoader extends URLClassLoader {
		public MockClassLoader(URL[] urls) {
			super(urls);
		}

		private Class<?> loadOriginalClass(String name) throws IOException, ClassNotFoundException {
			InputStream stream;
			Class<?> cl = findLoadedClass(name);
			if (cl == null) {
				stream = getInputStreamForClass(name);
				byte[] bytes = IOUtils.toByteArray(stream);
				cl = defineClass(name, bytes, 0, bytes.length);
			}
			return cl;
		}

		private Class<?> defClass(String name, byte[] bytes, int offset, int length) {
			return super.defineClass(name, bytes, offset, length);
		}

		@Override
		protected void addURL(URL u) {
			super.addURL(u);
		}

	}

}
