package com.sheffield.leapmotion.frame.util;

import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.App;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
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
        //modifier = Math.min(0.999999f, modifier);
        if (quaternions.size() == 1){
            return quaternions.get(0);
        }
        if (quaternions.size() == 2){
            return fadeQuaternions(quaternions.get(0), quaternions.get(1), modifier);
        }
        try {
            quaternions = new ArrayList<Quaternion>(quaternions);

            int i = (int) (modifier * (quaternions.size()-1));

            float size = quaternions.size()-1;

            modifier = modifier - (i * (1f/size));

            modifier *= size;

            Quaternion q1 = quaternions.get(i).normalise();
            Quaternion q2 = quaternions.get(i + 1).normalise();

            Quaternion qr = q1;

            if (!q1.equals(q2)){
                qr = fadeQuaternions(q1, q2, modifier).normalise();
            }

            return qr;

        } catch (IllegalArgumentException e) {
            App.out.println(e.getLocalizedMessage());
            return quaternions.get(0);
        }
    }

    public static float angleBetween(Quaternion q1, Quaternion q2){
        float angle = q1.innerProduct(q2.inverse());

//        if (angle > Math.PI){
//            angle = (float) (Math.PI - angle);
//        }

        if (angle < 0){
            angle = -angle;
        }

        if (angle > Math.PI){
            angle = (float)(2d*Math.PI - angle);
        }

        return angle;
    }

    public static Quaternion fadeQuaternions(Quaternion q1, Quaternion q2, float modifier){

//        if (q1.equals(q2)){
//            return q1;
//        }

        q1 = q1.normalise();
        q2 = q2.normalise();


        float cosAngle = angleBetween(q1, q2);

        // are they facing same direction?
        if (Math.abs(Math.cos(cosAngle)) >= 0.99999){
            return q1;
        }

        float angle = 2f * (float) Math.acos(cosAngle);

        // is q2 the rPi flip or q1? (same direction)
        float sinTheta = (float) Math.sin(angle);

        if (Math.abs(sinTheta) <= 0.00001){
            Quaternion q = new Quaternion((q1.w + q2.w) * 0.5f,
                    (q1.x + q2.x) * 0.5f,
                    (q1.y + q2.y) * 0.5f,
                    (q1.z + q2.z) * 0.5f);

            return q.normalise();
        }


        float ra = (float) (Math.sin((1f-modifier) * angle) / sinTheta);
        float rb = (float) (Math.sin(modifier * angle) / sinTheta);

        float w = q1.w * ra + q2.w * rb;
        float x = q1.x * ra + q2.x * rb;
        float y = q1.y * ra + q2.y * rb;
        float z = q1.z * ra + q2.z * rb;

        return new Quaternion(w, x, y, z).normalise();



    }


    public static Quaternion toQuaternion (Vector[] vs){
        float[][] m = new float[3][3];

        for (int i = 0; i < vs.length; i++){
            m[i][0] = vs[i].getX();
            m[i][1] = vs[i].getY();
            m[i][2] = vs[i].getZ();
        }

        if (vs.length != 3){
            throw new IllegalArgumentException("Vector[] should be of length 3");
        }

        float w = 0, x = 0, y = 0, z = 0;

            w = (float) (Math.sqrt(1f + m[0][0] + m[1][1] + m[2][2]) / 2f);

            float w4 = 4 * w;

            x = (m[2][1] - m[1][2]) / w4;
            y = (m[0][2] - m[2][0]) / w4;
            z = (m[1][0] - m[0][1]) / w4;
        return new Quaternion(w, x, y, z).normalise();
    }

    public static void main(String[] args){

        final ArrayList<Quaternion> qs = new ArrayList<Quaternion>();

        qs.add(new Quaternion(1,0,0,0));
        qs.add(new Quaternion(0,0,0,1));
        qs.add(new Quaternion(0,0,1,0));
        qs.add(new Quaternion(0,1,0,0));
        qs.add(new Quaternion(0.7071f,0,0.7071f,0));


        final int SPOT = 10;
        final int SCALE = 300;

        Quaternion q1 = qs.get(0);

        for (int i = 1; i < qs.size(); i++){
            q1 = q1.multiply(qs.get(i));
        }

        final Quaternion q = q1;


        JFrame frame = new JFrame("Quaternion Plotter"){

            private int o = 0;

            private static final int MAX_O = 500;

            @Override
            public void paintComponents(Graphics g) {
                jp.setDoubleBuffered(true);
//                jp.revalidate();
//                jp.repaint();
                jp.paintComponents(null);
            }

            @Override
            public void setSize(int width, int height) {
                super.setSize(width, height);
                jp.setSize(width, height);
            }

            private JPanel jp = new JPanel(){

                private BufferedImage bi;

                @Override
                public void paintComponents(Graphics g) {
                    if (g == null){
                        g = getContentPane().getGraphics();
                    }

                    //super.paintComponents(g);

                    Graphics2D g2d = (Graphics2D) g;

                    if (bi != null) {
                        g2d.drawImage(bi, 0, 0, Color.BLACK, null);
                    }

                    bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);


                    g2d = bi.createGraphics();


                    g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));

                    g2d.clearRect(0, 0, getWidth(), getHeight());

                    g2d.setColor(Color.BLUE);
                    g2d.setBackground(Color.WHITE);
                    g2d.setStroke(new BasicStroke(3));

                    int counter = 0;

                    final int MAX = 100;

                    Vector offset = new Vector(getWidth()/2, getHeight()/2, 0f);
                    Vector[] drawables = new Vector[]{
                            //new Vector(0f, -SCALE, 0f),
                            new Vector(0f, SCALE, 0f),
                            new Vector(SCALE, 0f, 0f),
                            new Vector(0f, 0f, SCALE),
                            //new Vector(SCALE, SCALE, 0f),

                    };

                    for (int i = o; i < o + MAX; i++){
                        for (Vector draw : drawables) {
                            float m1 = i / (float) (MAX_O - o);
                            float m2 = (i + 1) / (float) (MAX_O - o);
                            Quaternion q1 = fadeQuaternions(qs, m1);
                            Quaternion q2 = fadeQuaternions(qs, m2);

                            Vector v1 = q1.rotateVector(draw);
                            Vector v2 = q2.rotateVector(draw);

                            g2d.setColor(Color.RED);

                            g2d.setStroke(new BasicStroke((float) (30f * Math.pow((v1.getZ() + SCALE) / (SCALE * 2), 2))));

                            g2d.drawLine((int) (offset.getX() + v1.getX()),
                                    (int) (offset.getY() + v1.getY()),
                                    (int) (offset.getX() + v2.getX()),
                                    (int) (offset.getY() + v2.getY()));

                            g2d.setColor(Color.GREEN);


                            Quaternion qs1 = fadeQuaternions(new Quaternion(1f, 0f, 0f, 0f), q, m1);
                            Quaternion qs2 = fadeQuaternions(new Quaternion(1f, 0f, 0f, 0f), q, m2);
                            v1 = qs1.rotateVector(draw);
                            v2 = qs2.rotateVector(draw);

                            g2d.setStroke(new BasicStroke((float) (30f * Math.pow((v1.getZ() + SCALE) / (SCALE * 2), 2))));

                            g2d.drawLine((int) (offset.getX() + v1.getX()),
                                    (int) (offset.getY() + v1.getY()),
                                    (int) (offset.getX() + v2.getX()),
                                    (int) (offset.getY() + v2.getY()));
                        }
                    }


                    g2d.setPaint(Color.BLACK);
                    g2d.fillRect((int) (offset.getX() -(SPOT/2)), (int) (offset.getY() -(SPOT/2)), SPOT, SPOT);
                    g2d.drawString("O[0, 0]", offset.getX() + ((3*SPOT)/4), offset.getY());

                    g2d.setStroke(new BasicStroke(10f));

                    for (Vector draw : drawables) {
                        g2d.setPaint(Color.GRAY);
                        g2d.drawLine((int)offset.getX(), (int)offset.getY(), (int)(offset.getX() + draw.getX()), (int)(offset.getY() + draw.getY()));

                        g2d.setPaint(Color.BLACK);
                        g2d.drawString((counter++) + "", offset.getX() + draw.getX() + ((3*SPOT)/4), offset.getY() + draw.getY());

                        g2d.setPaint(Color.DARK_GRAY);
                        g2d.fillRect((int) (offset.getX() + draw.getX()-(SPOT/2)), (int) (offset.getY() + draw.getY()-(SPOT/2)), SPOT, SPOT);
                    }

                    g2d.dispose();

                    o++;

                    if (o >= MAX + MAX_O){
                        o = 0;
                    }

                }
            };


        };

        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 1000);


        while(true){
            frame.paintComponents(null);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
