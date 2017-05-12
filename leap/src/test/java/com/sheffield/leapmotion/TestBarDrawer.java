package com.sheffield.leapmotion;

import com.sheffield.leapmotion.util.ProgressBar;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by thomas on 18/11/2016.
 */
public class TestBarDrawer {

    String os = System.getProperty("os.name").toLowerCase();

    public String osify(String s){
        if (os.contains("windows")){
            s = s.replace("∎", "#");
        }
        return s;
    }

    @Test
    public void testHeaderedBarDrawer(){
        ProgressBar.BarDrawer bd = new ProgressBar.HeaderedBarDrawer();

        int bars = 21;

        assertEquals("|0 ----- 50 ----- 100%|", bd
                .getBarHeader(bars));

        assertEquals(osify("|∎∎∎∎∎∎∎∎∎∎           | 50.0%"), bd.drawBar(bars,
                0.5f));

        assertEquals(bd.getBarHeader(bars).length(), bd.drawBar(bars, 0.5f).lastIndexOf("|")+1);
    }


    @Test
    public void testHeaderedBarDrawerEmpty(){
        ProgressBar.BarDrawer bd = new ProgressBar.HeaderedBarDrawer();

        int bars = 21;

        assertEquals("|0 ----- 50 ----- 100%|", bd
                .getBarHeader(bars));

        assertEquals(osify("|                     | 0.0%"), bd.drawBar(bars,
                0.0f));
    }

    @Test
    public void testHeaderedBarDrawerFull(){
        ProgressBar.BarDrawer bd = new ProgressBar.HeaderedBarDrawer();

        int bars = 21;

        assertEquals("|0 ----- 50 ----- 100%|", bd
                .getBarHeader(bars));

        assertEquals(osify("|∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎| 100.0%"), bd.drawBar(bars,
                1f));
    }

}
