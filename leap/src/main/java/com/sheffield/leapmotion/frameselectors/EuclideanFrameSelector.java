package com.sheffield.leapmotion.frameselectors;

import com.leapmotion.leap.Bone;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.FileHandler;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.Serializer;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.output.Csv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;


public class EuclideanFrameSelector extends FrameSelector {
	private HashMap<Long, Frame> frames;
	public Csv getCsv() {
		return new Csv();
	}

	// 100^2
	public static final float MAX_DIFFERENCE = 10000f;

	public class FrameSimilarity {
		public long frame;
		public float similarity;

		public FrameSimilarity(long frame, float similarity) {
			this.frame = frame;
			this.similarity = similarity;
		}
	}

	public HashMap<Long, List<FrameSimilarity>> similarities;

	private long currentFrame;
	private File[] files;

	private Random random;

	public EuclideanFrameSelector() {
		random = new Random();
		files = FileHandler.getFiles(Properties.DIRECTORY);

		frames = new HashMap<Long, Frame>();

		similarities = new HashMap<Long, List<FrameSimilarity>>();
		while (frames.size() < Properties.MAX_LOADED_FRAMES) {
			loadNextFile();
		}

	}

	public void sortFrames() {
		for (List<FrameSimilarity> list : similarities.values()) {
			Collections.sort(list, new Comparator<FrameSimilarity>() {

				public int compare(FrameSimilarity o1, FrameSimilarity o2) {
					// TODO Auto-generated method stub
					return (int) (o2.similarity - o1.similarity);
				}

			});
		}
	}

	public void addSimilarity(Frame frame1, Frame frame2, float similarity) {
		if (!similarities.containsKey(frame1.id())) {
			similarities.put(frame1.id(), new ArrayList<FrameSimilarity>());
		}

		if (!similarities.containsKey(frame2.id())) {
			similarities.put(frame2.id(), new ArrayList<FrameSimilarity>());
		}

		similarities.get(frame1.id()).add(new FrameSimilarity(frame2.id(), similarity));
		similarities.get(frame2.id()).add(new FrameSimilarity(frame1.id(), similarity));
	}

	public void loadNextFile() {
		if (frames.size() < Properties.MAX_LOADED_FRAMES || frames.size() != 0) {
			File file = files[random.nextInt(files.length)];
			if (file.isDirectory()) {
				loadNextFile();
				return;
			}
			String contents = "";

			try {
				contents = FileHandler.readFile(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Frame frame = SeededController.newFrame();
			frame = Serializer.fromJson(frame, contents);
			if (frame != null && frame.hands() != null) {
				frames.put(frame.id(), frame);
			} else {
				return;
			}

			Long[] keyset = new Long[frames.size()];
			frames.keySet().toArray(keyset);

			for (int i = 0; i < keyset.length; i++) {
				long s1 = keyset[i];
				long s2 = frame.id();
				calculateSimilarity(s1, s2);

			}
			sortFrames();
		}
	}

	public void calculateSimilarity(long s1, long s2) {
		Frame f1 = frames.get(s1);
		Frame f2 = frames.get(s2);
		if (f1 == null || f2 == null || f1.hands() == null || f2.hands() == null || f1.equals(f2)) {
			return;
		}
		float similarity = 0;
		for (Hand h : f1.hands()) {
			for (Hand h1 : f2.hands()) {
				for (Finger fi : h.fingers()) {
					for (Bone.Type t : Bone.Type.values()) {
						Bone b = fi.bone(t);
						if (b.isValid()) {
							Finger fi2 = h1.finger(fi.id());
							Bone b2 = fi2.bone(t);
							if (b2.isValid()) {
								Vector v1 = b.center();
								Vector v2 = b2.center();

								float difference = (float) (Math.pow(v1.getX() - v2.getX(), 2)
										+ Math.pow(v1.getY() - v2.getY(), 2) + Math.pow(v1.getZ() - v2.getZ(), 2));

								similarity += MAX_DIFFERENCE - difference;
							}
						}
					}
				}
			}
		}
		addSimilarity(f1, f2, similarity);
	}

	public void recycle(long frame) {
		frames.remove(frame);
		similarities.remove(frame);
		Long[] keyset = new Long[frames.size()];
		frames.keySet().toArray(keyset);

		for (int i = 0; i < keyset.length; i++) {
			for (int j = 0; j < similarities.get(keyset[i]).size(); j++) {
				FrameSimilarity fs = similarities.get(keyset[i]).get(j);
				if (fs.frame == frame) {
					similarities.get(keyset[i]).remove(fs);
				}
			}

		}
		while (frames.size() < Properties.MAX_LOADED_FRAMES) {
			loadNextFile();
		}
	}

	public Frame newFrame() {
		Set<Long> set = similarities.keySet();
		if (!set.contains(currentFrame)) {
			int item = random.nextInt(set.size());
			int i = 0;
			long frame = -1;
			for (long s : set) {
				if (i >= item) {
					frame = s;
					break;
				}
				i++;
			}
			currentFrame = frame;
			Frame f = frames.get(frame);
			return f;
		} else {
			Frame f = null;
			int i = 0;
			while (f == null) {
				long frame = similarities.get(currentFrame).get(i).frame;

				if (frames.containsKey(frame)) {
					f = frames.get(frame);

					recycle(currentFrame);

					currentFrame = frame;
				} else {
					similarities.get(currentFrame).remove(i);
					i--;
				}

				i++;

				if (i >= frames.size()) {
					currentFrame = -1;
					return newFrame();
				}
			}
			return f;
		}

	}

	@Override
	public String status() {
		return null;
	}

	private long lastUpdate = 0;
	@Override
	public void tick(long time) {
		lastUpdate = time;
	}

	public long lastTick(){
		return lastUpdate;
	}

	@Override
	public void cleanUp() {

	}
}
