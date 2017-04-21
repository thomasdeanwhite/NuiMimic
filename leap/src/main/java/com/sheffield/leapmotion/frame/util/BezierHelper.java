package com.sheffield.leapmotion.frame.util;

import com.leapmotion.leap.Vector;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by thoma on 27/04/2016.
 */
public class BezierHelper {

    public static Vector bezier(ArrayList<Vector> vectors, float modifier){
        ArrayList<Vector> points = new ArrayList<Vector>(vectors);
        while (points.size() > 1){
            ArrayList<Vector> newPoints = new ArrayList<Vector>();
            for (int i = 0; i < points.size()-1; i++){
                Vector v = points.get(i);
                Vector v1 = points.get(i+1);
                Vector result = fadeVector(v, v1, modifier);
                newPoints.add(result);
            }
            points.clear();
            points = newPoints;
        }
        return points.get(0);
    }

    public static Vector fadeVector(Vector prev, Vector next, float modifier){
        return prev.plus(next.minus(prev).times(modifier));
    }

    public static void main(String[] args){
        JFrame frame = new JFrame("Bezier Plotter"){

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

                Vector[] vs = new Vector[]{
                        new Vector(500, 500, 0),
                        new Vector(100, 100, 0),
                        new Vector(500, 100, 0),
                        new Vector(100, 500, 0),
                };

                g2d.setPaint(Color.DARK_GRAY);

                ArrayList<Vector> vectors = new ArrayList<Vector>();

                final int SPOT = 30;

                int counter = 0;

                for (Vector v : vs){
                    vectors.add(v);
                    g2d.drawString((counter++) + "", v.getX() + ((3*SPOT)/4), v.getY());
                    g2d.fillRect((int)v.getX()-(SPOT/2), (int)v.getY()-(SPOT/2), SPOT, SPOT);
                }

                g2d.setColor(Color.RED);

                for (int i = 0; i < 500; i++){
                    float m1 = i / (float) 500;
                    float m2 = (i+1) / (float) 500;
                    Vector v = bezier(vectors, m1);
                    Vector v2 = bezier(vectors, m2);
                    g2d.drawLine((int)v.getX(),
                            (int)v.getY(),
                            (int)v2.getX(),
                            (int)v2.getY());
                }

            }
        };

        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 1000);
        frame.paintComponents(null);
    }

    public static Vector stabiliseVector(Vector position, Vector velocity){
        return position.minus(velocity.divide(velocity.magnitude()/4));
    }
}
