package com.sheffield.leapmotion;

import com.leapmotion.leap.Bone;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.Vector;

import static org.junit.Assert.assertEquals;

/**
 * Created by thomas on 02/05/17.
 */
public class TestUtils {

    public static final Bone.Type[] fingerBoneTypes = { Bone.Type.TYPE_METACARPAL, Bone.Type.TYPE_PROXIMAL,
            Bone.Type.TYPE_INTERMEDIATE, Bone.Type.TYPE_DISTAL };
    public static final Finger.Type[] fingerTypes = { Finger.Type.TYPE_THUMB, Finger.Type.TYPE_INDEX, Finger.Type.TYPE_MIDDLE,
            Finger.Type.TYPE_RING, Finger.Type.TYPE_PINKY };

    public static void assertVectorEquals(Vector v1, Vector v2){
        assertVectorEquals("\nExpected: " + v1 + "\nActual: " + v2,
                v1, v2);
    }

    public static void assertVectorEquals(String error, Vector v1, Vector v2){
        assertEquals(error, v1.getX(), v2.getX(), 0.0001f);
        assertEquals(error, v1.getY(), v2.getY(), 0.0001f);
        assertEquals(error, v1.getZ(), v2.getZ(), 0.0001f);
    }

    public static void assertFingerEquals(Finger f1, Finger f2){
        for (Bone.Type bt : fingerBoneTypes){

            String error = f1.type() + ":" + bt + " is incorrectly reconstructed!";

            assertVectorEquals(error, f1.bone(bt).nextJoint(),
                    f2.bone(bt).nextJoint());

            assertVectorEquals(error, f1.bone(bt).prevJoint(),
                    f2.bone(bt).prevJoint());

            assertVectorEquals(error, f1.bone(bt).direction(),
                    f2.bone(bt).direction());


            assertVectorEquals(error, f1.bone(bt).center(),
                    f2.bone(bt).center());

        }

        Vector tip1 = f1.tipPosition();

        Vector tip2 = f2.tipPosition();

        assertVectorEquals("Finger Tip Position reconstruction error",
                tip1, tip2);

        tip1 = f1.stabilizedTipPosition();

        tip2 = f2.stabilizedTipPosition();


        assertVectorEquals("Stabilised Tip Position Reconstruction Error",
                tip1, tip2
        );
    }
}
