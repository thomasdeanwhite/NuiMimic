package com.sheffield.leapmotion.controller.mocks;

import com.leapmotion.leap.FingerList;
import com.leapmotion.leap.Pointable;
import com.leapmotion.leap.PointableList;
import com.leapmotion.leap.ToolList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class SeededPointableList extends PointableList implements Serializable {

	protected ArrayList<Pointable> pointables;

	protected Pointable frontMost;

	public SeededPointableList() {
		pointables = new ArrayList<Pointable>();
	}

	public void addPointable(Pointable p) {
		pointables.add(p);
	}

	public void clear() {
		pointables.clear();
	}

	public boolean contains(Pointable p){
		return pointables.contains(p);
	}

	@Override
	public PointableList append(FingerList arg0) {
		for (Pointable p : arg0) {
			pointables.add(p);
		}
		return this;
	}

	@Override
	public PointableList append(PointableList arg0) {
		for (Pointable p : arg0) {
			pointables.add(p);
		}
		return this;
	}

	@Override
	public PointableList append(ToolList arg0) {
		for (Pointable p : arg0) {
			pointables.add(p);
		}
		return this;
	}

	@Override
	public int count() {
		// TODO Auto-generated method stub
		return pointables.size();
	}

	@Override
	public synchronized void delete() {
		// TODO Auto-generated method stub
		// super.delete();
	}

	@Override
	public PointableList extended() {
		SeededPointableList ext = new SeededPointableList();
		for (Pointable p : this){
			if (p.isExtended()){
				ext.addPointable(p);
			}
		}
		return ext;
	}

	@Override
	protected void finalize() {
		// TODO Auto-generated method stub
		// super.finalize();
	}

	@Override
	public Pointable frontmost() {
		if (frontMost == null) {
			Pointable fm = Pointable.invalid();
			for (Pointable p : this) {
				if (!fm.isValid() || fm.tipPosition().minus(p.tipPosition()).getZ() > 0) {
					fm = p;
				}
			}
			frontMost = fm;
		}
		return frontMost;
	}

	@Override
	public Pointable get(int arg0) {
		if (arg0 >= 0 && arg0 < pointables.size()) {
			return pointables.get(arg0);
		}
		return Pointable.invalid();
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return pointables.isEmpty();
	}

	@Override
	public Iterator<Pointable> iterator() {
		// TODO Auto-generated method stub
		return pointables.iterator();
	}

	@Override
	public Pointable leftmost() {
		Pointable lm = Pointable.invalid();
		for (Pointable p : this){
			if (!lm.isValid() || lm.tipPosition().minus(p.tipPosition()).getX() > 0){
				lm = p;
			}
		}
		return lm;
	}

	@Override
	public Pointable rightmost() {
		Pointable lm = Pointable.invalid();
		for (Pointable p : this){
			if (!lm.isValid() || lm.tipPosition().minus(p.tipPosition()).getX() < 0){
				lm = p;
			}
		}
		return lm;
	}

}
