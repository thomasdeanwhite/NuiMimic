package com.sheffield.leapmotion;

import com.leapmotion.leap.Vector;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by thomas on 13/05/2016.
 */
public class QuaternionHelper {

    public static Vector[] fadeMatrices(Vector[] v1, Vector[] v2, float modifier){
        try {
            Quaternion q1 = toQuaternion(v1);
            Quaternion q2 = toQuaternion(v2);

            Quaternion qr = fadeQuaternions(q1, q2, modifier);

            return qr.toMatrix();

        } catch (IllegalArgumentException e){
            App.out.println(e.getLocalizedMessage());
            return v1;
        }
    }

    public static Vector[] fadeMatrices(ArrayList<Vector[]> vectors, float modifier) {
        try {
            while (vectors.size() > 1) {
                for (int i = 0; i < vectors.size() - 1; i++) {
                    Vector[] v1 = vectors.get(i);
                    Vector[] v2 = vectors.get(i + 1);

                    Quaternion q1 = toQuaternion(v1);
                    Quaternion q2 = toQuaternion(v2);

                    Quaternion qr = fadeQuaternions(q1, q2, modifier);

                    vectors.remove(i);
                    vectors.add(i, qr.toMatrix());
                }
                vectors.remove(vectors.size() - 1);
            }

            return vectors.get(0);

        } catch (IllegalArgumentException e) {
            App.out.println(e.getLocalizedMessage());
            return vectors.get(0);
        }
    }
//
//    public static Quaternion fadeQuaternions(ArrayList<Quaternion> quaternions, float modifier) {
//        try {
//            quaternions = new ArrayList<Quaternion>(quaternions);
//            while (quaternions.size() > 1) {
//                for (int i = 0; i < quaternions.size() - 1; i++) {
//                    Quaternion q1 = quaternions.get(i).normalise();
//                    Quaternion q2 = quaternions.get(i + 1).normalise();
//
//                    Quaternion qr = fadeQuaternions(q1, q2, modifier);
//
//                    quaternions.remove(i);
//                    quaternions.add(i, qr);
//                }
//                quaternions.remove(quaternions.size() - 1);
//            }
//
//            return quaternions.get(0).normalise();
//
//        } catch (IllegalArgumentException e) {
//            App.out.println(e.getLocalizedMessage());
//            return quaternions.get(0);
//        }
//    }

    public static Quaternion fadeQuaternions(ArrayList<Quaternion> quaternions, float modifier) {
        if (quaternions.size() == 2){
            return fadeQuaternions(quaternions.get(0), quaternions.get(1), modifier);
        }
        try {
            quaternions = new ArrayList<Quaternion>(quaternions);
            modifier = Math.min(0.999999f, modifier);

            int i = (int) (modifier * (quaternions.size()-1));

            float size = quaternions.size()-1;

            modifier = modifier - (i * (1f/size));

            modifier *= size;

            Quaternion q1 = quaternions.get(i).normalise();
            Quaternion q2 = quaternions.get(i + 1).normalise();

            Quaternion qr = fadeQuaternions(q1, q2, modifier);

            return qr;

        } catch (IllegalArgumentException e) {
            App.out.println(e.getLocalizedMessage());
            return quaternions.get(0);
        }
    }

    public static float angleBetween(Quaternion q1, Quaternion q2){
        float angle = q1.innerProduct(q2.inverse());

        if (angle > Math.PI){
            angle = (float) (2*Math.PI - angle);
        }

        return angle;
    }

    public static Quaternion fadeQuaternions(Quaternion q1, Quaternion q2, float modifier){
        float cosAngle = angleBetween(q1, q2);

        // are they facing same direction?
        if (Math.abs(cosAngle) >= 1){
            return q1;
        }

        float angle = (float) Math.acos(cosAngle);

        float sinHalfAngle = (float) Math.sqrt(1f - cosAngle*cosAngle);

        // is q2 the rPi flip or q1? (same direction)
        if (Math.abs(sinHalfAngle) <= 0.001){
            Quaternion q = new Quaternion((q1.w + q2.w) * 0.5f,
                    (q1.x + q2.x) * 0.5f,
                    (q1.y + q2.y) * 0.5f,
                    (q1.z + q2.z) * 0.5f);

            return q;
        }

        float ra = (float) (Math.sin((1f-modifier) * angle) / sinHalfAngle);
        float rb = (float) (Math.sin(modifier * angle) / sinHalfAngle);

        return new Quaternion(q1.w * ra + q2.w * rb,
                q1.x * ra + q2.x * rb,
                q1.y * ra + q2.y * rb,
                q1.z * ra + q2.z * rb).normalise();



    }


    public static Quaternion toQuaternion (Vector[] vs){
        float[][] m = new float[3][3];

        for (int i = 0; i < vs.length; i++){
            m[0][i] = vs[i].getX();
            m[1][i] = vs[i].getY();
            m[2][i] = vs[i].getZ();
        }

        if (vs.length != 3){
            throw new IllegalArgumentException("Vector[] should be of length 3");
        }

        float w=0, x=0, y=0, z=0;

        w = (float) (Math.sqrt(1f + m[0][0] + m[1][1] + m[2][2]) / 2f);

        float w4 = 4 * w;

        x = (m[2][1] - m[1][2]) / w4;
        y = (m[0][2] - m[2][0]) / w4;
        z = (m[1][0] - m[0][1]) / w4;

        return new Quaternion(w,x,y,z).normalise();
    }

    public static void main(String[] args){
        JFrame frame = new JFrame("Quaternion Plotter"){

            @Override
            public void paintComponents(Graphics g) {
                if (g == null){
                    g = getGraphics();
                }
                Graphics2D g2d = (Graphics2D) g;
                g2d.clearRect(0, 0, getWidth(), getHeight());

                g2d.setColor(Color.BLUE);
                g2d.setBackground(Color.WHITE);
                g2d.setStroke(new BasicStroke(10));

                ArrayList<Quaternion> qs = new ArrayList<Quaternion>();

                qs.add(new Quaternion(1,0,0,0));
                qs.add(new Quaternion(0.7071f,0,0,0.7071f));
                qs.add(new Quaternion(0,0,0,1));
                qs.add(new Quaternion(0.7071f,0,0,-0.7071f));
                qs.add(new Quaternion(1,0,0,0));

                g2d.setPaint(Color.DARK_GRAY);

                final int SPOT = 30;
                final int SCALE = 300;

                int counter = 0;

                Vector offset = new Vector(getWidth()/2, getHeight()/2, 0f);
                Vector draw = new Vector(0f, -SCALE, 0f);

                for (Quaternion q : qs){
                    g2d.drawString((counter++) + "", offset.getX() + SCALE * q.x + ((3*SPOT)/4), offset.getY() + SCALE * q.y);
                    g2d.fillRect((int) (offset.getX() + (SCALE * q.x)-(SPOT/2)), (int) (offset.getY() + (SCALE * q.y)-(SPOT/2)), SPOT, SPOT);
                }

                g2d.setColor(Color.RED);

                for (int i = 0; i < 500; i++){
                    float m1 = i / (float) 500;
                    float m2 = (i+1) / (float) 500;
                    Quaternion q1 = fadeQuaternions(qs, m1);
                    Quaternion q2 = fadeQuaternions(qs, m2);

                    Vector v1 = q1.rotateVector(draw);
                    Vector v2 = q2.rotateVector(draw);

                    g2d.drawLine((int) (offset.getX() + v1.getX()),
                            (int) (offset.getY() + v1.getY()),
                            (int) (offset.getX() + v2.getX()),
                            (int) (offset.getY() + v2.getY()));
                }

            }
        };

        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 1000);
        frame.paintComponents(null);
    }

}
