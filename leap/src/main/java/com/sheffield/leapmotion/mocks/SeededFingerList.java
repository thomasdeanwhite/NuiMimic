package com.sheffield.leapmotion.mocks;

import com.leapmotion.leap.Finger;
import com.leapmotion.leap.Finger.Type;
import com.leapmotion.leap.FingerList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class SeededFingerList extends FingerList implements Serializable {

	protected ArrayList<Finger> fingers;

	public SeededFingerList() {
		fingers = new ArrayList<Finger>();
	}

	public SeededFingerList(ArrayList<Finger> fingers) {
		this();
		for (Finger f : fingers) {
			addFinger(f);
		}
	}

	public void clear() {
		fingers.clear();
	}

	public void addFinger(Finger f) {
		fingers.add(f);
		// if (f.isExtended()){
		// extended.fingers.add(f);
		// }
	}

	@Override
	public FingerList append(FingerList arg0) {
		for (Finger f : arg0) {
			fingers.add(f);
		}
		return this;
	}

	@Override
	public int count() {
		// TODO Auto-generated method stub
		return fingers.size();
	}

	@Override
	public synchronized void delete() {
		// TODO Auto-generated method stub
		// super.delete();
	}

	@Override
	public FingerList extended() {
		SeededFingerList ext = new SeededFingerList();
		for (Finger f : fingers) {
			if (f.isExtended() && f.type() != Finger.Type.TYPE_THUMB) {
				ext.addFinger(f);
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
	public FingerList fingerType(Type arg0) {
		return this;
	}

	@Override
	public Finger frontmost() {
		Finger fm = Finger.invalid();
		for (Finger p : this){
			if (!fm.isValid() || fm.tipPosition().minus(p.tipPosition()).getZ() > 0){
				fm = p;
			}
		}
		return fm;
	}

	@Override
	public Finger get(int arg0) {
		// TODO Auto-generated method stub
		return fingers.get(arg0);
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return fingers.isEmpty();
	}

	@Override
	public Iterator<Finger> iterator() {
		// TODO Auto-generated method stub
		return fingers.iterator();
	}


	@Override
	public Finger leftmost() {
		Finger lm = Finger.invalid();
		for (Finger p : this){
			if (!lm.isValid() || lm.tipPosition().minus(p.tipPosition()).getX() > 0){
				lm = p;
			}
		}
		return lm;
	}

	@Override
	public Finger rightmost() {
		Finger lm = Finger.invalid();
		for (Finger p : this){
			if (!lm.isValid() || lm.tipPosition().minus(p.tipPosition()).getX() < 0){
				lm = p;
			}
		}
		return lm;
	}

}
