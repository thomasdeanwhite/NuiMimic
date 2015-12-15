package com.sheffield.leapmotion.sampler;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Listener;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.display.DisplayWindow;
import com.sheffield.leapmotion.mocks.HandFactory;
import com.sheffield.instrumenter.Properties;

/**
 * Hello world!
 *
 */
public class SamplerApp extends Listener {

	private static SamplerApp APP;

	public static boolean USE_CONTROLLER = true;
	public static PrintStream out = System.out;

	public static boolean SHOW_GUI = true;

	private AppStatus status;
	private long startTime;
	private long lastSampleTime;
	private long timeBetweenSamples;
	private DisplayWindow display;
	private ArrayList<Frame> framesInSequence;
	private boolean startedRecording = false;
	private int frameInSequence;
	private File currentSequence;
	private File currentHands;
	private File currentPosition;
	private File currentRotation;

	public AppStatus status() {
		return status;
	}

	public void setStatus(AppStatus status) {
		this.status = status;
	}

	private SamplerApp() {
		super();
		status = AppStatus.DISCONNECTED;
		startTime = System.currentTimeMillis();
		lastSampleTime = startTime;
		timeBetweenSamples = 1000 / Properties.SAMPLE_RATE;
		if (SHOW_GUI) {
			display = new DisplayWindow();
		}
		if (Properties.SEQUENCE) {
			framesInSequence = new ArrayList<Frame>();
		}
		frameInSequence = Properties.SEQUENCE_LENGTH;
	}

	public static SamplerApp getApp() {
		if (APP == null) {
			APP = new SamplerApp();
		}
		return APP;
	}

	public static void main(String[] args) {
		SamplerApp app = SamplerApp.getApp();
		app.setStatus(AppStatus.CONNECTING);
		Controller controller = null;
		if (USE_CONTROLLER) {
			controller = new Controller();
			controller.addListener(app);
		}

		while (app.status() != AppStatus.FINISHED) {
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
		if (Properties.SEQUENCE) {
			app.writeFramesInSequence();
		}

		SamplerApp.out.println("Finished data collection");
		System.exit(0);

	}

	@Override
	public void onConnect(Controller controller) {
		status = AppStatus.CONNECTED;

		// Policy hack so app always receives data
		com.leapmotion.leap.LeapJNI.Controller_setPolicy(Controller.getCPtr(controller), controller, 1);
		com.leapmotion.leap.LeapJNI.Controller_setPolicy(Controller.getCPtr(controller), controller, 1 << 15);
		SamplerApp.out.println("- Connected to LeapMotion");
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
		if (!startedRecording) {
			boolean validHand = false;

			for (Hand h : frame.hands()) {
				if (h.isValid()) {
					validHand = true;
				}
			}

			if (validHand) {
				startedRecording = true;
			}
			if (SHOW_GUI) {
				display.setFrame(frame);
			}
		} else {

			if (Properties.SEQUENCE) {
				framesInSequence.add(frame);
			} else {
				long time = System.currentTimeMillis();
				if (lastSampleTime < time) {

					for (Hand h : frame.hands()) {
						if (h.isValid() && h.isRight()) {

							if (SHOW_GUI) {
								frame = HandFactory.injectHandIntoFrame(frame,
										HandFactory.createHand(HandFactory.handToString("hand", h), frame));
								display.setFrame(frame);
							}

							String uniqueId = System.currentTimeMillis() + "@"
									+ ManagementFactory.getRuntimeMXBean().getName();
							String frameAsString = HandFactory.handToString(uniqueId, h);
							try {
								if (currentHands == null) {
									currentHands = new File(FileHandler.generateFile() + ".handdata");
									currentHands.getParentFile().mkdirs();
									currentHands.createNewFile();
								}
								FileHandler.appendToFile(currentHands, frameAsString + "\n");
								if (currentSequence == null) {
									frameInSequence = 0;
									currentSequence = new File(FileHandler.generateFile() + ".seqdata");
									currentSequence.getParentFile().mkdirs();
									currentSequence.createNewFile();
								}
								FileHandler.appendToFile(currentSequence, uniqueId + ",");

								if (currentPosition == null) {
									currentPosition = new File(FileHandler.generateFile() + ".positiondata");
									currentPosition.getParentFile().mkdirs();
									currentPosition.createNewFile();
								}
								String position = uniqueId + "," + h.palmPosition().getX() + ","
										+ h.palmPosition().getY() + "," + h.palmPosition().getZ() + "\n";
								FileHandler.appendToFile(currentPosition, position);

								if (currentRotation == null) {
									currentRotation = new File(FileHandler.generateFile() + ".rotationdata");
									currentRotation.getParentFile().mkdirs();
									currentRotation.createNewFile();
									// TODO: FIX ROTATION
								}
								String rotation = uniqueId + ",";
								Vector[] vectors = new Vector[3];
								vectors[0] = h.basis().getXBasis();
								vectors[1] = h.basis().getYBasis();
								vectors[2] = h.basis().getZBasis();
								for (Vector v : vectors) {
									rotation += v.getX() + "," + v.getY() + "," + v.getZ() + ",";
								}
								rotation.substring(0, rotation.length() - 1);
								rotation += "\n";
								FileHandler.appendToFile(currentRotation, rotation);
								frameInSequence++;
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							lastSampleTime = time + timeBetweenSamples;

						}
					}
				}
			}
		}
	}

	public void tick() {
		if (SHOW_GUI) {
			if (!display.isVisible()) {
				appFinished();
			}
		}
	}

	public void appFinished() {
		status = AppStatus.FINISHED;
	}

	public void writeFramesInSequence() {
		if (Properties.SEQUENCE) {
			Properties.DIRECTORY += "/sequences";
			File file = FileHandler.generateFile();
			try {
				SamplerApp.out.println("Writing to file: " + file.getAbsolutePath());
				FileHandler.writeToFile(file, Serializer.sequenceToJson(framesInSequence));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				SamplerApp.out.println("Writing failed!");
				e.printStackTrace();
			}

		}

	}
}
