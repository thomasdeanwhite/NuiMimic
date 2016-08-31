package com.sheffield.leapmotion.output;

import org.junit.Test;
import org.omg.PortableServer.POAManagerPackage.State;

import static org.junit.Assert.assertEquals;

/**
 * Created by thomas on 31/08/2016.
 */
public class TestStateComparator {

    Integer[][] states = new Integer[][]{
            {5, 5, 5, 5},
            {20, 0, 0, 0},
            {0, 10, 10, 0},
            {4, 6, 5, 5},
        };

    @Test
    public void testAddNew(){
        assertEquals(0, StateComparator.addState(states[0]));
        assertEquals(1, StateComparator.addState(states[1]));
        assertEquals(2, StateComparator.addState(states[2]));
        assertEquals(3, StateComparator.getStatesVisited());
    }

    @Test
    public void testAddRepeated(){
        assertEquals(0, StateComparator.addState(states[0]));
        assertEquals(1, StateComparator.addState(states[1]));
        assertEquals(0, StateComparator.addState(states[0]));
        assertEquals(0, StateComparator.getStatesVisited().size());

    }

    @Test
    public void testAddSimilar(){
        assertEquals(0, StateComparator.addState(states[0]));
        assertEquals(1, StateComparator.addState(states[1]));
        assertEquals(0, StateComparator.addState(states[3]));
        assertEquals(0, StateComparator.getStatesVisited().size());
    }

    @Test
    public void calculateStateDifference(){
        assertEquals(2, StateComparator.calculateStateDifference(states[0],
                states[3]));

        assertEquals(0, StateComparator.calculateStateDifference(states[0],
                states[0]));

        assertEquals(20, StateComparator.calculateStateDifference(states[0],
                states[2]));
        assertEquals(0, StateComparator.getStatesVisited().size());
    }
}
