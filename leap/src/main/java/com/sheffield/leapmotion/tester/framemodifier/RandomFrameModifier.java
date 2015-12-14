package com.sheffield.leapmotion.tester.framemodifier;

import java.util.Random;

import com.sheffield.leapmotion.mocks.SeededFrame;

public class RandomFrameModifier implements FrameModifier {

	public static final float JITTER = 2;
	private static final float JITTER_OVER_2 = JITTER / 2;

	public static final float Z_ROTATION_JITTER = 0.1f;
	public static final float Z_ROTATION_JITTER_OVER_2 = Z_ROTATION_JITTER / 2;

	private Random r = new Random();
	private float x = 0;
	private float y = 200;
	private float z = 0;
	private float zRotation = 0;

	@Override
	public void modifyFrame(SeededFrame frame) {
		// for (Hand h : frame.hands()) {
		// if (h instanceof SeededHand) {
		// SeededHand hand = (SeededHand) h;
		// x += r.nextFloat() * JITTER - JITTER_OVER_2;
		// y += r.nextFloat() * JITTER - JITTER_OVER_2;
		// z += r.nextFloat() * JITTER - JITTER_OVER_2;
		// zRotation += r.nextFloat() * Z_ROTATION_JITTER -
		// Z_ROTATION_JITTER_OVER_2;
		// hand.setOrigin(new Vector(x, y, z));
		// hand.setRotation(Vector.zAxis(), zRotation);
		// }
		// }
	}

}
