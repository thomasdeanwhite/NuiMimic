package com.sheffield.instrumenter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Properties implements PropertySource {
	private Properties() {
		reflectMap();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Parameter {
		String key();

		String group()

		default "Experimental";

		String description();
	}

	public static String DIRECTORY = "C:/data/leap-motion/processed";

	public static String PLAYBACK_FILE = null;

	// Number of times to change frames per second
	public static long SWITCH_RATE = 100;

	public static int SWITCH_TIME = 500;

	public static long DELAY_TIME = 2000;

	public static boolean SMOOTH_ANIMATION = true;

	public static String SUT = null;

	public static String LIBRARY = "";

	public static String CLASS_PATH = "";

	public static String LM_AGENT_JAR = "lm-agent.jar";
	
	public static boolean RECORDING = false;

	public static String[] EXILED_CLASSES;

	public enum FrameSelectionStrategy {
		RANDOM, EUCLIDEAN, RANDOM_DISTANCE, N_GRAM, EMPTY, ADAPTIVE_RANDOM_DISTANCE, STATIC_DISTANCE, RANDOM_TEMPLATE
	}

	public static FrameSelectionStrategy FRAME_SELECTION_STRATEGY = FrameSelectionStrategy.STATIC_DISTANCE;

	public enum InstrumentationApproach {
		STATIC, ARRAY, NONE
	}

	@Parameter(key = "instrumentation_approach", description = "Determines the approach to be used during class instrumentation. A static approach inserts calls to ClassAnalyzer.lineFound etc to track which lines/branches have been covered. Using an array stores all line/branch executions in an array of integers and has a method to get all the values")
	public static InstrumentationApproach INSTRUMENTATION_APPROACH = InstrumentationApproach.ARRAY;

	@Parameter(key = "instrument_lines", description = "Switch on line instrumentation")
	public static boolean INSTRUMENT_LINES = true;

	@Parameter(key = "instrument_branches", description = "Switch on branch instrumentation")
	public static boolean INSTRUMENT_BRANCHES = true;

	public static int MAX_LOADED_FRAMES = 10;

	public static long RUNTIME = 600000;

	public static long BACKGROUND_FRAMES = 20;

	public static String BRANCHES_TO_COVER = null;

	public static boolean SHOW_GUI = false;

	public static boolean SHOW_HAND = true;

	public static boolean DELAY_LIBRARY = false;

	public static String[] INSTRUMENTED_PACKAGES = null;

	public static String NGRAM_TYPE = "25-2";

	public static String[] GESTURE_FILES = {"tom-gorogoa-300000ms",
//			"processed/bigcircle-25-2",
//			"processed/circle-25-2",
//			"processed/keytap-25-2",
//			"processed/punch-25-2",
//			"processed/swipe-down-25-2",
//			"processed/swipe-left-25-2",
//			"processed/swipe-right-25-2",
//			"processed/swipe-up-25-2",
//			"processed/screen-tap-25-2"
	};

	public static long SAMPLE_RATE = 300;

	public static int SEQUENCE_LENGTH = 10;

	public static long TIMEOUT = 0;

	public static boolean SEQUENCE = true;

	public static boolean REPLACE_FINGERS_METHOD = true;

	@Parameter(key = "write_class", description = "flag to determine whether or not to write classes. If set to true, the InstrumentingClassLoader will write out all classes to the value of BYTECODE_DIR")
	public static boolean WRITE_CLASS = false;

	@Parameter(key = "bytecode_dir", description = "directory in which to store bytecode if the WRITE_CLASS property is set to true")
	public static String BYTECODE_DIR = System.getProperty("user.home") + "/.bytecode/";
	
	@Parameter(key = "log_dir", description = "directory in which to store log files (application.log, timings.log)")
	public static String LOG_DIR = System.getProperty("user.home")+"/.logs/";

	@Parameter(key = "log_tinigs", description = "set whether application timings should be written to a log file")
	public static boolean LOG = true;
	
	private Map<String, Field> parameterMap = new HashMap<String, Field>();

	private void reflectMap() {
		for (Field field : Arrays.asList(Properties.class.getFields())) {
			if (field.isAnnotationPresent(Parameter.class)) {
				parameterMap.put(field.getAnnotation(Parameter.class).key(), field);
			}
		}
	}

	@Override
	public boolean hasParameter(String name) {
		return parameterMap.keySet().contains(name);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setParameter(String key, String value) throws IllegalArgumentException, IllegalAccessException {
		if (!parameterMap.containsKey(key)) {
			throw new IllegalArgumentException(key + " was not found in the Properties class");
		}
		Field f = parameterMap.get(key);
		Class<?> cl = f.getType();
		if (cl.isAssignableFrom(Number.class) || cl.isPrimitive()) {
			if (cl.equals(Long.class) || cl.equals(long.class)) {
				Long l = Long.parseLong(value);
				f.setLong(null, l);
			} else if (cl.equals(Double.class) || cl.equals(double.class)) {
				Double d = Double.parseDouble(value);
				f.setDouble(null, d);
			} else if (cl.equals(Float.class) || cl.equals(float.class)) {
				Float fl = Float.parseFloat(value);
				f.setFloat(null, fl);
			} else if (cl.equals(Integer.class) || cl.equals(int.class)) {
				Integer in = Integer.parseInt(value);
				f.setInt(null, in);
			} else if (cl.equals(Boolean.class) || cl.equals(boolean.class)) {
				Boolean bl = Boolean.parseBoolean(value);
				f.setBoolean(null, bl);
			}
		} else if (cl.isAssignableFrom(String.class)) {
			f.set(null, value);
		}
		if (f.getType().isEnum()) {
			f.set(null, Enum.valueOf((Class<Enum>) f.getType(), value.toUpperCase()));
		}
	}

	@Override
	public Set<String> getParameterNames() {
		return parameterMap.keySet();
	}

	private static Properties instance;

	public static Properties instance() {
		if (instance == null) {
			instance = new Properties();
		}
		return instance;
	}
}
