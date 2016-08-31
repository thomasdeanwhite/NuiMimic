package com.sheffield.leapmotion;

import com.leapmotion.leap.Vector;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by thomas on 16/05/2016.
 */
public class TestQuaternionHelper {

    @Test
    public void testNormalisation (){
        Quaternion q = new Quaternion(0.95018107247191f,0.226925766966292f,-0.0176523683707865f,-0.212077376629213f);

        q = q.normalise();

        assertEquals(1f, q.squareMagnitude(), 0.001f);

    }

    @Test
    public void fromRotationMatrix(){
        Vector[] vs = new Vector[]{
                new Vector(1, 0, 0),
                new Vector(0, 0, -1),
                new Vector(0, 1, 0)
        };
        Quaternion q = QuaternionHelper.toQuaternion(vs);

        Vector[] out = q.toMatrix(false);


        for (int i = 0; i < out.length; i++) {
            assertEquals(vs[i].getX(), out[i].getX(), 0.0001);
            assertEquals(vs[i].getY(), out[i].getY(), 0.0001);
            assertEquals(vs[i].getZ(), out[i].getZ(), 0.0001);
        }
    }

    @Test
    public void toRotationMatrix(){
        Quaternion q = new Quaternion(0.95035785f, 0.22696799f,-0.017655654f,-0.21211684f);

        Vector[] v = q.toMatrix(false);

        assertEquals(0.909389, v[0].getX(), 0.00001);
        assertEquals(0.395159, v[0].getY(), 0.00001);
        assertEquals(-0.129846, v[0].getZ(), 0.00001);

        assertEquals(-0.411188, v[1].getX(), 0.00001);
        assertEquals(0.806984, v[1].getY(), 0.00001);
        assertEquals(-0.423912, v[1].getZ(), 0.00001);

        assertEquals(-0.0627291, v[2].getX(), 0.00001);
        assertEquals(0.438892, v[2].getY(), 0.00001);
        assertEquals(0.896348, v[2].getZ(), 0.00001);
    }

    @Test
    public void testMultiplication (){
        Quaternion q = new Quaternion(1, 0, 1, 0);
        Quaternion q2 = new Quaternion(1, 0.5f, 0.5f, 0.75f);

        Quaternion r = q.multiply(q2);

        assertEquals(0.5, r.w, 0.0001);
        assertEquals(1.25, r.x, 0.0001);
        assertEquals(1.5, r.y, 0.0001);
        assertEquals(0.25, r.z, 0.0001);
    }

    @Test
    public void testMultiplication2 (){
        Quaternion q = new Quaternion(1, 0, 1, 0);

        Quaternion r = q.multiply(q);

        assertEquals(0, r.w, 0.0001);
        assertEquals(0, r.x, 0.0001);
        assertEquals(2, r.y, 0.0001);
        assertEquals(0, r.z, 0.0001);
    }

}
