package com.sheffield.leapmotion.frame.generators;

import com.google.gson.Gson;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.util.FileHandler;
import com.sheffield.output.Csv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class RegressionFrameGenerator extends SequenceFrameGenerator {

	private ArrayList<RegressionOrder> clustersOrders;
	int currentRegression = 0;

	@Override
	public void setGestureOutputFile(File f) {

	}

	@Override
	public Csv getCsv() {
		return new Csv();
	}

	public RegressionFrameGenerator(String filename) {
		super(filename);
		File regressionFile = new File(Properties.TESTING_OUTPUT + "/" + Properties.CURRENT_RUN + "/regression_orders.json");

		//disable output from super class as we're regression testing
		disableOutput();

		try {
			String[] regressionStrings = FileHandler.readFile(regressionFile).split("\n");

			Gson gson = new Gson();

			for (String s : regressionStrings){
				clustersOrders.add(gson.fromJson(s, RegressionOrder.class));
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

	@Override
	public void tick(long time) {
		while(clustersOrders.get(currentRegression).getTimestamp() < time){
			currentRegression++;
		}


		super.tick(time);
	}

	public long lastTick(){
		return lastUpdate;
	}

	@Override
	public void cleanUp() {

	}
}
