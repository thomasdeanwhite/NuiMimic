package com.sheffield.leapmotion.controller;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.controller.listeners.FrameSwitchListener;
import com.sheffield.leapmotion.frame.generators.gestures.GestureHandler;
import com.scythe.output.Csv;

/**
 * Created by thomas on 07/04/17.
 */
public class EmptyFrameHandler extends FrameHandler {
    @Override
    public String status() {
        return "";
    }

    @Override
    public FrameHandler copy() {
        return this;
    }

    public EmptyFrameHandler() {
        super();
    }

    @Override
    public void init(Controller seededController) {

    }

    @Override
    public void setGestureHandler(GestureHandler gh) {

    }

    @Override
    public void addFrameSwitchListener(FrameSwitchListener fsl) {

    }

    @Override
    public Frame getFrame() {
        return getFrame(0);
    }

    @Override
    public Frame getFrame(int i) {
        return Frame.invalid();
    }

    @Override
    public void loadNewFrame(long time) {

    }

    long lastTick = 0;

    @Override
    public void tick(long time) {
        lastTick = time;
    }

    @Override
    public long lastTick() {
        return lastTick;
    }

    @Override
    public void cleanUp() {

    }

    @Override
    public Csv getCsv() {
        return new Csv();
    }

    @Override
    public boolean allowProcessing() {
        return false;
    }

    @Override
    public boolean hasNextFrame() {
        return false;
    }

    @Override
    public float getProgress() {
        return App.getApp().getProgress();
    }

    @Override
    public String getTechnique() {
        return "Manual";
    }
}
