package com.sheffield.leapmotion.controller.mocks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.GestureList;

public class SeededGestureList extends GestureList implements Serializable {

	protected ArrayList<Gesture> gestures;

	public SeededGestureList() {
		gestures = new ArrayList<Gesture>();
	}

	public void addGesture(Gesture g) {
		gestures.add(g);
	}

	@Override
	public GestureList append(GestureList arg0) {
		Iterator<Gesture> i = arg0.iterator();

		while (i.hasNext()) {
			gestures.add(i.next());
		}

		return this;
	}

	@Override
	public int count() {
		// TODO Auto-generated method stub
		return gestures.size();
	}

	@Override
	public synchronized void delete() {
		for (Gesture g : gestures) {
			g.delete();
		}
		gestures.clear();
		// super.delete();
	}

	@Override
	protected void finalize() {
		// super.finalize();
	}

	@Override
	public Gesture get(int arg0) {
		return gestures.get(arg0);
	}

	@Override
	public boolean isEmpty() {
		return gestures.isEmpty();
	}

	@Override
	public Iterator<Gesture> iterator() {
		return gestures.iterator();
	}

}
