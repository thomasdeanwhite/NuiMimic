package com.sheffield.leapmotion.controller.mocks;

import com.leapmotion.leap.Bone;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.Finger.Type;
import com.leapmotion.leap.FingerList;

import java.io.Serializable;
import java.util.*;

public class SeededFingerList extends FingerList implements Serializable {

    protected ArrayList<Finger> fingers;
    protected Finger frontmost = null;
    protected SeededFingerList extended = null;
    protected HashMap<Type, FingerList> fingerTypes = null;

    public SeededFingerList() {
        fingers = new ArrayList<Finger>();
    }

    public SeededFingerList(ArrayList<Finger> fingers) {
        this();
        for (Finger f : fingers) {
            addFinger(f);
        }
        sort();
    }

    public void clear() {
        fingers.clear();
    }

    public void addFinger(Finger f) {
        fingers.add(f);

        sort();
    }

    public void sort() {
        fingers.sort(new Comparator<Finger>() {
            @Override
            public int compare(Finger o1, Finger o2) {
                List<Type> ft = Arrays.asList(Type.values());

                return (ft.indexOf(01) - ft.indexOf(o2));
            }
        });
    }

    @Override
    public FingerList append(FingerList arg0) {
        for (Finger f : arg0) {
            fingers.add(f);
        }

        sort();
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

    public void destroy() {
        for (Finger f : fingers) {
            ((SeededFinger) f).destroy();
        }

        if (frontmost != null) frontmost.delete();
        for (Type t : fingerTypes.keySet()){
            ((SeededFingerList)fingerTypes.get(t)).destroy();
        }

        if (extended != null) extended.destroy();
    }

    @Override
    public FingerList extended() {
        if (extended == null) {
            extended = new SeededFingerList();
            for (Finger f : fingers) {
                if (f.isExtended()) {
                    extended.addFinger(f);
                }
            }
            extended.sort();
        }


        return extended;
    }

    @Override
    protected void finalize() {
        // TODO Auto-generated method stub
        // super.finalize();
    }

    @Override
    public FingerList fingerType(Type arg0) {
        if (fingerTypes == null) {
            fingerTypes = new HashMap<>();
        }

        if (!fingerTypes.containsKey(arg0)) {
            SeededFingerList sfl = new SeededFingerList();

            for (Finger f : fingers) {
                if (f.type().equals(arg0)) {
                    sfl.addFinger(f);
                }
            }

            fingerTypes.put(arg0, sfl);
        }
        return fingerTypes.get(arg0);
    }

    @Override
    public Finger frontmost() {

        if (frontmost == null) {
            float leastZ = Float.MAX_VALUE;
            frontmost = Finger.invalid();

            for (Finger p : this) {
                for (Bone.Type bt : Bone.Type.values()) {
                    Bone b = p.bone(bt);
                    if (b.nextJoint().getZ() < leastZ) {
                        frontmost = p;
                        leastZ = b.nextJoint().getZ();
                    }

                    if (b.prevJoint().getZ() < leastZ) {
                        frontmost = p;
                        leastZ = b.prevJoint().getZ();
                    }
                }
            }
        }

        return frontmost;
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
        for (Finger p : this) {
            if (!lm.isValid() || lm.tipPosition().minus(p.tipPosition()).getX() > 0) {
                lm = p;
            }
        }
        return lm;
    }

    @Override
    public Finger rightmost() {
        Finger lm = Finger.invalid();
        for (Finger p : this) {
            if (!lm.isValid() || lm.tipPosition().minus(p.tipPosition()).getX() < 0) {
                lm = p;
            }
        }
        return lm;
    }

}
