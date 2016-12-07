package com.sheffield.leapmotion.controller.mocks;

import com.leapmotion.leap.Hand;
import com.leapmotion.leap.HandList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

public class SeededHandList extends HandList implements Serializable {

	protected ArrayList<Hand> hands;

	public SeededHandList() {
		hands = new ArrayList<Hand>();
	}

	public void addHand(Hand h) {
		hands.add(h);

		sort();
	}

	public void clear() {
		hands.clear();
	}

	@Override
	public HandList append(HandList arg0) {
		for (Hand h : arg0) {
			hands.add(h);
		}
		sort();
		return this;
	}

	public void sort(){
		hands.sort(new Comparator<Hand>() {
			@Override
			public int compare(Hand o1, Hand o2) {
				int x = o1.isLeft()? 1 : -1;

				int y = o2.isLeft() ? 1 : -1;

				return x - y;
			}
		});
	}

	@Override
	public int count() {
		return hands.size();
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

	@Override
	public Hand frontmost() {
		// TODO Auto-generated method stub
		return hands.get(0);
	}

	@Override
	public Hand get(int arg0) {
		if (arg0 < hands.size() && arg0 >= 0) {
			return hands.get(arg0);
		} else {
			return Hand.invalid();
		}
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return hands.isEmpty();
	}

	@Override
	public Iterator<Hand> iterator() {
		// TODO Auto-generated method stub
		return hands.iterator();
	}

	@Override
	public Hand leftmost() {
		// TODO Auto-generated method stub
		return hands.get(0);
	}

	@Override
	public Hand rightmost() {
		return hands.get(0);
	}

}
