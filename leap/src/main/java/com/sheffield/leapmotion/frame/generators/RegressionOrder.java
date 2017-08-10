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

    public RegressionOrder(String jointCluster, String posCluster, String rotCluster,
                           String stabilisedCluster, String gesture, String gestureCircleCluster, long timestamp) {
        this.jointCluster = jointCluster;
        this.posCluster = posCluster;
        this.rotCluster = rotCluster;
        this.stabilisedCluster = stabilisedCluster;
        this.gesture = gesture;
        this.gestureCircleCluster = gestureCircleCluster;
        this.timestamp = timestamp;
    }

    public String getJointCluster() {
        return jointCluster;
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
