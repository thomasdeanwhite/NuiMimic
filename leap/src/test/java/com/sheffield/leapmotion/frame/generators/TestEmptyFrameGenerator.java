package com.sheffield.leapmotion.frame.generators;

import com.leapmotion.leap.Frame;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.controller.mocks.SeededFrame;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by thomas on 27/03/17.
 */
public class TestEmptyFrameGenerator {


    @Test
    public void emptyFgInit(){
        EmptyFrameGenerator efg = new EmptyFrameGenerator();

        SeededFrame empty = new SeededFrame(Frame.invalid());

        efg.tick(0);

        SeededFrame first = (SeededFrame) efg.newFrame();

        assertEquals(empty.hands().count(), first.hands().count());

        efg.tick(1);

        SeededFrame second = (SeededFrame) efg.newFrame();

        assertEquals(empty.hands().count(), second.hands().count());
        assertNotEquals(first.id(), second.id());
    }

}