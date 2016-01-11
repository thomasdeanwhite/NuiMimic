package com.sheffield.leapmotion.sampler;

import com.google.gson.Gson;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.sheffield.leapmotion.mocks.HandFactory;
import com.sheffield.leapmotion.mocks.SeededFrame;
import com.sheffield.leapmotion.mocks.SeededHandList;

public class Serializer {
	private static Gson GSON = new Gson();

	public static String sequenceToJson(Frame frame) {
		String content = GSON.toJson(frame.serialize());
		return content;
	}

	public static Frame sequenceFromJson(String json) {
		byte[] rawFrames = GSON.fromJson(json, byte[].class);
		Frame f = new Frame();
		f.deserialize(rawFrames);
		return f;
	}

	public static Frame fromJson(Frame currentFrame, String frame) {

		if (!(currentFrame instanceof SeededFrame)) {
			currentFrame = new SeededFrame(currentFrame);
		}

		SeededFrame sf = (SeededFrame) currentFrame;

		SeededHandList shl = new SeededHandList();
		Hand hand = HandFactory.createHand(frame, currentFrame);
		shl.addHand(hand);
		sf.setHandList(shl);
		sf.setId(System.currentTimeMillis());
		return sf;
	}
}
