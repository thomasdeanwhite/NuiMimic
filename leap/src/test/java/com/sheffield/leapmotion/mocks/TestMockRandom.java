package com.sheffield.leapmotion.mocks;

import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.instrumentation.MockRandom;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Created by thomas on 18/11/2016.
 */
public class TestMockRandom {

    @Before
    public void clearRandoms(){
        MockRandom.randoms.clear();
    }

    @Test
    public void testDeterministicRandom(){
        Random r = MockRandom.getRandom("Foo");

        assertEquals(-1070839818, r.nextInt());
        assertEquals(-1527491647, r.nextInt());
    }

    @Test
    public void testRandomSameClassName(){
        Random r = MockRandom.getRandom("Foo");

        MockRandom.randoms.clear();

        Random r2 = MockRandom.getRandom("Foo");

        for (int i = 0; i < 100; i++){
            assertEquals(r.nextInt(), r2.nextInt());
        }
    }

}
