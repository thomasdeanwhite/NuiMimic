package com.sheffield.leapmotion.display;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Listener;

public class LeapmotionDisplay extends Listener {

	private static LeapmotionDisplay APP;
	private static DisplayWindow display = new DisplayWindow();

	public static LeapmotionDisplay getApp() {
		if (APP == null) {
			APP = new LeapmotionDisplay();
		}
		return APP;
	}

	public static void main(String[] args) {
		LeapmotionDisplay app = LeapmotionDisplay.getApp();
		Controller controller = new Controller();
		controller.addListener(app);
		while (display.isVisible()) {
			try {
				app.tick();
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (controller != null) {
			controller.removeListener(app);
		}

		System.exit(0);

	}

	@Override
	public void onFrame(Controller arg0) {
		Frame frame = arg0.frame();
		if (frame.isValid()) {
			frame(frame);
			// super.onFrame(arg0);
		}
	}

	public void frame(Frame frame) {
		display.setFrame(frame);
	}

	public void tick() {

	}

	public void appFinished() {
	}
}
