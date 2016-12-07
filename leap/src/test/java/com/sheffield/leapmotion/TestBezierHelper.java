package com.sheffield.leapmotion;

import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.frame.util.BezierHelper;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Created by thomas on 10/27/2016.
 */
public class TestBezierHelper {

    @Test
    public void testLinearInterpolation(){
        ArrayList<Vector> vecs = new ArrayList<Vector>();

        vecs.add(new Vector(0, 1, 1));
        vecs.add(new Vector(1, 0, 0));

        Vector result = BezierHelper.bezier(vecs, 0.5f);

        assertEquals(0.5f, result.getX(), 0.0000001f);
        assertEquals(0.5f, result.getY(), 0.0000001f);
        assertEquals(0.5f, result.getZ(), 0.0000001f);

        result = BezierHelper.bezier(vecs, 0f);

        assertEquals(vecs.get(0).getX(), result.getX(), 0.0000001f);
        assertEquals(vecs.get(0).getY(), result.getY(), 0.0000001f);
        assertEquals(vecs.get(0).getZ(), result.getZ(), 0.0000001f);

        result = BezierHelper.bezier(vecs, 1f);

        assertEquals(vecs.get(1).getX(), result.getX(), 0.0000001f);
        assertEquals(vecs.get(1).getY(), result.getY(), 0.0000001f);
        assertEquals(vecs.get(1).getZ(), result.getZ(), 0.0000001f);

    }

    @Test
    public void testBezierInterpolation(){
        ArrayList<Vector> vecs = new ArrayList<Vector>();

        vecs.add(new Vector(0, 0, 0));
        vecs.add(new Vector(1, 1, 1));
        vecs.add(new Vector(0, 0, 1));


        Vector result = BezierHelper.bezier(vecs, 0.5f);

        assertEquals(0.5f, result.getX(), 0.0000001f);
        assertEquals(0.5f, result.getY(), 0.0000001f);
        assertEquals(0.75f, result.getZ(), 0.0000001f);

        result = BezierHelper.bezier(vecs, 0f);

        assertEquals(vecs.get(0).getX(), result.getX(), 0.0000001f);
        assertEquals(vecs.get(0).getY(), result.getY(), 0.0000001f);
        assertEquals(vecs.get(0).getZ(), result.getZ(), 0.0000001f);

        result = BezierHelper.bezier(vecs, 1f);

        assertEquals(vecs.get(2).getX(), result.getX(), 0.0000001f);
        assertEquals(vecs.get(2).getY(), result.getY(), 0.0000001f);
        assertEquals(vecs.get(2).getZ(), result.getZ(), 0.0000001f);

    }

}
