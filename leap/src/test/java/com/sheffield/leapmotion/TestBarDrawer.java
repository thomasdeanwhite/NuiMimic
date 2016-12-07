package com.sheffield.leapmotion;

import com.sheffield.leapmotion.util.ProgressBar;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by thomas on 18/11/2016.
 */
public class TestBarDrawer {

    @Test
    public void testHeaderedBarDrawer(){
        ProgressBar.BarDrawer bd = new ProgressBar.HeaderedBarDrawer();

        int bars = 21;

        assertEquals("|0 ----- 50 ----- 100%|", bd
                .getBarHeader(bars));

        assertEquals("===========            50.0%", bd.drawBar(bars, 0.5f));
    }

}
