package com.sheffield.leapmotion.controller.mocks;

import com.leapmotion.leap.Bone;
import com.leapmotion.leap.Matrix;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.frame.util.Quaternion;

import java.io.Serializable;
import java.util.Random;

public class SeededBone extends Bone implements Serializable {

	protected Matrix basis;
	protected Vector offset = Vector.zero();
	protected Vector center;
	protected Vector direction;
	protected float length;
	protected Vector nextJoint;
	protected Vector prevJoint;
	protected Bone.Type type;
	protected float width;
	protected Quaternion rotation;

	protected static Random RANDOM = new Random(0);

	public SeededBone() {
		basis = Matrix.identity();
		center = Vector.zero();
		direction = Vector.zero();
		length = 0;
		nextJoint = Vector.zero();
		prevJoint = Vector.zero();
		type = Bone.Type.TYPE_PROXIMAL;
		width = 10;
	}

	@Override
	public Matrix basis() {
		// TODO Auto-generated method stub
		return basis;
	}

	@Override
	public synchronized void delete() {
		// TODO Auto-generated method stub
		// super.delete();
	}

	@Override
	public Vector direction() {
		// TODO Auto-generated method stub
		return direction;//basis.transformDirection(direction);
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
		//basis = basis.times(Matrix.identity());
		//basis.setOrigin(offset);
		direction = prevJoint().minus(nextJoint()).normalized();

		center = prevJoint().plus(nextJoint()).divide(2f);

		length = nextJoint().minus(prevJoint).magnitude();
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

	private static final Quaternion FLIP_IN_Z = new Quaternion(0, 0, 1, 0);

	protected Vector translatePoint (Vector v){
		//basis.setOrigin(offset.minus(new Vector(0f, 0f, 50f)));
		v = offset.plus(rotatePoint(v));

		if (Properties.INVERT_Z_AXIS)
			v = FLIP_IN_Z.rotateVector(v);
		return v.plus(new Vector(RANDOM.nextFloat() * Properties.JITTER,
				RANDOM.nextFloat() * Properties.JITTER,
				RANDOM.nextFloat() * Properties.JITTER));
	}



	protected Vector rotatePoint(Vector v){
		return rotation.rotateVector(v);
	}

	@Override
	public Vector nextJoint() {
		// TODO Auto-generated method stub
		return translatePoint(nextJoint);
	}

	@Override
	public Vector prevJoint() {
		// TODO Auto-generated method stub
		return translatePoint(prevJoint);
	}

	@Override
	public Vector center() {
		// TODO Auto-generated method stub
		return center;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Seeded Bone " + type + " with joints " + prevJoint().toString() + " " + nextJoint().toString() + ".";
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
