package com.sheffield.leapmotion.mocks;

import com.sheffield.leapmotion.instrumentation.MockGraphicsDevice;
import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.*;

/**
 * Created by thomas on 11/15/2016.
 */
public class TestMockGraphicsDevice {

    @Test
    public void testFullscreen(){
        assertFalse(MockGraphicsDevice.getDefaultScreenDevice().isFullScreenSupported());
    }

    @Test
    public void testDefaultDisplayBitDepth(){
        DisplayMode dm = MockGraphicsDevice.getDefaultScreenDevice().getDisplayMode();

        assertEquals(32, dm.getBitDepth());
    }


    @Test
    public void testDefaultDisplayRefreshRate(){
        DisplayMode dm = MockGraphicsDevice.getDefaultScreenDevice().getDisplayMode();

        assertEquals(60, dm.getRefreshRate());
    }

    @Test
    public void testDefaultDisplayResolution(){
        DisplayMode dm = MockGraphicsDevice.getDefaultScreenDevice().getDisplayMode();

        assertEquals(800, dm.getWidth());
        assertEquals(600, dm.getHeight());
    }

    @Test
    public void testDefaultDisplayGetDisplays(){
        DisplayMode dm = MockGraphicsDevice.getDefaultScreenDevice().getDisplayMode();
        DisplayMode[] dms = MockGraphicsDevice.getDefaultScreenDevice().getDisplayModes();
        assertEquals(dm, dms[0]);
    }

}
