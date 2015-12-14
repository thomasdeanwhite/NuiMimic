package com.sheffield.leapmotion.sampler;

import com.google.gson.Gson;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.sheffield.leapmotion.mocks.HandFactory;
import com.sheffield.leapmotion.mocks.SeededFrame;
import com.sheffield.leapmotion.mocks.SeededHandList;

import java.util.ArrayList;

public class Serializer {
	private static Gson GSON = new Gson();

	public static String sequenceToJson(ArrayList<Frame> frames) {
		byte[][] list = new byte[frames.size()][];

		for (int i = 0; i < frames.size(); i++) {
			byte[] b = frames.get(i).serialize();
			list[i] = b;
		}

		String content = GSON.toJson(list);
		return content;
	}

	public static ArrayList<Frame> sequenceFromJson(String json) {
		byte[][] rawFrames = GSON.fromJson(json, byte[][].class);
		ArrayList<Frame> frames = new ArrayList<Frame>();
		for (int i = 0; i < rawFrames.length; i++) {
			byte[] rawFrame = rawFrames[i];
			Frame frame = new Frame();
			frame.deserialize(rawFrame);
			frames.add(frame);
		}
		return frames;
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
