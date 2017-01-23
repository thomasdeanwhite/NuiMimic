package com.sheffield.leapmotion.instrumentation;

import com.sheffield.leapmotion.instrumentation.MockJOptionPane;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by thomas on 18/11/2016.
 */
public class TestMockJOptionsPane {

    @Test
    public void testMockJOptionsPaneInputDialog(){
        assertNotNull(MockJOptionPane.showInputDialog(null, null));
        assertNotNull(MockJOptionPane.showInputDialog(null, null, null));
        assertNotNull(MockJOptionPane.showInputDialog(null, null, null, 0));
    }


}
