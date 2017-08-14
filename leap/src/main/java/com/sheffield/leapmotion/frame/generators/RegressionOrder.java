package com.sheffield.leapmotion.frame.generators;

import java.io.Serializable;

public class RegressionOrder implements Serializable {

    private String jointCluster;
    private String posCluster;
    private String rotCluster;
    private String stabilisedCluster;
    private String gesture;
    private String gestureCircleCluster;
    private long timestamp;
    private int state;

    public RegressionOrder(String jointCluster, String posCluster, String rotCluster,
                           String stabilisedCluster, String gesture, String gestureCircleCluster, long timestamp,
                           int state) {
        this.jointCluster = jointCluster;
        this.posCluster = posCluster;
        this.rotCluster = rotCluster;
        this.stabilisedCluster = stabilisedCluster;
        this.gesture = gesture;
        this.gestureCircleCluster = gestureCircleCluster;
        this.timestamp = timestamp;
        this.state = state;
    }

    public String getJointCluster() {
        return jointCluster;
    }

    public int getState() {
        return state;
    }

    public String getPosCluster() {
        return posCluster;
    }

    public String getRotCluster() {
        return rotCluster;
    }

    public String getStabilisedCluster() {
        return stabilisedCluster;
    }

    public String getGesture() {
        return gesture;
    }

    public String getGestureCircleCluster() {
        return gestureCircleCluster;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
