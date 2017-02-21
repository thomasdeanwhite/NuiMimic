package com.sheffield.leapmotion.controller;

import com.sheffield.leapmotion.Properties;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by thomas on 21/02/2017.
 */
public class TestFrameHandler {

    @Test
    public void testEmptyGen(){
        Properties.FRAME_SELECTION_STRATEGY = Properties
                .FrameSelectionStrategy.EMPTY;

        FrameHandler fh = new FrameHandler();

        SeededController.disableSuperclass();

        fh.init(SeededController.getSeededController(false));

        fh.loadNewFrame();

        assertEquals(0, fh.getFrame().hands().count());
        assertEquals(0, fh.getFrame().gestures().count());
        assertEquals(0, fh.getFrame().pointables().count());
    }

}
