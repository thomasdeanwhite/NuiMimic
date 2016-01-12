package com.sheffield.leapmotion.mocks;

import com.leapmotion.leap.*;

import java.util.HashMap;

public class SeededFinger extends Finger {

	public static final int MAX_Z_POSITION = 400;

	protected HashMap<Bone.Type, Bone> bones;
	protected Type type;
	protected Vector direction;
	protected Frame frame;
	protected Hand hand;
	protected int id;
	protected boolean extended;
	protected HashMap<Joint, Vector> jointPositions;
	protected float length;
	protected int timeVisible;
	protected Vector tipVelocity;
	protected Vector tipPosition;
	protected float touchDistance;
	protected Zone touchZone;
	protected float width;
	protected Matrix basis;

	private static final float EXTENDED_THRESHOLD = 0.8f;

	public SeededFinger() {
		bones = new HashMap<Bone.Type, Bone>();
		jointPositions = new HashMap<Joint, Vector>();
		basis = Matrix.identity();
		extended = true;
		touchDistance = tipPosition().getZ() / 400;
		width = 50f;
		tipPosition = Vector.forward();
		length = bone(Bone.Type.TYPE_DISTAL).nextJoint().minus(bone(Bone.Type.TYPE_METACARPAL).prevJoint()).magnitude();
		touchZone = touchDistance > 0 ? Zone.ZONE_TOUCHING
				: touchDistance > 50 / MAX_Z_POSITION ? Zone.ZONE_HOVERING : Zone.ZONE_NONE;

	}

	public void transform(Matrix basis) {
		this.basis = basis;
		for (Bone b : bones.values()) {
			((SeededBone) b).basis = basis;
		}
	}

	@Override
	public Bone bone(com.leapmotion.leap.Bone.Type arg0) {
		if (bones.containsKey(arg0)) {
			return bones.get(arg0);
		}
		return Bone.invalid();
	}

	@Override
	public synchronized void delete() {
		// TODO Auto-generated method stub
		// super.delete();
	}

	@Override
	protected void finalize() {
		// TODO Auto-generated method stub
		// super.finalize();

	}

	public void normalize() {

		for (Bone.Type bt : Bone.Type.values()) {
			Bone b = bone(bt);
			if (b instanceof SeededBone) {
				((SeededBone) b).normalize();
			}
		}
		float dot = bone(Bone.Type.TYPE_PROXIMAL).direction().dot(bone(Bone.Type.TYPE_DISTAL).direction());
		float threshold = EXTENDED_THRESHOLD;
		if (dot > EXTENDED_THRESHOLD) {
			extended = true;
		} else {
			extended = false;
		}
		tipPosition = tipPosition();
		touchDistance = tipPosition().getZ() / 200;
		length = bone(Bone.Type.TYPE_DISTAL).nextJoint().minus(bone(Bone.Type.TYPE_METACARPAL).prevJoint()).magnitude();
		touchZone = touchDistance > 0 ? Zone.ZONE_TOUCHING
				: touchDistance > 50 / MAX_Z_POSITION ? Zone.ZONE_HOVERING : Zone.ZONE_NONE;
	}

	@Override
	public Vector jointPosition(Joint arg0) {
		if (jointPositions.containsKey(arg0)) {
			return jointPositions.get(arg0);
		}
		return Vector.zero();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Seeded Finger of type: " + type + " on " + (hand.isLeft() ? "LEFT" : "RIGHT") + " hand.";
	}

	@Override
	public Type type() {
		// TODO Auto-generated method stub
		return type;
	}

	@Override
	public Vector direction() {
		// TODO Auto-generated method stub
		return direction;
	}

	@Override
	public boolean equals(Pointable arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Frame frame() {
		// TODO Auto-generated method stub
		return frame;
	}

	@Override
	public Hand hand() {
		// TODO Auto-generated method stub
		return hand;
	}

	@Override
	public int id() {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public boolean isExtended() {
		// TODO Auto-generated method stub
		return extended;
	}

	@Override
	public boolean isFinger() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isTool() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public float length() {
		// TODO Auto-generated method stub
		return length;
	}

	@Override
	public Vector stabilizedTipPosition() {
		// TODO Auto-generated method stub
		//App.out.println("tip: " + tipPosition);
		return tipPosition;
	}

	@Override
	public float timeVisible() {
		// TODO Auto-generated method stub
		return timeVisible;
	}

	@Override
	public Vector tipPosition() {
		// TODO Auto-generated method stub
		return bone(Bone.Type.TYPE_DISTAL).nextJoint();
	}

	@Override
	public Vector tipVelocity() {
		// TODO Auto-generated method stub
		return tipVelocity;
	}

	@Override
	public float touchDistance() {
		// TODO Auto-generated method stub
		return touchDistance;
	}

	@Override
	public Zone touchZone() {
		// TODO Auto-generated method stub
		return touchZone;
	}

	@Override
	public float width() {
		// TODO Auto-generated method stub
		return width;
	}

}
