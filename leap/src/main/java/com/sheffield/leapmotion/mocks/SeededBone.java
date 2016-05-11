package com.sheffield.leapmotion.mocks;

import com.leapmotion.leap.Bone;
import com.leapmotion.leap.Matrix;
import com.leapmotion.leap.Vector;

import java.io.Serializable;

public class SeededBone extends Bone implements Serializable {

	protected Matrix basis;
	protected Vector center;
	protected Vector direction;
	protected float length;
	protected Vector nextJoint;
	protected Vector prevJoint;
	protected Bone.Type type;
	protected float width;

	public SeededBone() {
		basis = Matrix.identity();
		center = Vector.zero();
		direction = Vector.zero();
		length = 0;
		nextJoint = Vector.zero();
		prevJoint = Vector.zero();
		type = Bone.Type.TYPE_PROXIMAL;
		width = 0;
	}

	@Override
	public Matrix basis() {
		// TODO Auto-generated method stub
		return basis;
	}

	@Override
	public Vector center() {
		// TODO Auto-generated method stub
		return basis.transformPoint(center);
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
	public boolean equals(Bone arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void finalize() {
		// TODO Auto-generated method stub
		// super.finalize();

	}

	public void normalize() {
		direction = prevJoint().minus(nextJoint()).normalized();
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
	public Vector nextJoint() {
		// TODO Auto-generated method stub
		return basis.transformPoint(nextJoint);
	}

	@Override
	public Vector prevJoint() {
		// TODO Auto-generated method stub
		return basis.transformPoint(prevJoint);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Seeded Bone " + type + " with joints " + prevJoint.toString() + " " + nextJoint.toString() + ".";
	}

	@Override
	public Type type() {
		// TODO Auto-generated method stub
		return type;
	}

	@Override
	public float width() {
		// TODO Auto-generated method stub
		return width;
	}

}
