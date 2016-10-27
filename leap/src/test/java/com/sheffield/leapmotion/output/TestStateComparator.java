package com.sheffield.leapmotion.output;

import com.sheffield.leapmotion.Properties;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by thomas on 31/08/2016.
 */

public class TestStateComparator {

    Integer[][] states = new Integer[][]{
            {50, 50, 50, 50},
            {200, 0, 0, 0},
            {0, 60, 60, 80},
            {49, 51, 50, 50},
        };

    @Before
    public void setup(){
        StateComparator.cleanUp();
    }

    @Test
    public void testAddNew(){
        assertEquals(0, StateComparator.addState(states[0]));
        assertEquals(1, StateComparator.addState(states[1]));
        assertEquals(2, StateComparator.addState(states[2]));
        assertEquals(3, StateComparator.getStates().size());
    }

    @Test
    public void testAddRepeated(){
        assertEquals(0, StateComparator.addState(states[0]));
        assertEquals(1, StateComparator.addState(states[1]));
        assertEquals(0, StateComparator.addState(states[0]));
        assertEquals(2, StateComparator.getStates().size());

    }

    @Test
    public void testAddSimilar(){
        assertEquals(0, StateComparator.addState(states[0]));
        assertEquals(1, StateComparator.addState(states[1]));

        float d = StateComparator.calculateStateDifference(states[0], states[3]) / (float) StateComparator.sum(states[0]);

        assertEquals(0, StateComparator.addState(states[3]));
        assertEquals(2, StateComparator.getStates().size());
    }

    @Test
    public void calculateStateDifference(){
        assertEquals(2, StateComparator.calculateStateDifference(states[0],
                states[3]));

        assertEquals(0, StateComparator.calculateStateDifference(states[0],
                states[0]));

        assertEquals(100, StateComparator.calculateStateDifference(states[0],
                states[2]));
        assertEquals(0, StateComparator.getStates().size());
    }

    @Test
    public void testAddSameResize(){
        Properties.HISTOGRAM_BINS = 4;
        assertEquals(0, StateComparator.addState(states[0]));
        assertEquals(0, StateComparator.addState(new Integer[]{45, 5, 5, 45, 45, 5, 5, 45}));
        assertEquals(1, StateComparator.getStates().size());
    }
}
