package com.sheffield.instrumenter.instrumentation;

import java.util.HashMap;

public class ClassStore {
	private static HashMap<String, Class<?>> store = new HashMap<String, Class<?>>();
	/**
	 *
	 */
	private static final long serialVersionUID = -1002975153253026174L;

	public static void put(String name, Class<?> cl) {
		store.put(name, cl);
	}

	public static Class<?> get(String name) {
		name = name.replace('/', '.');
		if(store.containsKey(name)){
			return store.get(name);
		}
		return null;
	}
}
