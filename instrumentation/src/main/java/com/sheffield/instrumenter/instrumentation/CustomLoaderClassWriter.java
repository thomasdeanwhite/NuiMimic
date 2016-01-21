package com.sheffield.instrumenter.instrumentation;

import org.objectweb.asm.ClassWriter;

public class CustomLoaderClassWriter extends ClassWriter {
	private InstrumentingClassLoader loader;

	public CustomLoaderClassWriter(int flags, InstrumentingClassLoader loader) {
		super(flags);
		this.loader = loader;
	}

	@Override
	protected String getCommonSuperClass(String type1, String type2) {
		Class<?> c, d;
		try {
			c = loader.loadOriginalClass(type1);
			d = loader.loadOriginalClass(type2);
		} catch (Exception e) {
			throw new RuntimeException(e.toString());
		}
		if (c.isAssignableFrom(d)) {
			return type1;
		}
		if (d.isAssignableFrom(c)) {
			return type2;
		}
		if (c.isInterface() || d.isInterface()) {
			return "java/lang/Object";
		} else {
			do {
				c = c.getSuperclass();
			} while (!c.isAssignableFrom(d));
			return c.getName().replace('.', '/');
		}
	}

}
