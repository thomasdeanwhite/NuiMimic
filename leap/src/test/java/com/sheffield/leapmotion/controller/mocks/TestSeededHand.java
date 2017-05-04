package com.sheffield.leapmotion.controller.mocks;

import com.leapmotion.leap.*;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.frame.generators.gestures.RandomGestureHandler;
import com.sheffield.leapmotion.frame.util.Quaternion;
import com.sheffield.leapmotion.util.Serializer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static com.sheffield.leapmotion.TestUtils.assertFingerEquals;
import static com.sheffield.leapmotion.TestUtils.assertFingerNotEquals;
import static org.junit.Assert.assertEquals;

/**
 * Created by thomas on 21/02/2017.
 */
public class TestSeededHand {

    @Before
    public void setup(){
        System.loadLibrary("LeapJava");
    }

    @Ignore
    public void testRandomHandSerialisation(){
        SeededFrame f1 = new SeededFrame(Frame.invalid());
        SeededHand h1 = HandFactory.createRandomHand(f1, "h1");

        //TODO: Test serialising and deserialising hands (HandFactory
        // .handFromString?)

        String s = null;//Serializer.(f1);

        Frame f2 = Serializer.sequenceFromJson(s);

        Hand h2 = f2.hand(0);

        for (Finger.Type ft : Finger.Type.values()){
            Finger fg1 = null;

            for (Finger fg : h1.fingerList){
                if (fg.type().equals(ft)){
                    fg1 = fg;
                    break;
                }
            }

            Finger fg2 = null;

            for (Finger fg : h2.fingers()){
                if (fg.type().equals(ft)){
                    fg2 = fg;
                    break;
                }
            }
            for (Bone.Type bt : Bone.Type.values()){
                Bone b1 = fg1.bone(bt);
                Bone b2 = fg2.bone(bt);
                assertEquals(b1.nextJoint(), b2.nextJoint());
                assertEquals(b1.prevJoint(), b2.prevJoint());
                assertEquals(b1.center(), b2.center());
                assertEquals(b1.direction(), b2.direction());
            }
        }
    }

    @Test
    public void testCircleGesture(){
        Frame f = new SeededFrame(SeededController.newFrame());
        SeededHand sh = HandFactory.createRandomHand(f, "h1");

        RandomGestureHandler circleGesture = new RandomGestureHandler(){
            @Override
            public String getNextGesture() {
                return Gesture.Type.TYPE_CIRCLE.name();
            }
        };

        circleGesture.tick(0);

        Properties.FRAME_SELECTION_STRATEGY = Properties.FrameSelectionStrategy.EMPTY;

        GestureList gl = circleGesture.handleFrame(f, new Controller());

        assertEquals(Gesture.Type.TYPE_CIRCLE, gl.get(0).type());
    }

    @Ignore
    public void testCircleGestureSetup(){
        //TODO: THIS TEST IS FLAKY!
        Frame f = new SeededFrame(SeededController.newFrame());
        SeededHand sh = HandFactory.createRandomHand(f, "h1");

        HandFactory.injectHandIntoFrame(f, sh);

        sh.setOrigin(new Vector(10, 10, 10));

        Vector newCentre = ((SeededFinger)sh.pointables()
                .frontmost()).tipPosition();

//        ((SeededBone)((SeededFinger)sh.pointables().frontmost()).bone(Bone.Type
//                .TYPE_DISTAL)).nextJoint = newCentre;

        RandomGestureHandler circleGesture = new RandomGestureHandler(){
            @Override
            public String getNextGesture() {
                return Gesture.Type.TYPE_CIRCLE.name();
            }
        };

        circleGesture.tick(0);

        GestureList gl = circleGesture.handleFrame(f, new Controller());

        Vector actualCentre = ((SeededGesture)gl.get(0)).circleGesture.center();

        assertEquals(newCentre.getX(), actualCentre.getX(), 0.00001);
        assertEquals(newCentre.getY(), actualCentre.getY(), 0.00001);
        assertEquals(newCentre.getZ(), actualCentre.getZ(), 0.00001);
    }

    @Test
    public void testCopy(){
        SeededFrame f1 = new SeededFrame(Frame.invalid());
        SeededHand h1 = HandFactory.createRandomHand(f1, "h1");

        SeededHand h2 = h1.copy();

        for (Finger f : h1.fingers()){
            Finger f2 = null;

            for (Finger fi : h2.fingers()){
                if (fi.type().equals(f.type())){
                    f2 = fi;
                    break;
                }
            }

            assertFingerEquals(f, f2);
        }
    }


    @Test
    public void testDeepCopy(){
        SeededFrame f1 = new SeededFrame(Frame.invalid());
        SeededHand h1 = HandFactory.createRandomHand(f1, "h1");

        SeededHand h2 = h1.copy();

        h1.setRotation(new Quaternion((float)Math.PI/4, 0, (float)Math.PI/4, 0));

        for (Finger f : h1.fingers()){
            Finger f2 = null;

            for (Finger fi : h2.fingers()){
                if (fi.type().equals(f.type())){
                    f2 = fi;
                    break;
                }
            }

            assertFingerNotEquals(f, f2);
        }
    }

}
