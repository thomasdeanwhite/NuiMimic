package com.sheffield.leapmotion.controller;

import com.leapmotion.leap.BugReport;
import com.leapmotion.leap.Config;
import com.leapmotion.leap.Controller;
import com.leapmotion.leap.DeviceList;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.ImageList;
import com.leapmotion.leap.Listener;
import com.leapmotion.leap.ScreenList;
import com.leapmotion.leap.TrackedQuad;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.util.AppStatus;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.util.Tickable;
import com.sheffield.leapmotion.frame.generators.gestures.GestureHandler;
import com.sheffield.leapmotion.controller.listeners.FrameSwitchListener;
import com.sheffield.output.Csv;

import java.util.ArrayList;

public class SeededController extends Controller implements FrameSwitchListener, Tickable {

	public static SeededController CONTROLLER;
	private static boolean initializing = false;
	private static boolean calledAsSuperclass = true;

	public static boolean initialized(){
		return CONTROLLER != null;
	}

	public String status(){

		String status = App.getApp().getFps() + " fps ";

		if (frameHandler == null){
			status += "Manual Testing";
		} else {
			status += frameHandler.status();
		}
		return status;
	}

	private Listener leapmotionListener = new Listener(){
		@Override
		public void onConnect(Controller controller) {

		}

		@Override
		public void onFrame(Controller controller) {
			for (int i = 0; i < listeners.size(); i++) {
				listeners.get(i).onFrame(CONTROLLER);
			}
		}
	};

	public boolean hasNextFrame(){
		return frameHandler.hasNextFrame();
	}

	public static void resetSeededController(){
		CONTROLLER = null;
	}

	public static SeededController getSeededController() {
		return getSeededController(true);
	}

	public static SeededController getSeededController(boolean setupForTesting) {
		while (initializing){
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace(App.out);
			}
		}
		if (CONTROLLER == null) {
			initializing = true;
			//App.startTesting();
			CONTROLLER = new SeededController();
			App.getApp().setup(setupForTesting);
			CONTROLLER.setup();
			initializing = false;
		}
		return CONTROLLER;
	}

	public Csv getCsv(){
		return frameHandler.getCsv();
	}

	public static Controller getController() {
		disableSuperclass();
		return getSeededController();
	}

	public static void disableSuperclass(){
		calledAsSuperclass = false;
	}

	public static Frame newFrame() {
		if (CONTROLLER == null) {
			return Frame.invalid();
		}
		return CONTROLLER.createFrame();
	}

	private static ArrayList<Listener> listeners;
	private static FrameHandler frameHandler;
	private boolean frameRequested = true;

	public SeededController() {
		super();
		enableGesture(Gesture.Type.TYPE_SWIPE);
		enableGesture(Gesture.Type.TYPE_KEY_TAP);
		enableGesture(Gesture.Type.TYPE_SCREEN_TAP);
		enableGesture(Gesture.Type.TYPE_CIRCLE);
        App.getApp().setStatus(AppStatus.TESTING);
		CONTROLLER = this;

        if (calledAsSuperclass){
            initializing = true;
            App.setTesting();

            //App.startTesting();
            App.getApp().setup(true);
            CONTROLLER.setup();
            initializing = false;
        }

	}


	private boolean setup = false;

	private void setup(){
		if (setup){
			return;
		}

		setup = true;
		listeners = new ArrayList<Listener>();
		CONTROLLER = this;

		if (Properties.LEAVE_LEAPMOTION_ALONE){
			super.addListener(leapmotionListener);
			// Policy hack so app always receives data
			com.leapmotion.leap.LeapJNI.Controller_setPolicy(Controller.getCPtr(this), this, 1);
			com.leapmotion.leap.LeapJNI.Controller_setPolicy(Controller.getCPtr(this), this, 1 << 15);

			//enable gestures for gesture model to work
			enableGesture(Gesture.Type.TYPE_SWIPE);
			enableGesture(Gesture.Type.TYPE_CIRCLE);
			enableGesture(Gesture.Type.TYPE_SCREEN_TAP);
			enableGesture(Gesture.Type.TYPE_KEY_TAP);

			App.out.println("- Only recording testing information!");
		} else {
			frameHandler = new FrameHandler();
			frameHandler.init(this);
			frameHandler.addFrameSwitchListener(this);
		}

		setPolicyFlags(Controller.PolicyFlag.POLICY_BACKGROUND_FRAMES);
		if (App.getApp() == null){
			App.startTesting();
		}
		//App.out.println("\r- Controller Initialized.");
		// addListener(com.sheffield.leapmotion_sampler.App.getApp());
	}

	@Override
	public void onFrameSwitch(Frame lastFrame, Frame nextFrame) {
		for (int i = 0; i < listeners.size(); i++) {
			final Listener l = listeners.get(i);
			final Controller controller = this;
			l.onFrame(controller);
		}
	}

	private long lastUpdate = 0;
	@Override
	public void tick(long time) {
		lastUpdate = time;
		if (frameHandler != null){
			//if (frameRequested) {
			try {
				frameHandler.tick(time);
				frameHandler.loadNewFrame(time);
			} catch (Exception e){
				e.printStackTrace(App.out);
			}
			//}
		}
		frameRequested = false;
	}

	public long lastTick(){
		return lastUpdate;
	}

	@Override
	public boolean addListener(Listener arg0) {
		App.getApp().setStatus(AppStatus.TESTING);

		if (App.APP != null && App.APP.status() == AppStatus.FINISHED) {
			throw new IllegalArgumentException("Runtime Finished!");
		}
		if (!listeners.contains(arg0)) {
			listeners.add(arg0);
			arg0.onConnect(this);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Frame frame() {
		App.getApp().setStatus(AppStatus.TESTING);
		if (App.APP != null && App.APP.status() == AppStatus.FINISHED) {
			throw new IllegalArgumentException("Runtime Finished!");
		}
		Frame f = frame(0);
		//ClassAnalyzer.newFrame();
		if (App.DISPLAY_WINDOW != null) {
			App.DISPLAY_WINDOW.setFrame(f);
		}
		return f;
	}

	public float getProgress(){
		return frameHandler.getProgress();
	}

	@Override
	public Frame frame(int arg0) {
		App.getApp().setStatus(AppStatus.TESTING);

		if (Properties.LEAVE_LEAPMOTION_ALONE){
			return super.frame();
		}

		frameRequested = true;

		if (App.APP != null && App.APP.status() == AppStatus.FINISHED) {
			throw new IllegalArgumentException("Runtime Finished!");
		}
		Frame f = frameHandler.getFrame(arg0);
		return f;
	}

	@Override
	public boolean removeListener(Listener arg0) {
		App.getApp().setStatus(AppStatus.TESTING);

		if (App.APP != null && App.APP.status() == AppStatus.FINISHED) {
			throw new IllegalArgumentException("Runtime Finished!");
		}
		if (listeners.contains(arg0)) {
			listeners.remove(arg0);
			return true;
		} else {
			return false;
		}
	}

	public void setGestureHandler(GestureHandler gh) {
		frameHandler.setGestureHandler(gh);
	}

	private Frame createFrame() {
		return super.frame();
	}

	public SeededController(long l, boolean b) {
		super(l, b);
	}

	@Override
	protected void finalize() {
		//super.finalize();
	}

	@Override
	public synchronized void delete() {
		//super.delete();
	}

	public SeededController(Listener listener) {
		super();
		addListener(listener);
	}

	@Override
	public boolean isConnected() {
		return true;
	}

	@Override
	public boolean isServiceConnected() {
		return true;
	}

	@Override
	public boolean hasFocus() {
		return true;
	}

	@Override
	public PolicyFlag policyFlags() {
		//notSupported();
		return super.policyFlags();
	}

	@Override
	public void setPolicyFlags(PolicyFlag policyFlag) {
		//notSupported();
		super.setPolicyFlags(policyFlag);
	}

	@Override
	public void setPolicy(PolicyFlag policyFlag) {
		//notSupported();
		super.setPolicy(policyFlag);
	}

	@Override
	public void clearPolicy(PolicyFlag policyFlag) {
		//notSupported();
		super.clearPolicy(policyFlag);
	}

	@Override
	public boolean isPolicySet(PolicyFlag policyFlag) {
		//notSupported();
		return super.isPolicySet(policyFlag);
	}

	@Override
	public ImageList images() {
		notSupported();
		return super.images();
	}

	@Override
	public Config config() {
		notSupported();
		return super.config();
	}

	@Override
	public DeviceList devices() {
		return super.devices();
	}

	@Override
	public ScreenList locatedScreens() {
		notSupported();
		return super.locatedScreens();
	}

	@Override
	public BugReport bugReport() {
		notSupported();
		return super.bugReport();
	}

	@Override
	public void enableGesture(Gesture.Type type, boolean b) {
		//super.enableGesture(type, b);
	}

	@Override
	public void enableGesture(Gesture.Type type) {
		super.enableGesture(type);
	}

	@Override
	public boolean isGestureEnabled(Gesture.Type type) {
		//return super.isGestureEnabled(type);
		return true;
	}

	@Override
	public TrackedQuad trackedQuad() {
		notSupported();
		return TrackedQuad.invalid();
	}

	@Override
	public long now() {
		return System.currentTimeMillis();
	}

	private void notSupported(){
		throw new IllegalStateException("Feature not supported!");
	}

	public void cleanUp(){
		frameHandler.cleanUp();

//		if (App.DISPLAY_WINDOW != null){
//			App.DISPLAY_WINDOW.setVisible(false);
//			App.DISPLAY_WINDOW = null;
//		}
	}

	public boolean allowProcessing(){
		return frameHandler.allowProcessing() && !Properties
				.PROCESS_SCREENSHOTS;
	}

	public String getTechnique(){
		return frameHandler.getTechnique();
	}
}
