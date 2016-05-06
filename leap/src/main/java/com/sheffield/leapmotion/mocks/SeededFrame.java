package com.sheffield.leapmotion.mocks;

import com.leapmotion.leap.*;
import com.sheffield.instrumenter.Properties;

public class SeededFrame extends Frame {

	protected Frame frame;
	protected GestureList gestureList;
	protected boolean gestureSet = false;
	protected SeededHandList handList;
	protected SeededPointableList pointables;
	protected SeededFingerList fingerList;
	protected long id;
	// TODO: SeededInteractionBox
	protected InteractionBox interactionBox = InteractionBox.invalid();

	public SeededFrame(Frame frame) {
		if (frame instanceof SeededFrame) {
			SeededFrame sf = (SeededFrame) frame;
			this.frame = sf.frame;
			gestureSet = sf.gestureSet;
			handList = sf.handList;
			fingerList = sf.fingerList;
			pointables = sf.pointables;
		} else {
			this.frame = frame;
			gestureSet = false;
			handList = new SeededHandList();
			fingerList = new SeededFingerList();
			pointables = new SeededPointableList();
			for (Hand h : frame.hands()) {
				handList.addHand(h);

				for (Finger f : h.fingers()) {
					fingerList.addFinger(f);
					pointables.addPointable(f);
				}
			}
			for (Pointable p : frame.pointables()) {
				if (!pointables.contains(p)) {
					pointables.addPointable(p);
				}
			}
		}
	}

	public SeededFrame(Frame frame, GestureList gestures) {
		this(frame);
		if (gestures != null) {
			setGestures(gestures);
		}
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setHandList(SeededHandList hl) {
		handList.clear();
		fingerList.clear();
		pointables.clear();
		for (Hand h : hl) {
			handList.addHand(h);

			for (Finger f : h.fingers()) {
				fingerList.addFinger(f);
				pointables.addPointable(f);
			}
		}
	}

	public void setGestures(GestureList gl) {
		gestureList = gl;
		gestureSet = true;
	}

	public boolean isGestureSet() {
		return gestureSet;
	}

	@Override
	public Gesture gesture(int arg0) {
		// TODO Auto-generated method stub
		return gestureList.get(arg0);
	}

	@Override
	public GestureList gestures() {
		// TODO Auto-generated method stub
		// App.out.println("GESTURES: " + gestureList.count());
		return gestureList;
	}

	@Override
	public GestureList gestures(Frame arg0) {
		// TODO Auto-generated method stub
		return arg0.gestures();
	}

	/**
	 * Delegate methods onto the frame object now:
	 */

	@Override
	public float currentFramesPerSecond() {
		return Properties.SAMPLE_RATE;
	}

	@Override
	public synchronized void delete() {
		// frame.delete();
	}

	@Override
	public void deserialize(byte[] arg0, int arg1) {
		// TODO Auto-generated method stub
		frame.deserialize(arg0, arg1);
	}

	@Override
	public void deserialize(byte[] arg0) {
		// TODO Auto-generated method stub
		frame.deserialize(arg0);
	}

	@Override
	public boolean equals(Frame arg0) {
		// TODO Auto-generated method stub
		if (arg0 instanceof SeededFrame) {
			return arg0.id() == id;
		}
		return arg0.equals(frame);
	}

	@Override
	public Finger finger(int arg0) {
		// TODO Auto-generated method stub
		return fingerList.get(arg0);
	}

	@Override
	public FingerList fingers() {
		// TODO Auto-generated method stub
		return fingerList;
	}

	@Override
	public Hand hand(int arg0) {
		// TODO Auto-generated method stub
		return handList.get(arg0);
	}

	@Override
	public HandList hands() {
		// TODO Auto-generated method stub
		return handList;
	}

	@Override
	public long id() {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public ImageList images() {
		// TODO Auto-generated method stub
		return frame.images();
	}

	@Override
	public InteractionBox interactionBox() {
		// TODO Auto-generated method stub

		return interactionBox;
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Pointable pointable(int arg0) {

		 Pointable p = pointables.get(arg0);
		return p == null ? Pointable.invalid() : p;
	}

	@Override
	public PointableList pointables() {
		// TODO Auto-generated method stub
		return pointables;// frame.pointables();
	}

	@Override
	public float rotationAngle(Frame arg0, Vector arg1) {
		// TODO Auto-generated method stub
		return frame.rotationAngle(arg0, arg1);
	}

	@Override
	public float rotationAngle(Frame arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Vector rotationAxis(Frame arg0) {
		// TODO Auto-generated method stub
		return Vector.zero();
	}

	@Override
	public Matrix rotationMatrix(Frame arg0) {
		// TODO Auto-generated method stub
		return Matrix.identity();
	}

	@Override
	public float rotationProbability(Frame arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float scaleFactor(Frame arg0) {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public float scaleProbability(Frame arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public byte[] serialize() {
		// TODO Auto-generated method stub
		return frame.serialize();
	}

	@Override
	public void serialize(byte[] arg0) {
		// TODO Auto-generated method stub
		frame.serialize(arg0);
	}

	@Override
	public int serializeLength() {
		// TODO Auto-generated method stub
		return frame.serializeLength();
	}

	@Override
	public long timestamp() {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "SeededFrame:" + frame.toString();
	}

	@Override
	public Tool tool(int arg0) {
		// TODO Auto-generated method stub
		return Tool.invalid();// frame.tool(arg0);
	}

	@Override
	public ToolList tools() {
		// TODO Auto-generated method stub
		return new ToolList();
	}

	@Override
	public TrackedQuad trackedQuad() {
		throw new IllegalStateException("Experimental/Unsupported API Feature");
	}

	@Override
	public Vector translation(Frame arg0) {
		return Vector.zero();
	}

	@Override
	public float translationProbability(Frame arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

}
