package com.sheffield.leapmotion.controller;

import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

/**
 * Created by thomas on 21/02/2017.
 */
public class TestFrameHandler {

    @Before
    public void test(){
        PrintStream dummyStream = new PrintStream(new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                // TODO Auto-generated method stub
                //App.out.write(b);
            }

        }, true);

        App.out = dummyStream;
    }

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
