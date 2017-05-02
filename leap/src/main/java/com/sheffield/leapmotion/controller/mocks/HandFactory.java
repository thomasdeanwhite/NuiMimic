package com.sheffield.leapmotion.controller.mocks;

import com.leapmotion.leap.Bone;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.FingerList;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Matrix;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.frame.util.QuaternionHelper;

import java.util.HashMap;
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
		//sf.setId(System.currentTimeMillis()*1000);
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
		thumb.rotation = hand.rotation;
		thumb.tipPosition = vectorFromStrings(offset++, info);
		thumb.stabilizedTipPosition = vectorFromStrings(offset++, info);
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
			finger.rotation = hand.rotation;
			finger.tipPosition = vectorFromStrings(offset++, info);
			finger.stabilizedTipPosition = vectorFromStrings(offset++, info);
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

		hand.basis = Matrix.identity();
		hand.basis.setRotation(new Vector((float)Math.random(), (float)Math.random(), (float)Math.random()), (float)(Math.random() * Math.PI * 2f));
		hand.rotation = QuaternionHelper.toQuaternion(new Vector[]{
				hand.basis.getXBasis(), hand.basis.getYBasis(), hand.basis.getZBasis()
		});

		for (int i = 0; i < thumbBoneTypes.length; i++) {
			SeededBone b = new SeededBone();
			b.type = thumbBoneTypes[i];
			b.prevJoint = randomVector(200, -100);
			b.nextJoint = randomVector(200, -100);
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
		thumb.basis = hand.basis;
		thumb.rotation = hand.rotation;
		thumb.tipPosition = randomVector(200, -100);
		thumb.stabilizedTipPosition = randomVector(200, -100);
		thumb.normalize();
		fingers[0] = thumb;

		for (int j = 0; j < fingerTypes.length; j++) {
			SeededFinger finger = new SeededFinger();
			for (int i = 0; i < fingerBoneTypes.length; i++) {
				SeededBone b = new SeededBone();
				b.type = fingerBoneTypes[i];
				b.prevJoint = randomVector(200, -100);
				b.nextJoint = randomVector(200, -100);
				finger.bones.put(b.type, b);
				offset += 2;
			}
			finger.frame = frame;
			finger.hand = hand;
			finger.type = fingerTypes[j];
			finger.basis = hand.basis;
			finger.rotation = hand.rotation;
			finger.tipPosition = randomVector(200, -100);
			finger.stabilizedTipPosition = randomVector(200, -100);
			fingers[j + 1] = finger;
			finger.normalize();
		}

		SeededFingerList fl = new SeededFingerList();
		for (Finger f : fingers) {
			fl.addFinger(f);
		}

		hand.stabilizedPalmPosition = randomVector(200, -100);
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
		Vector thumbTip = handTransform.transformPoint(thumb.tipPosition());
		Vector thumbStabilizedTip = handTransform.transformPoint(thumb.stabilizedTipPosition());

		output += thumbTip.getX() + ",";
		output += thumbTip.getY() + ",";
		output += thumbTip.getZ() + ",";


		output += thumbStabilizedTip.getX() + ",";
		output += thumbStabilizedTip.getY() + ",";
		output += thumbStabilizedTip.getZ() + ",";

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

			Vector fingerTip = handTransform.transformPoint(finger
					.tipPosition());

			Vector fingerStabilizedTip = handTransform.transformPoint(finger
					.stabilizedTipPosition());

			output += fingerTip.getX() + ",";
			output += fingerTip.getY() + ",";
			output += fingerTip.getZ() + ",";

			output += fingerStabilizedTip.getX() + ",";
			output += fingerStabilizedTip.getY() + ",";
			output += fingerStabilizedTip.getZ() + ",";
		}

		output += h.pinchStrength() + ",";
		output += h.grabStrength() + ",";

		if (output.endsWith(",")) {
			output = output.substring(0, output.length() - 1);
		}
		return output;
	}


	public static String handToHandJoint(String uniqueId, Hand h) {
		FingerList fl = h.fingers();
		String output = uniqueId + ",";
		Finger thumb = fl.fingerType(Finger.Type.TYPE_THUMB).get(0);
		for (int i = 0; i < thumbBoneTypes.length; i++) {
			Bone b = thumb.bone(thumbBoneTypes[i]);
			Vector prev = b.prevJoint();
			Vector next = b.nextJoint();
			output += prev.getX() + ",";
			output += prev.getY() + ",";
			output += prev.getZ() + ",";
			output += next.getX() + ",";
			output += next.getY() + ",";
			output += next.getZ() + ",";
		}

		Vector thumbTip = thumb.tipPosition();

		Vector thumbStabilizedTip = thumb.stabilizedTipPosition();

		output += thumbTip.getX() + ",";
		output += thumbTip.getY() + ",";
		output += thumbTip.getZ() + ",";


		output += thumbStabilizedTip.getX() + ",";
		output += thumbStabilizedTip.getY() + ",";
		output += thumbStabilizedTip.getZ() + ",";

		for (int j = 0; j < fingerTypes.length; j++) {
			Finger finger = fl.fingerType(fingerTypes[j]).get(0);
			for (int i = 0; i < fingerBoneTypes.length; i++) {
				Bone b = finger.bone(fingerBoneTypes[i]);
				Vector prev = b.prevJoint();
				Vector next = b.nextJoint();
				output += prev.getX() + ",";
				output += prev.getY() + ",";
				output += prev.getZ() + ",";
				output += next.getX() + ",";
				output += next.getY() + ",";
				output += next.getZ() + ",";
			}

			Vector fingerTip = finger.tipPosition();

			Vector fingerStabilizedTip = finger
					.stabilizedTipPosition();

			output += fingerTip.getX() + ",";
			output += fingerTip.getY() + ",";
			output += fingerTip.getZ() + ",";

			output += fingerStabilizedTip.getX() + ",";
			output += fingerStabilizedTip.getY() + ",";
			output += fingerStabilizedTip.getZ() + ",";
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

	public static String toJavaScript(Frame f){
		//TODO: Continue implementing this abiding by Leap Motion JS API.
		// big long method to convert everything to JavaScript

		String fingers = "[";

		for (Finger fi : f.fingers()){
			fingers += "{";
			String bones = "[";

			HashMap<Bone.Type, String> boneMap = new HashMap<>();

			for (Bone.Type bt : Bone.Type.values()){
				String b = "{";

				Bone bone = fi.bone(bt);

				Matrix basis = bone.basis();

				b += "this.basis=[[" + vectorToCsv(basis.getXBasis()) + "]," +
						vectorToCsv(basis.getYBasis()) + "]," +
						vectorToCsv(basis.getZBasis()) + "]];";
				b += "this.length=" + bone.length() + ";";

				b += "this.nextJoint=[" + vectorToCsv(bone.nextJoint()) + "];";
				b += "this.prevJoint=[" + vectorToCsv(bone.prevJoint()) + "];";

				b += "this.type=" + bt.swigValue() + ";";

				b += "this.width=" + bone.width() + ";";

				b += "this.center=function(){" +
						"return [" + vectorToCsv(bone.center()) + "];" +
						"}";

				b += "this.direction=function(){return [" + vectorToCsv(bone.direction()) + "];}";

				b += "this.left=function(){return" + fi.hand().isLeft() + ";}";

				b += "this.lerp=function(out, t){ " +
						"out[0] = (this.prevJoint[0] + this.nextJoint[0]) * t;" +
						"out[1] = (this.prevJoint[1] + this.nextJoint[1]) * t;" +
						"out[2] = (this.prevJoint[2] + this.nextJoint[2]) * t;" +
						"return out;}";

				float[] basisMatrix = bone.basis().toArray4x4();

				b += "this.matrix=[";

				for (int p = 0; p < basisMatrix.length; p++){
					b += basisMatrix[p];
					if (p < basisMatrix.length-1){
						b += ",";
					}
				}

				b += "];";




				b += "}";

				boneMap.put(bt, b);

				bones += b + ",";
			}
			bones += "]";
			fingers += "this.bones=" + bones + ";";

			fingers += "this.carpPosition=[" + vectorToCsv(fi.bone(Bone.Type.TYPE_METACARPAL).prevJoint()) + "];";

			fingers += "this.dipPosition=[" + vectorToCsv(fi.bone(Bone.Type.TYPE_DISTAL).prevJoint()) + "];";

			fingers += "this.distal=" + boneMap.get(Bone.Type.TYPE_DISTAL) + ";";

			fingers += "this.medial=" + boneMap.get(Bone.Type.TYPE_INTERMEDIATE) + ";";

			fingers += "this.extended=" + fi.isExtended() + ";";

			fingers += "this.mcpPosition=[" + vectorToCsv(fi.bone(Bone.Type.TYPE_METACARPAL).nextJoint()) + "];";

			fingers += "this.metacarpel=" + boneMap.get(Bone.Type.TYPE_METACARPAL) + ";";

			fingers += "this.pipPosition=[" + vectorToCsv(fi.bone(Bone.Type.TYPE_PROXIMAL).nextJoint()) + "];";

			fingers += "this.proximal=" + boneMap.get(Bone.Type.TYPE_PROXIMAL) + ";";

			fingers += "this.type=" + fi.type().swigValue() + ";";

			fingers += "}";
		}

		fingers += "]";

		String script = "function Frame () {" +
				"this.currentFrameRate=" + App.getApp().getFps() + ";" +
				"this.id=" + f.id() + ";" +
				"this.valid=" + f.isValid() + ";" +
				"this.timestamp=" + f.timestamp() + ";" +
				"this.fingers=" + fingers + ";" +
				"};";

		return script;
	}

	private static String vectorToCsv(Vector v){
		return v.getX() + "," + v.getY() + "," + v.getZ();
	}

	private HandFactory() {

	}
}
