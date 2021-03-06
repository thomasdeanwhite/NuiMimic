package com.sheffield.leapmotion.controller.mocks;

import com.leapmotion.leap.*;

import java.io.Serializable;

public class SeededGesture extends Gesture implements Serializable {


	public static CircleGesture getCircleGesture(Gesture g){
		if (g instanceof SeededGesture){
			CircleGesture cg = ((SeededGesture) g).getCircleGesture();
			return cg;
		}
		return new CircleGesture(g);
	}

	public static SwipeGesture getSwipeGesture(Gesture g){
		if (g instanceof SeededGesture){
			return ((SeededGesture) g).getSwipeGesture();
		}
		return new SwipeGesture(g);
	}


	protected Type type;
	protected State state;
	protected Frame frame;
	protected int duration;
	protected int id = 0;
	protected CircleGesture circleGesture;
	protected SwipeGesture swipeGesture;

	public SeededGesture(Type gestureType, State gestureState, Frame frame, int duration, int id) {
		type = gestureType;
		state = gestureState;
		this.frame = frame;
		this.duration = duration;
		this.id = id;
		circleGesture = new CircleGesture();
	}

	public CircleGesture getCircleGesture(){
		return circleGesture;
	}

	public void setCircleGesture(SeededCircleGesture g){
		circleGesture = g;
	}

	public SwipeGesture getSwipeGesture(){
		return swipeGesture;
	}

	public void setSwipeGesture(SeededSwipeGesture g){
		swipeGesture = g;
	}

	@Override
	public synchronized void delete() {
	}

	public void destroy(){

	}

	@Override
	public long duration() {
		// TODO Auto-generated method stub
		return duration;
	}

	@Override
	public float durationSeconds() {
		// TODO Auto-generated method stub
		return duration / 1000000;
	}

	@Override
	public boolean equals(Gesture arg0) {
		if (arg0.duration() == duration && arg0.state() == state && arg0.type() == type) {
			return true;
		}
		return false;
	}

	@Override
	public Frame frame() {
		// TODO Auto-generated method stub
		return frame;
	}

	@Override
	public HandList hands() {
		// TODO Auto-generated method stub
		return frame.hands();
	}

	@Override
	public int id() {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return true;
	}

	private SeededPointableList spl = new SeededPointableList();

	public void addPointable(SeededFinger sf){
		spl.addPointable(sf);
	}

	@Override
	public PointableList pointables() {
		// TODO Auto-generated method stub
		return spl;
	}

	@Override
	public State state() {
		// TODO Auto-generated method stub
		return state;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Seeded Gesture " + type + " " + state + " " + duration;
	}

	@Override
	public Type type() {
		// TODO Auto-generated method stub
		return type;
	}

}
