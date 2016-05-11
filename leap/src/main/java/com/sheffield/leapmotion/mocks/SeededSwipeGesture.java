package com.sheffield.leapmotion.mocks;

import com.leapmotion.leap.*;

import java.io.Serializable;

/**
 * Created by thomas on 28/01/2016.
 */
public class SeededSwipeGesture extends SwipeGesture implements Serializable {

    private Gesture gesture;
    private Vector startPosition, position, direction;
    private float speed;
    private Pointable pointable;

    public SeededSwipeGesture(Gesture g, Vector startPosition, Vector position, Vector direction, float speed, Pointable p) {
        super(g);
        gesture = g;
        this.startPosition = startPosition;
        this.position = position;
        this.direction = direction;
        this.speed = speed;
        this.pointable = p;
    }

    @Override
    protected void finalize() {
        //super.finalize();
    }

    @Override
    public synchronized void delete() {
        //super.delete();
    }

    @Override
    public Vector startPosition() {
        return startPosition;
    }

    @Override
    public Vector position() {
        return position;
    }

    @Override
    public Vector direction() {
        return direction;
    }

    @Override
    public float speed() {
        return speed;
    }

    @Override
    public Pointable pointable() {
        return pointable;
    }

    //NORMAL GESTURE STUFF

    @Override
    public Type type() {
        return gesture.type();
    }

    @Override
    public State state() {
        return gesture.state();
    }

    @Override
    public int id() {
        return gesture.id();
    }

    @Override
    public long duration() {
        return gesture.duration();
    }

    @Override
    public float durationSeconds() {
        return gesture.durationSeconds();
    }

    @Override
    public Frame frame() {
        return gesture.frame();
    }

    @Override
    public HandList hands() {
        return gesture.hands();
    }

    @Override
    public PointableList pointables() {
        return gesture.pointables();
    }

    @Override
    public boolean isValid() {
        return gesture.isValid();
    }

    @Override
    public boolean equals(Gesture gesture) {
        return gesture.equals(gesture);
    }

    @Override
    public String toString() {
        return gesture.toString();
    }
}
