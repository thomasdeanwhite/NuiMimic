package com.sheffield.leapmotion.controller;

import com.leapmotion.leap.*;
import com.sheffield.instrumenter.Properties;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.AppStatus;
import com.sheffield.leapmotion.controller.gestures.GestureHandler;
import com.sheffield.leapmotion.listeners.FrameSwitchListener;
import com.sheffield.leapmotion.sampler.SamplerApp;

import java.util.ArrayList;

public class SeededController extends Controller implements FrameSwitchListener {

	public static SeededController CONTROLLER;

	public static SeededController getSeededController() {
		if (CONTROLLER == null) {
			CONTROLLER = new SeededController();
			App.startTesting();
			CONTROLLER.setup();
			//CONTROLLER.setGestureHandler(new NGramGestureHandler(Properties.GESTURE_FILES[0]));
		}
		return CONTROLLER;
	}

	public static Controller getController() {
		return getSeededController();
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
        App.getApp().setStatus(AppStatus.TESTING);
		CONTROLLER = this;

	}

	private void setup(){
		App.out.println("- Controller Initialized.");
		listeners = new ArrayList<Listener>();
		frameHandler = new FrameHandler();
		frameHandler.addFrameSwitchListener(this);
		CONTROLLER = this;
		setPolicyFlags(Controller.PolicyFlag.POLICY_BACKGROUND_FRAMES);
		if (App.getApp() == null){
			App.startTesting();
		}
		// addListener(com.sheffield.leapmotion_sampler.App.getApp());
		CONTROLLER = this;
	}

	@Override
	public void onFrameSwitch(Frame lastFrame, Frame nextFrame) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).onFrame(this);
		}
	}

	public void tick() {
		if (frameHandler != null){
			//if (frameRequested) {
				frameHandler.loadNewFrame();
			//}
		}
		frameRequested = false;
	}

	@Override
	public boolean addListener(Listener arg0) {
		App.getApp().setStatus(AppStatus.TESTING);
		if (Properties.RECORDING) {
			return super.addListener(arg0);
		}
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

	@Override
	public Frame frame(int arg0) {
		App.getApp().setStatus(AppStatus.TESTING);
		frameRequested = true;
		if (Properties.RECORDING) {
			final Frame f = super.frame(arg0);
			new Thread(new Runnable() {

				public void run() {
					SamplerApp.getApp().frame(f);
				}

			}).start();
			return f;
		}

		if (App.APP != null && App.APP.status() == AppStatus.FINISHED) {
			throw new IllegalArgumentException("Runtime Finished!");
		}
		Frame f = frameHandler.getFrame(arg0);
		return f;
	}

	@Override
	public boolean removeListener(Listener arg0) {
		App.getApp().setStatus(AppStatus.TESTING);

		if (Properties.RECORDING) {
			return super.removeListener(arg0);
		}
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
		//super.enableGesture(type);
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
}
