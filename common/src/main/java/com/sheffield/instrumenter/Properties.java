package com.sheffield.instrumenter;

import java.lang.reflect.Method;

public class Properties {
	public static String DIRECTORY = "H:\\data\\leapmotion";

	public static String PLAYBACK_FILE = null;

	// Number of times to change frames per second
	public static long SWITCH_RATE = 100;

	public static int SWITCH_TIME = 2000;

	public static boolean SMOOTH_ANIMATION = true;

	public static String SUT = null;

	public static String LIBRARY = "";

	public static String CLASS_PATH = "";

	public static String LM_AGENT_JAR = "lm-agent.jar";

	public static Method MAIN_METHOD = null;

	public static boolean RECORDING = false;

	public static String[] EXILED_CLASSES;

	public enum FrameSelectionStrategy {
		RANDOM, EUCLIDEAN, RANDOM_DISTANCE, N_GRAM, EMPTY, ADAPTIVE_RANDOM_DISTANCE, STATIC_DISTANCE
	}

	public static FrameSelectionStrategy FRAME_SELECTION_STRATEGY = FrameSelectionStrategy.STATIC_DISTANCE;

	public static int MAX_LOADED_FRAMES = 10;

	public static long RUNTIME = 120000;

	public static long BACKGROUND_FRAMES = 20;

	public static String BRANCHES_TO_COVER = null;

	public static boolean SHOW_GUI = false;

	public static boolean SHOW_HAND = true;

	public static boolean DELAY_LIBRARY = false;

	public static String[] INSTRUMENTED_PACKAGES = null;

	public static String NGRAM_TYPE = "50-2";

	public static String[] GESTURE_FILES = {"processed/asl-" + NGRAM_TYPE,
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

	public static boolean SEQUENCE = false;

}
