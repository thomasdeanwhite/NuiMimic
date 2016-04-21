package com.sheffield.leapmotion.instrumentation;

import java.awt.*;

/**
 * Created by thomas on 20/04/2016.
 */
public class MockGraphicsDevice extends GraphicsDevice {

    private static GraphicsDevice gd;

    public static GraphicsDevice getDefaultScreenDevice(){
        if (gd == null){
            gd = new MockGraphicsDevice();
        }
        return gd;
    }

    GraphicsDevice original = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    @Override
    public int getType() {
        return original.getType();
    }

    @Override
    public String getIDstring() {
        return original.getIDstring();
    }

    @Override
    public GraphicsConfiguration[] getConfigurations() {
        return original.getConfigurations();
    }

    @Override
    public GraphicsConfiguration getDefaultConfiguration() {
        return original.getDefaultConfiguration();
    }

    @Override
    public boolean isFullScreenSupported() {
        return false;
    }

    @Override
    public DisplayMode getDisplayMode() {
        return new DisplayMode(800, 600, 32, 60);
    }

    @Override
    public DisplayMode[] getDisplayModes() {
        return new DisplayMode[]{getDisplayMode()};
    }
}
