package com.sheffield.leapmotion.instrumentation;


import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Created by thomas on 11/15/2016.
 */
public class TestMockGraphicsDevice {

    @Test
    public void testFullscreen(){
        assertFalse(MockGraphicsDevice.getDefaultScreenDevice().isFullScreenSupported());
    }


    @Test
    public void testDefaultDisplay(){
        DisplayMode dm = MockGraphicsDevice.getDefaultScreenDevice().getDisplayMode();

        assertNotNull(dm);
    }

//    @Test
//    public void testDefaultDisplayGetDisplays(){
//        DisplayMode dm = MockGraphicsDevice.getDefaultScreenDevice().getDisplayMode();
//        DisplayMode[] dms = MockGraphicsDevice.getDefaultScreenDevice().getDisplayModes();
//        assertEquals(dm, dms[0]);
//    }

}
