package com.sheffield.instrumenter.listeners;

public interface StateChangeListener {
	public void onStateChange(int lastState, int nextState);
}
