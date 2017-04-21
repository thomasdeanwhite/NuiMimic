package com.sheffield.leapmotion.controller.mocks;

import com.leapmotion.leap.InteractionBox;
import com.leapmotion.leap.Vector;

/**
 * Created by thomas on 18/04/17.
 */
public class SeededInteractionBox extends InteractionBox {

    public SeededInteractionBox(long l, boolean b) {
        super(l, b);
    }

    @Override
    protected void finalize() {
        //super.finalize();
    }

    @Override
    public synchronized void delete() {
        //super.delete();
    }

    public SeededInteractionBox() {
        super();
    }

    @Override
    public Vector normalizePoint(Vector vector, boolean b) {
        return super.normalizePoint(vector, b);
    }

    @Override
    public Vector normalizePoint(Vector vector) {
        Vector p = vector.minus(center());
        return new Vector(p.getX() / width(),
                p.getY() / height(),
                p.getZ() / depth());
    }

    @Override
    public Vector denormalizePoint(Vector vector) {
        return super.denormalizePoint(vector);
    }

    @Override
    public Vector center() {
        return super.center();
    }

    @Override
    public float width() {
        return super.width();
    }

    @Override
    public float height() {
        return super.height();
    }

    @Override
    public float depth() {
        return super.depth();
    }

    @Override
    public boolean isValid() {
        return super.isValid();
    }

    @Override
    public boolean equals(InteractionBox interactionBox) {
        return super.equals(interactionBox);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
