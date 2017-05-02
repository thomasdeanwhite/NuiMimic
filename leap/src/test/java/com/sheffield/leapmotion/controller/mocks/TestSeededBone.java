package com.sheffield.leapmotion.controller.mocks;

import com.leapmotion.leap.*;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.frame.util.Quaternion;
import com.sheffield.leapmotion.output.FrameDeconstructor;
import com.sheffield.leapmotion.util.Serializer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static com.sheffield.leapmotion.TestUtils.*;

/**
 * Created by thomas on 10/03/17.
 */
public class TestSeededBone {

    @Test
    public void testIdentityRotation(){
        SeededBone sb = new SeededBone();


        Vector next = new Vector(0, 0, 1);
        Vector prev = new Vector(0, 0, 0);

        sb.nextJoint = next;
        sb.prevJoint = prev;

        sb.rotation = Quaternion.IDENTITY;

        assertVectorEquals(next, sb.nextJoint);

        assertVectorEquals(prev, sb.prevJoint);


    }

    @Test
    public void testRotation(){
        SeededBone sb = new SeededBone();


        Vector next = new Vector(0, 0, 1);
        Vector prev = new Vector(0, 0, 0);

        sb.nextJoint = next;
        sb.prevJoint = prev;

        //90 deg rotation in y axis (clockwise)
        sb.rotation = new Quaternion((float)Math.cos(Math.PI/4),
                0,
                (float)Math.cos(Math.PI/4),
                0
        );

        Vector actualNext = sb.nextJoint();

        assertVectorEquals(new Vector(1, 0 ,0), actualNext);

        assertVectorEquals(prev, sb.prevJoint());


    }

    @Test
    public void testLength(){
        SeededBone sb = new SeededBone();

        sb.nextJoint = new Vector(0, 0, 1);
        sb.prevJoint = new Vector(0, 0, 0);

        sb.normalize();

        assertEquals(1, sb.length, 0.000001);
    }

    @Test
    public void testCenter(){
        SeededBone sb = new SeededBone();

        sb.nextJoint = new Vector(0, 0, 1);
        sb.prevJoint = new Vector(0, 0, 0);

        sb.normalize();

        assertVectorEquals(new Vector(0, 0 , 0.5f), sb.center);
    }

}
