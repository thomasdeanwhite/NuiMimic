package com.sheffield.instrumenter.instrumentation;

import com.sheffield.instrumenter.Properties;
import com.sheffield.instrumenter.analysis.ClassAnalyzer;
import com.sheffield.instrumenter.instrumentation.visitors.ArrayClassVisitor;
import com.sheffield.instrumenter.instrumentation.visitors.DependencyTreeClassVisitor;
import com.sheffield.instrumenter.instrumentation.visitors.StaticClassVisitor;
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
import java.util.ArrayList;

public class InstrumentingClassLoader extends URLClassLoader {

	private static InstrumentingClassLoader instance;
	private ClassLoader classLoader;
	private ClassReplacementTransformer crt = new ClassReplacementTransformer();
	private boolean shouldInstrument;
	private MockClassLoader loader;
	private ArrayList<ClassInstrumentingInterceptor> classInstrumentingInterceptors;

	private boolean buildDependencyTree = false;

	public interface ClassInstrumentingInterceptor {
		public ClassVisitor intercept(ClassVisitor parent, String className);
	}

	public void setBuildDependencyTree(boolean b){
		buildDependencyTree = true;
	}

	public void addClassInstrumentingInterceptor(ClassInstrumentingInterceptor cii) {
		ClassAnalyzer.out.printf("- Added ClassInstrumentingInterceptor: %s.\n", cii.getClass().getName());
		classInstrumentingInterceptors.add(cii);
	}

	public void removeClassInstrumentingInterceptor(ClassInstrumentingInterceptor cii) {
		classInstrumentingInterceptors.remove(cii);
	}

	public void setShouldInstrument(boolean shouldInstrument) {
		this.shouldInstrument = shouldInstrument;
	}

	private InstrumentingClassLoader(URL[] urls) {
		super(urls);
		loader = new MockClassLoader(urls);
		this.classLoader = getClass().getClassLoader();
		classInstrumentingInterceptors = new ArrayList<ClassInstrumentingInterceptor>();
		init();

	}

	@Override
	public void addURL(URL u) {
		super.addURL(u);
		loader.addURL(u);
		// Add url to system class loader.
	}

	private void init() {
		try {
			Field scl = ClassLoader.class.getDeclaredField("scl");
			scl.setAccessible(true);
			URLClassLoader loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
			scl.set(loader, this);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public ClassReplacementTransformer getClassReplacementTransformer() {
		return crt;
	}

	/**
	 * * Add a inmemory representation of a class. * @param name * name of the class * @param bytes * class definition
	 */

	@Override
	public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		String className = name.replace('/', '.');
		if (ClassStore.containsKey(className)) {
			return ClassStore.get(className);
		}
		if ("".equals(className)) {
			throw new ClassNotFoundException();
		}

		if (!crt.shouldInstrumentClass(className) || !shouldInstrument) {
			Class<?> cl = findLoadedClass(className);
			if (cl != null) {
				return cl;
			}
			return super.loadClass(className, resolve);
		}
		InputStream stream = null;
		ByteArrayOutputStream out = null;
		try {

			stream = getInputStreamForClass(name);
			ClassWriter writer = new CustomLoaderClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS,
					this);
			ClassVisitor cw = writer;
			for (ClassInstrumentingInterceptor cii : classInstrumentingInterceptors) {
				ClassVisitor newVisitor = cii.intercept(cw, name);
				if (newVisitor != null) {
					cw = newVisitor;
				}
			}

			ClassVisitor cv = Properties.INSTRUMENTATION_APPROACH == Properties.InstrumentationApproach.STATIC
					? new StaticClassVisitor(cw, name) : new ArrayClassVisitor(cw, name);

			if (buildDependencyTree){
				cv = new DependencyTreeClassVisitor(cv, name);
			}

			byte[] bytes = crt.transform(name, IOUtils.toByteArray(stream), cv, writer);
			if (Properties.WRITE_CLASS) {
				String outputDir = Properties.BYTECODE_DIR + "/"
						+ name.replace(".", "/").substring(0, name.lastIndexOf("."));
				File folder = new File(outputDir);
				folder.mkdirs();
				File output = new File(
						folder.getAbsolutePath() + "/" + name.substring(name.lastIndexOf(".") + 1) + ".class");
				output.createNewFile();
				FileOutputStream outFile = new FileOutputStream(output);
				outFile.write(bytes);
				outFile.flush();
				outFile.close();
			}
			Class<?> cl = null;
			try {
				cl = defineClass(className, bytes, 0, bytes.length);
			} catch (final Throwable e) {
				e.printStackTrace(ClassAnalyzer.out);
			}

			ClassStore.put(className, cl);
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
				return loader.loadClass(name);
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
