package com.sheffield.leapmotion.frame.generators;

import com.google.gson.Gson;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.output.StateComparator;
import com.sheffield.leapmotion.output.TestingStateComparator;
import com.sheffield.leapmotion.util.FileHandler;
import com.scythe.output.Csv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class RegressionFrameGenerator extends SequenceFrameGenerator {

	private ArrayList<RegressionOrder> clustersOrders;
	private int currentRegression = 0;
	private HashMap<Integer, Integer[]> regressionStates;
	private HashMap<Integer, Integer> stateConversionMap;
	private int assertionState = -1;
	private long firstTime = -1;

	@Override
	public void setGestureOutputFile(File f) {

	}

	@Override
	public Csv getCsv() {
		return new Csv();
	}

	public RegressionFrameGenerator(String filename) {
		super(filename);
		File regressionFile = new File(Properties.TESTING_OUTPUT + "/" + Properties.CURRENT_RUN+ "/" + Properties.FRAME_REGRESSION_STRATEGY + "/regression_orders.json");
		File statesFile = new File(Properties.TESTING_OUTPUT + "/" + Properties.CURRENT_RUN+ "/" + Properties.FRAME_REGRESSION_STRATEGY + "/states.json");

		regressionStates = new HashMap<>();
		stateConversionMap = new HashMap<>();
		clustersOrders = new ArrayList<>();

		try {
			String[] regressionStrings = FileHandler.readFile(regressionFile).split("\n");

			Gson gson = new Gson();

			for (String s : regressionStrings){
				clustersOrders.add(gson.fromJson(s, RegressionOrder.class));
			}

			clustersOrders.sort((o1, o2) -> (int)(o1.getTimestamp()-o2.getTimestamp()));

			long firstTime = clustersOrders.get(0).getTimestamp();

			for (RegressionOrder rg : clustersOrders){
				rg.reduceTimestamp(firstTime);
			}

			String[] stateStrings = FileHandler.readFile(statesFile).split("\n");

			for (String s : stateStrings){
				if (s.trim().length() > 0) {
					String[] split = s.split(":");

					String[] stateSplit = split[1].split(",");

					Integer[] state = new Integer[stateSplit.length];

					for (int i = 0; i < stateSplit.length; i++){
						state[i] = Integer.parseInt(stateSplit[i]);
					}


					int newState = TestingStateComparator.addState(state);

					stateConversionMap.put(Integer.parseInt(split[0]), newState);

					regressionStates.put(newState, state);
				}
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("File " + regressionFile + " not found!");
		}
	}

	@Override
	public boolean allowProcessing() {
		return true;
	}

	@Override
	public String getName() {
		return "Regression Testing";
	}

	@Override
	public String nextSequenceJoints() {
		return clustersOrders.get(currentRegression).getJointCluster();
	}

	@Override
	public String nextSequencePosition() {
		return clustersOrders.get(currentRegression).getPosCluster();
	}

	@Override
	public String nextSequenceRotation() {
		return clustersOrders.get(currentRegression).getRotCluster();
	}

	@Override
	public String nextSequenceGesture() {
		return clustersOrders.get(currentRegression).getGesture();
	}

	@Override
	public String nextSequenceCircleGesture() {
		return clustersOrders.get(currentRegression).getGestureCircleCluster();
	}

	@Override
	public String nextSequenceStabilisedTips() {
		return clustersOrders.get(currentRegression).getStabilisedCluster();
	}

	private String status = "";

	@Override
	public void tick(long time) {

		int state = TestingStateComparator.getCurrentState();

		status = "assert " + assertionState + " == " + state;

		if (assertionState != -1){
			int assertionEquals = stateConversionMap.get(assertionState);
			if (state != assertionEquals){
				//Screenshot cache could be outdated, lets update
				StateComparator.captureState();
				state = TestingStateComparator.getCurrentState();
				if (state != assertionEquals){
					//Screenshot is up to date: This is a failure
					Integer[] actualState = TestingStateComparator.getState(state);
					Integer[] expectedState = regressionStates.get(assertionEquals);
					Integer[] storedExpected = TestingStateComparator.getState(assertionEquals);


					int expectedSum = TestingStateComparator.sum(expectedState);

					if (expectedState != storedExpected) {
						// references are not equal, maybe there is a desync between TStateComp and RegFrameGen?
						int syncDifference = TestingStateComparator.calculateStateDifference(
								expectedState, storedExpected);

						if (!TestingStateComparator.isSameState(syncDifference
								, expectedSum)) {
							//Desync!
							throw new IllegalStateException("Desync between Comparator and Regression Storage!");
						}
					}

					// We must be in a difference screen state?
					int regressionDifference = TestingStateComparator.calculateStateDifference(
							expectedState, actualState);

					if (!TestingStateComparator.isSameState(regressionDifference
							, expectedSum)) {
						//This is a failure!
						throw new AssertionError("Unseen state found! Difference of " +
								(100f * regressionDifference / (float) expectedSum) + "%.");
					}
				}
			}
		}

		if (firstTime < 0){
			firstTime = time;
		}

		while(clustersOrders.get(currentRegression).getTimestamp() < time - firstTime && currentRegression < clustersOrders.size()){
			assertionState = clustersOrders.get(currentRegression).getState();
			currentRegression++;
		}


		super.tick(time);
	}

	public long lastTick(){
		return lastUpdate;
	}

	@Override
	public String status() {
		return status;
	}

	@Override
	public void cleanUp() {

	}
}
