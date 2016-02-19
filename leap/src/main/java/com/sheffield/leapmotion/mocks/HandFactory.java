package com.sheffield.leapmotion.mocks;

import com.leapmotion.leap.*;

import java.util.Random;

public class HandFactory {

	private static Random random = new Random();

	public static final Bone.Type[] fingerBoneTypes = { Bone.Type.TYPE_METACARPAL, Bone.Type.TYPE_PROXIMAL,
			Bone.Type.TYPE_INTERMEDIATE, Bone.Type.TYPE_DISTAL };
	public static final Bone.Type[] thumbBoneTypes = { Bone.Type.TYPE_PROXIMAL, Bone.Type.TYPE_INTERMEDIATE,
			Bone.Type.TYPE_DISTAL };
	public static final Finger.Type[] fingerTypes = { Finger.Type.TYPE_INDEX, Finger.Type.TYPE_MIDDLE,
			Finger.Type.TYPE_RING, Finger.Type.TYPE_PINKY };

	public static Frame injectHandIntoFrame(Frame currentFrame, Hand hand) {
		if (!(currentFrame instanceof SeededFrame)) {
			currentFrame = new SeededFrame(currentFrame);
		}

		SeededFrame sf = (SeededFrame) currentFrame;

		SeededHandList shl = new SeededHandList();
		shl.addHand(hand);
		if (hand instanceof SeededHand) {
			((SeededHand) hand).frame = sf;
		}
		sf.setHandList(shl);
		sf.setId(System.currentTimeMillis());
		return sf;
	}

	public static SeededHand createHand(String data, Frame frame) {
		String[] info = data.split(",");

		SeededHand hand = new SeededHand();

		// construct thumb
		Finger[] fingers = new Finger[1 + fingerTypes.length];
		int offset = 1;
		SeededFinger thumb = new SeededFinger();
		String id = info[0];
		hand.uniqueId = id;

		for (int i = 0; i < thumbBoneTypes.length; i++) {
			int index = offset;
			SeededBone b = new SeededBone();
			b.type = thumbBoneTypes[i];
			b.prevJoint = vectorFromStrings(index, info);
			b.nextJoint = vectorFromStrings(index + 1, info);
			thumb.bones.put(b.type, b);
			if (i == 0){
				SeededBone b2 = new SeededBone();
				b2.type = Bone.Type.TYPE_METACARPAL;
				b2.prevJoint = b.prevJoint;
				b2.nextJoint = b.prevJoint;
				thumb.bones.put(Bone.Type.TYPE_METACARPAL, b2);
			}
			offset += 2;
		}
		thumb.frame = frame;
		thumb.type = Finger.Type.TYPE_THUMB;
		thumb.hand = hand;
		thumb.normalize();
		fingers[0] = thumb;

		for (int j = 0; j < fingerTypes.length; j++) {
			SeededFinger finger = new SeededFinger();
			for (int i = 0; i < fingerBoneTypes.length; i++) {
				int index = offset;
				SeededBone b = new SeededBone();
				b.type = fingerBoneTypes[i];
				b.prevJoint = vectorFromStrings(index, info);
				b.nextJoint = vectorFromStrings(index + 1, info);
				finger.bones.put(b.type, b);
				offset += 2;
			}
			finger.frame = frame;
			finger.hand = hand;
			finger.type = fingerTypes[j];
			fingers[j + 1] = finger;
			finger.normalize();
		}

		SeededFingerList fl = new SeededFingerList();
		for (Finger f : fingers) {
			fl.addFinger(f);
		}
		hand.setFingerList(fl);
		hand.pinchStrength = Float.parseFloat(info[offset++]);
		hand.grabStrength = Float.parseFloat(info[offset++]);

		return hand;
	}

	private static Vector randomVector(float scale, float transpose){
		return new Vector(transpose + (random.nextFloat()*scale),
				transpose + (random.nextFloat()*scale),
				transpose + (random.nextFloat()*scale));
	}


	public static SeededHand createRandomHand(Frame frame, String id) {

		SeededHand hand = new SeededHand();

		// construct thumb
		Finger[] fingers = new Finger[1 + fingerTypes.length];
		int offset = 1;
		SeededFinger thumb = new SeededFinger();
		hand.uniqueId = id;

		for (int i = 0; i < thumbBoneTypes.length; i++) {
			int index = offset;
			SeededBone b = new SeededBone();
			b.type = thumbBoneTypes[i];
			b.prevJoint = randomVector(100, 0);
			b.nextJoint = randomVector(100, 0);
			thumb.bones.put(b.type, b);
			if (i == 0){
				SeededBone b2 = new SeededBone();
				b2.type = Bone.Type.TYPE_METACARPAL;
				b2.prevJoint = b.prevJoint;
				b2.nextJoint = b.prevJoint;
				thumb.bones.put(Bone.Type.TYPE_METACARPAL, b2);
			}
			offset += 2;
		}
		thumb.frame = frame;
		thumb.type = Finger.Type.TYPE_THUMB;
		thumb.hand = hand;
		thumb.normalize();
		fingers[0] = thumb;

		for (int j = 0; j < fingerTypes.length; j++) {
			SeededFinger finger = new SeededFinger();
			for (int i = 0; i < fingerBoneTypes.length; i++) {
				int index = offset;
				SeededBone b = new SeededBone();
				b.type = fingerBoneTypes[i];
				b.prevJoint = randomVector(100, 0);
				b.nextJoint = randomVector(100, 0);
				finger.bones.put(b.type, b);
				offset += 2;
			}
			finger.frame = frame;
			finger.hand = hand;
			finger.type = fingerTypes[j];
			fingers[j + 1] = finger;
			finger.normalize();
		}

		SeededFingerList fl = new SeededFingerList();
		for (Finger f : fingers) {
			fl.addFinger(f);
		}
		hand.setFingerList(fl);
		hand.pinchStrength = random.nextFloat();
		hand.grabStrength = random.nextFloat();

		return hand;
	}

	public static String handToString(String uniqueId, Hand h) {

		Vector handXBasis = h.palmNormal().cross(h.direction()).normalized();
		Vector handYBasis = h.palmNormal().opposite();
		Vector handZBasis = h.direction().opposite();
		Vector handOrigin = h.palmPosition();
		Matrix handTransform = new Matrix(handXBasis, handYBasis, handZBasis, handOrigin);
		handTransform = handTransform.rigidInverse();
		FingerList fl = h.fingers();
		String output = uniqueId + ",";
		Finger thumb = fl.fingerType(Finger.Type.TYPE_THUMB).get(0);
		for (int i = 0; i < thumbBoneTypes.length; i++) {
			Bone b = thumb.bone(thumbBoneTypes[i]);
			Vector prev = handTransform.transformPoint(b.prevJoint());
			Vector next = handTransform.transformPoint(b.nextJoint());
			output += prev.getX() + ",";
			output += prev.getY() + ",";
			output += prev.getZ() + ",";
			output += next.getX() + ",";
			output += next.getY() + ",";
			output += next.getZ() + ",";
		}
		for (int j = 0; j < fingerTypes.length; j++) {
			Finger finger = fl.fingerType(fingerTypes[j]).get(0);
			for (int i = 0; i < fingerBoneTypes.length; i++) {
				Bone b = finger.bone(fingerBoneTypes[i]);
				Vector prev = handTransform.transformPoint(b.prevJoint());
				Vector next = handTransform.transformPoint(b.nextJoint());
				output += prev.getX() + ",";
				output += prev.getY() + ",";
				output += prev.getZ() + ",";
				output += next.getX() + ",";
				output += next.getY() + ",";
				output += next.getZ() + ",";
			}
		}

		output += h.pinchStrength() + ",";
		output += h.grabStrength() + ",";

		if (output.endsWith(",")) {
			output = output.substring(0, output.length() - 1);
		}
		return output;
	}

	public static Vector vectorFromStrings(int index, String[] strings) {
		int i = 1 + (index - 1) * 3;

		return new Vector(Float.parseFloat(strings[i]), Float.parseFloat(strings[i + 1]),
				Float.parseFloat(strings[i + 2]));
	}

	public static Hand convertToLeft(Hand hand) {
		return hand;
	}

	private HandFactory() {

	}
}
