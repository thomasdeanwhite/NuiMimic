package com.sheffield.leapmotion.controller.mocks;

import com.leapmotion.leap.*;

import java.io.Serializable;

/**
 * Created by thomas on 05/01/2016.
 */
public class SeededCircleGesture extends CircleGesture implements Serializable {

    private Vector center;
    private Vector normal;
    private float progress;
    private float radius;
    private Pointable pointable;
    private Gesture gesture;

    //CIRCLE GESTURE OVERRIDES


    public void setCenter(Vector center) {
        this.center = center;
    }

    public void setNormal(Vector normal) {
        this.normal = normal;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setPointable(Pointable pointable) {
        this.pointable = pointable;
    }

    public void setGesture(Gesture gesture) {
        this.gesture = gesture;
    }

    public SeededCircleGesture(Gesture gesture) {
        //super(gesture);
        if (gesture instanceof SeededGesture){
            SeededGesture g = (SeededGesture) gesture;
            this.gesture = gesture;
        }
    }

    public Gesture getGesture() {
        return gesture;
    }

    @Override
    public Vector center() {
        return center;
    }

    @Override
    public Vector normal() {
        return normal;
    }

    @Override
    public float progress() {
        return progress;
    }

    @Override
    public float radius() {
        return radius;
    }

    @Override
    public Pointable pointable() {
        return pointable;
    }

    // GESTURE OVERRIDES
    @Override
    protected void finalize() {
        //super.finalize();
    }

    @Override
    public synchronized void delete() {
        //super.delete();
    }

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
        return this.gesture.equals(gesture);
    }

    @Override
    public String toString() {
        return gesture.toString();
    }

    public SeededCircleGesture copy(){
        SeededCircleGesture scg = new SeededCircleGesture(gesture);
        scg.center = center;
        scg.normal = normal;
        scg.radius = radius;
        scg.pointable = pointable;
        scg.progress = progress;
        scg.gesture = gesture;
        return scg;
    }

}
