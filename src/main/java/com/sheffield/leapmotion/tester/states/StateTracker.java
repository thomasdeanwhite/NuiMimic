package com.sheffield.leapmotion.tester.states;

import java.awt.Graphics2D;

import com.sheffield.leapmotion.tester.App;
import com.sheffield.leapmotion.tester.Display;
import com.sheffield.leapmotion.tester.listeners.StateChangeListener;

public class StateTracker implements StateChangeListener {

	private StateTree stateTree;

	private int maxState = 0;

	public StateTracker() {
		stateTree = new StateTree();
		stateTree.addChild(new StateNode(0));
	}

	public void onStateChange(int lastState, int nextState) {

		if (lastState > maxState) {
			maxState = lastState;
		}

		if (nextState > maxState) {
			maxState = nextState;
		}

		if (!stateTree.containsState(lastState)) {
			// The old state isn't in the tree? We have a problem
			App.out.println(stateTree.toString());
			stateTree.addChild(new StateNode(nextState));
			// throw new IllegalArgumentException("State " + lastState + "
			// cannot be found in state tree!");
		} else {
			StateNode snOld = stateTree.getStateNode(lastState);
			if (stateTree.containsState(nextState)) {
				StateNode sn = stateTree.getStateNode(nextState);

				snOld.addChild(sn);
			} else {
				snOld.addChild(new StateNode(nextState));
			}
		}
		Display.getDisplay().addCommand("Changing to state @s" + nextState);
		Display.getDisplay().drawTrackerChange(this);

	}

	public String toString() {
		return stateTree.toString();
	}

	public int getMaxState() {
		return maxState;
	}

	public void paint(Graphics2D g) {
		stateTree.paint(g);
	}

}
