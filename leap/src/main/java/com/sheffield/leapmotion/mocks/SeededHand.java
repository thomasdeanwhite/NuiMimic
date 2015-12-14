package com.sheffield.leapmotion.mocks;

import com.leapmotion.leap.*;

import java.io.Serializable;

public class SeededHand extends Hand implements Serializable {

	protected FingerList fingerList;
	protected Matrix basis;
	protected Vector direction;
	protected Frame frame;
	protected Vector palmNormal;
	protected Vector palmPosition;
	protected Vector palmVelocity;
	protected float palmWidth;
	protected float timeVisible;
	protected boolean isLeft;

	protected float pinchStrength;
	protected float grabStrength;

	protected PointableList pointables;
	protected ToolList tools;

	protected int id;
	protected String uniqueId;

	public Hand fadeHand(SeededHand hand, float modifier) {
		SeededHand h = new SeededHand();
		h.basis = basis;
		h.direction = direction;
		h.frame = frame;
		h.palmNormal = palmNormal;
		h.palmPosition = palmPosition;
		h.palmVelocity = palmVelocity;
		h.palmWidth = palmWidth;
		h.timeVisible = timeVisible;
		h.isLeft = isLeft;
		h.id = id;
		h.uniqueId = uniqueId;
		SeededFingerList sfl = new SeededFingerList();
		for (Finger f : fingerList) {
			SeededFinger sf = new SeededFinger();
			Finger f2 = null;
			for (Finger f2enum : hand.fingers()) {
				if (f2enum.type() == f.type()) {
					f2 = f2enum;
					break;
				}
			}
			for (Bone.Type bt : Bone.Type.values()) {
				SeededBone sb = new SeededBone();
				Bone rb = f.bone(bt);
				Bone rb2 = f2.bone(bt);
				if (!(rb instanceof SeededBone && rb2 instanceof SeededBone)) {
					continue;
				}
				SeededBone b = (SeededBone) rb;
				SeededBone b2 = (SeededBone) rb2;
				sb.type = bt;
				sb.basis = basis;
				sb.center = b.center.plus(b2.center.minus(b.center).times(modifier));
				sb.length = b.length();
				sb.nextJoint = b.nextJoint.plus(b2.nextJoint.minus(b.nextJoint).times(modifier));
				sb.prevJoint = b.prevJoint.plus(b2.prevJoint.minus(b.prevJoint).times(modifier));
				sb.width = b.width();
				sf.bones.put(bt, sb);
			}
			sf.normalize();
			sf.tipVelocity = f2.tipPosition().minus(sf.tipPosition());
			sfl.addFinger(sf);
		}
		h.fingerList = sfl;
		return h;
	}

	public SeededHand() {
		fingerList = new SeededFingerList();
		basis = Matrix.identity();
		direction = Vector.zAxis();
		palmNormal = Vector.yAxis().times(-1);
		palmPosition = Vector.zero();
		palmVelocity = Vector.zero();
		pointables = new PointableList();
		tools = new ToolList();
	}

	public SeededHand(FingerList fl) {
		this();
		fingerList = fl;
	}

	public SeededHand(Hand hand) {
		this();
		fingerList.append(hand.fingers());
		isLeft = hand.isLeft();
		palmWidth = hand.palmWidth();
	}

	private SeededHand(String s) {

	}

	public void setBasis(Vector x, Vector y, Vector z) {
		basis.setXBasis(x);
		basis.setYBasis(y);
		basis.setZBasis(z);
	}

	public void setOrigin(Vector origin) {
		basis.setOrigin(origin);
	}

	public void setRotation(Vector axis, float rotation) {
		basis.setRotation(axis, rotation);
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setFingerList(FingerList fl) {
		fingerList = fl;
	}

	@Override
	public Arm arm() {
		// TODO Auto-generated method stub
		return Arm.invalid();
	}

	@Override
	public Matrix basis() {
		// TODO Auto-generated method stub
		return basis;
	}

	@Override
	public float confidence() {
		// TODO Auto-generated method stub
		return 1f;
	}

	@Override
	public synchronized void delete() {
		// TODO Auto-generated method stub
		// super.delete();
	}

	@Override
	public Vector direction() {
		// TODO Auto-generated method stub
		return basis.transformDirection(direction);
	}

	@Override
	public boolean equals(Hand arg0) {
		if (arg0 instanceof SeededHand) {
			return uniqueId.equals(((SeededHand) arg0).uniqueId);
		}
		return false;
	}

	@Override
	protected void finalize() {
		// TODO Auto-generated method stub
		// super.finalize();
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
	public Frame frame() {
		// TODO Auto-generated method stub
		return frame;
	}

	@Override
	public float grabStrength() {
		// TODO Auto-generated method stub
		return grabStrength;
	}

	@Override
	public int id() {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public boolean isLeft() {
		// TODO Auto-generated method stub
		return isLeft;
	}

	@Override
	public boolean isRight() {
		// TODO Auto-generated method stub
		return !isLeft;
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Vector palmNormal() {
		// TODO Auto-generated method stub
		return basis.transformDirection(palmNormal);
	}

	@Override
	public Vector palmPosition() {
		// TODO Auto-generated method stub
		return basis.transformPoint(palmPosition);
	}

	@Override
	public Vector palmVelocity() {
		// TODO Auto-generated method stub
		return basis.transformDirection(palmVelocity);
	}

	@Override
	public float palmWidth() {
		// TODO Auto-generated method stub
		return palmWidth;
	}

	@Override
	public float pinchStrength() {
		// TODO Auto-generated method stub
		return pinchStrength;
	}

	@Override
	public Pointable pointable(int arg0) {
		// TODO Auto-generated method stub
		return Pointable.invalid();
	}

	@Override
	public PointableList pointables() {
		// TODO Auto-generated method stub
		SeededPointableList pl = new SeededPointableList();
		for (Finger f : fingerList.extended()) {
			pl.addPointable(f);
		}
		return pl;
	}

	@Override
	public float rotationAngle(Frame arg0, Vector arg1) {
		// TODO Auto-generated method stub
		return 0;
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
	public Vector sphereCenter() {
		// TODO Auto-generated method stub
		return Vector.zero();
	}

	@Override
	public float sphereRadius() {
		// TODO Auto-generated method stub
		return 1f;
	}

	@Override
	public Vector stabilizedPalmPosition() {
		// TODO Auto-generated method stub
		return palmPosition;
	}

	@Override
	public float timeVisible() {
		// TODO Auto-generated method stub
		throw new NotImplementedException();// super.timeVisible();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Seeded Hand " + uniqueId;
	}

	@Override
	public Tool tool(int arg0) {
		// TODO Auto-generated method stub
		return Tool.invalid();
	}

	@Override
	public ToolList tools() {
		// TODO Auto-generated method stub
		return tools;
	}

	@Override
	public Vector translation(Frame arg0) {
		// TODO Auto-generated method stub
		return Vector.zero();
	}

	@Override
	public float translationProbability(Frame arg0) {
		// TODO Auto-generated method stub
		return 0f;
	}

	@Override
	public Vector wristPosition() {
		// TODO Auto-generated method stub
		return basis.transformPoint(Vector.zAxis().times(-1));
	}

}
