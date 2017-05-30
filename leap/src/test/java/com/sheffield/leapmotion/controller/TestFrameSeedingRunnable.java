package com.sheffield.leapmotion.controller;

import com.leapmotion.leap.Frame;
import com.sheffield.leapmotion.controller.mocks.SeededFinger;
import com.sheffield.leapmotion.controller.mocks.SeededFrame;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by thomas on 30/05/17.
 */
public class TestFrameSeedingRunnable {

    @Test
    public void testOrder (){
        SeededFrame f1 = new SeededFrame(SeededController.newFrame());
        SeededFrame f2 = new SeededFrame(SeededController.newFrame());
        SeededFrame f3 = new SeededFrame(SeededController.newFrame());

        f1.setTimestamp(0);
        f2.setTimestamp(1);
        f3.setTimestamp(2);

        FrameSeedingRunnable fsr = new FrameSeedingRunnable(null,
                f3, f2, 2);

        FrameSeedingRunnable fsr2 = new FrameSeedingRunnable(null,
                f2, f1, 1);

        assertEquals(1, fsr.compareTo(fsr2));
        assertEquals(-1, fsr2.compareTo(fsr));
        assertEquals(0, fsr.compareTo(fsr));
        assertEquals(0, fsr2.compareTo(fsr2));
    }
}
