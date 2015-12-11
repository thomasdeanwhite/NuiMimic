package com.sheffield.leapmotion.mocks;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Listener;

public class SeededListener extends Listener {

	public static SeededListener listener;

	public static Listener getListener() {
		if (listener == null) {
			listener = new SeededListener();
		}
		return listener;
	}

	private SeededListener() {

	}

	@Override
	public void delete() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void finalize() {
	}

	@Override
	public void onConnect(Controller arg0) {
	}

	@Override
	public void onDeviceChange(Controller arg0) {
	}

	@Override
	public void onDisconnect(Controller arg0) {
	}

	@Override
	public void onExit(Controller arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onFocusGained(Controller arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onFocusLost(Controller arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onFrame(Controller arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onImages(Controller arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onInit(Controller arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onServiceConnect(Controller arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onServiceDisconnect(Controller arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void swigDirectorDisconnect() {
		// TODO Auto-generated method stub
	}

	@Override
	public void swigReleaseOwnership() {
		// TODO Auto-generated method stub
	}

	@Override
	public void swigTakeOwnership() {
		// TODO Auto-generated method stub
	}

}
