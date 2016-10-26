package com.sheffield.leapmotion.output;

import com.leapmotion.leap.Bone;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Matrix;
import com.leapmotion.leap.Pointable;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.FileHandler;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.Quaternion;
import com.sheffield.leapmotion.controller.SeededController;
import com.sheffield.leapmotion.controller.gestures.ReconstructiveGestureHandler;
import com.sheffield.leapmotion.mocks.HandFactory;
import com.sheffield.leapmotion.mocks.SeededHand;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by thoma on 11/05/2016.
 */
public class TrainingDataVisualiser extends JFrame {

    long lastSwitchTime = 0;
    int currentAnimationTime = 0;
    int currentLabel = 0;

    private HashMap<String, SeededHand> hands;
    private HashMap<String, Vector> vectors;
    private HashMap<String, Quaternion> rotations;

    private ArrayList<String> handLabelStack;
    private ArrayList<String> positionLabelStack;
    private ArrayList<String> rotationLabelStack;
    //private ArrayList<Long> timings;

    private PlotType pt = PlotType.JOINTS;

    public enum PlotType {
        JOINTS, POSITION, ROTATION
    }

    private final int HAND_WIDTH = 100;

    private long startTime = 0;

    private ReconstructiveGestureHandler tpgh;

    private long startSeededTime = 0;
    private long seededTime = 0;

    private int animationTime = 0;

    private int MAX_TILES = 10;


    public TrainingDataVisualiser(String filename){
        super("Cluster Visualiser");

        setVisible(true);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        filename = filename;

        try {
            tpgh = new ReconstructiveGestureHandler(filename);
            App.out.println("* Analysing Clusters");
            lastSwitchTime = 0;
            currentAnimationTime = 0;
            handLabelStack = new ArrayList<String>();
            positionLabelStack = new ArrayList<String>();
            rotationLabelStack = new ArrayList<String>();
            String clusterFile = Properties.DIRECTORY + "/" + filename + ".joint_position_data";
            hands = new HashMap<String, SeededHand>();

            currentLabel = 0;

            String contents = FileHandler.readFile(new File(clusterFile));

            String[] lines = contents.split("\n");
            for (String line : lines) {
                Frame f = SeededController.newFrame();
                SeededHand hand = HandFactory.createHand(line, f);

                hands.put(hand.getUniqueId(), hand);

                HandFactory.injectHandIntoFrame(f, hand);

            }

            String sequenceFile = Properties.DIRECTORY + "/" + filename + ".raw_sequence.joint_position_data";
            String sequenceInfo = FileHandler.readFile(new File(sequenceFile));
            String[] seqData = sequenceInfo.split("\n")[0].split(",");

            for (String s : seqData){
                if (s.length() > 0)
                    handLabelStack.add(s);
            }

            String positionFile = Properties.DIRECTORY + "/" + filename + ".hand_position_data";
            contents = FileHandler.readFile(new File(positionFile));
            lines = contents.split("\n");
            vectors = new HashMap<String, Vector>();
            for (String line : lines) {
                Vector v = new Vector();
                String[] vect = line.split(",");
                v.setX(Float.parseFloat(vect[1]));
                v.setY(Float.parseFloat(vect[2]));
                v.setZ(Float.parseFloat(vect[3]));

                vectors.put(vect[0], v);

            }

            sequenceFile = Properties.DIRECTORY + "/" + filename + ".raw_sequence.hand_position_data";
            sequenceInfo = FileHandler.readFile(new File(sequenceFile));
            seqData = sequenceInfo.split("\n")[0].split(",");

            for (String s : seqData){
                if (s.length() > 0)
                    positionLabelStack.add(s);
            }

            String rotationFile = Properties.DIRECTORY + "/" + filename + ".hand_rotation_data";
            contents = FileHandler.readFile(new File(rotationFile));
            lines = contents.split("\n");
            rotations = new HashMap<String, Quaternion>();
            for (String line : lines) {
                String[] vect = line.split(",");
                Quaternion q = new Quaternion(Float.parseFloat(vect[1]),
                        Float.parseFloat(vect[2]),
                        Float.parseFloat(vect[3]),
                        Float.parseFloat(vect[4])).normalise();

                rotations.put(vect[0], q.inverse());

            }

            sequenceFile = Properties.DIRECTORY + "/" + filename + ".raw_sequence.hand_rotation_data";
            sequenceInfo = FileHandler.readFile(new File(sequenceFile));
            seqData = sequenceInfo.split("\n")[0].split(",");

            for (String s : seqData){
                if (s.length() > 0)
                    rotationLabelStack.add(s);
            }

            setSize(new Dimension(HAND_WIDTH * (1 + MAX_TILES),
                    (HAND_WIDTH * (1 + (hands.keySet().size() / MAX_TILES)))));

        } catch (IOException e){
            e.printStackTrace(App.out);
        }
    }

    @Override
    public void paint(Graphics g){
        int counter = 0;
        Graphics2D g2d = (Graphics2D) g;

        g.setColor(Color.WHITE);

        g.fillRect(0, 0, getWidth(), getHeight());

        switch (pt){
            case JOINTS:
                ArrayList<Hand> hs = new ArrayList<Hand>();

                for (Hand h : hands.values()){
                    hs.add(h);
                }

                hs.sort(new Comparator<Hand>() {
                    @Override
                    public int compare(Hand o1, Hand o2) {
                        int h1 = 0;

                        int h2 = 0;

                        for (Finger f : o1.fingers()){
                            h1 += f.bone(Bone.Type.TYPE_DISTAL).center().getZ();
                        }

                        for (Finger f : o2.fingers()){
                            h2 += f.bone(Bone.Type.TYPE_DISTAL).center().getZ();
                        }

                        if (h1 == h2){
                            return 0;
                        }

                        return h1 > h2 ? 1 : -1;
                    }
                });

                for (Hand s : hs){
                    paintHand(g2d, s, new Vector(HAND_WIDTH * (1 + (counter% MAX_TILES)), HAND_WIDTH + (HAND_WIDTH * (counter / MAX_TILES)), 0));
                    counter++;
                }
                break;
            case POSITION:
                for (String p : vectors.keySet()){
                    paintPosition(g2d, vectors.get(p), new Vector(HAND_WIDTH * (1 + (counter% MAX_TILES)), HAND_WIDTH + (HAND_WIDTH * (counter / MAX_TILES)), 0));
                    counter++;
                }
                break;
            case ROTATION:
                Hand plotted = hands.get(hands.keySet().iterator().next());
                for (String r : rotations.keySet()){
                    Quaternion q = rotations.get(r);
                    q.setBasis((SeededHand)plotted);
                    paintRotation(g2d, plotted, new Vector(HAND_WIDTH * (1 + (counter% MAX_TILES)), HAND_WIDTH + (HAND_WIDTH * (counter / MAX_TILES)), 0));
                    counter++;
                }
                break;
        }
    }

    public void paintHand(Graphics g, Hand h, Vector os) {

        Graphics2D g2d = (Graphics2D) g;

        final float RADIUS = 4;
        final float SCALE = 2;

        Pointable leftMost = h.fingers().leftmost();
        Pointable rightMost = h.fingers().rightmost();
        Pointable frontMost = h.fingers().frontmost();
        for (Finger f : h.fingers()) {
            if (!f.isValid()) {
                continue;
            }

            if (f.equals(frontMost)){
                g2d.setColor(Color.ORANGE);
            } else if (f.equals(leftMost)) {
                g2d.setColor(Color.BLUE);
            } else if (f.equals(rightMost)){
                g2d.setColor(Color.RED);
            } else if (f.isExtended()) {
                g2d.setColor(Color.BLACK);
            }  else {
                g2d.setColor(Color.GRAY);
            }

            for (Bone.Type t : Bone.Type.values()) {
                Bone b = f.bone(t);
                if (!b.isValid()) {
                    continue;
                }
                if (b.isValid()) {

                    Vector prevVect = b.prevJoint().divide(200f).times(HAND_WIDTH);
                    Vector nextVect = b.nextJoint().divide(200f).times(HAND_WIDTH);

                    Vector origin = h.palmPosition();

                    float prevX = os.getX() - prevVect.getZ() + origin.getZ();
                    float prevY = os.getY() - prevVect.getY() + origin.getY();
                    float nextX = os.getX() - nextVect.getZ() + origin.getZ();
                    float nextY = os.getY() - nextVect.getY() + origin.getY();
                    g2d.drawLine((int) prevX, (int) prevY, (int) nextX, (int) nextY);
                    g2d.fillOval((int) (prevX - RADIUS), (int) (prevY - RADIUS), (int) RADIUS * 2,
                            (int) RADIUS * 2);
                    g2d.drawLine((int) prevX, (int) prevY, (int) nextX, (int) nextY);
                    g2d.fillOval((int) (prevX - RADIUS), (int) (prevY - RADIUS), (int) RADIUS * 2,
                            (int) RADIUS * 2);
                }
            }
        }

        g2d.setColor(Color.LIGHT_GRAY);
        int scale = (400 - (int)h.palmPosition().getZ()) / 30;
        float scaleWindowX = (getWidth()/2) / 400f;
        float scaleWindowY = (getHeight()/2) / 400f;
        g2d.fillOval((3*(getWidth() / 4)) + ((int)(h.palmPosition().getX()*scaleWindowX)), getHeight() - ((int)(h.palmPosition().getY()*scaleWindowY)), scale, scale);
        g2d.drawString("(" + (Math.round(10*h.palmPosition().getX())/10f) + ", " + (Math.round(10*h.palmPosition().getY())/10f) + ")",
                (3*(getWidth() / 4)) + ((int)(h.palmPosition().getX()*scaleWindowX)) + scale + 5, getHeight() - ((int)(h.palmPosition().getY()*scaleWindowY)) + scale);

        g2d.drawString(h.fingers().frontmost().tipPosition().toString(),
                (3*(getWidth() / 4)) + ((int)(h.palmPosition().getX()*scaleWindowX)) + scale + 5, getHeight() - ((int)(h.palmPosition().getY()*scaleWindowY)) + (2 * scale));
    }

    public void paintRotation(Graphics g, Hand h, Vector os) {

        Graphics2D g2d = (Graphics2D) g;
        Matrix basis = h.basis();

        Vector v = os;//.plus(new Vector(h.palmNormal().getZ(), h.palmPosition().getY(), 0f));

        final float LINE_SCALE = 50f;

        g.setColor(Color.BLUE);

        //App.out.println(basis.getXBasis());

        int height = getHeight();

        int x = (int)(v.getX() + (-basis.getXBasis().getZ()*LINE_SCALE));
        int y = height - (int)((v.getY() + basis.getXBasis().getY()*LINE_SCALE));

        g.drawLine(x, y, (int)v.getX(), height - (int)v.getY());
        g.drawString("x", x, y);

        g.setColor(Color.GREEN);

        x = (int)(v.getX() + (-basis.getZBasis().getZ()*LINE_SCALE));
        y = height - (int)((v.getY() + basis.getZBasis().getY()*LINE_SCALE));

        g.drawLine((int)v.getX(), height - (int)v.getY(), x, y);

        g.drawString("z", x, y);

        g.setColor(Color.RED);

        x = (int)(v.getX() + (-basis.getYBasis().getZ()*LINE_SCALE));
        y = height - (int)((v.getY() + basis.getYBasis().getY()*LINE_SCALE));

        g.drawLine((int)v.getX(), height - (int)v.getY(), x, y);
        g.drawString("y", x, y);

    }

    public void paintPosition(Graphics g, Vector p, Vector os) {

        Graphics2D g2d = (Graphics2D) g;

        g.setColor(Color.BLUE);

        int height = getHeight();

        int ho2 = HAND_WIDTH / 2;

        g.setColor(Color.GREEN);
        g.drawLine((int)os.getX(), (int)os.getY()- (HAND_WIDTH/3), (int)os.getX(), (int)os.getY() + (HAND_WIDTH/3));

        g.setColor(Color.BLUE);
        g.drawLine((int)os.getX()- (HAND_WIDTH/3), (int)os.getY(), (int)os.getX()+ (HAND_WIDTH/3), (int)os.getY());

        g2d.setColor(Color.LIGHT_GRAY);
        int scale = (400 - (int)p.getZ()) / 30;
        float scaleWindowX = HAND_WIDTH / 400f;
        float scaleWindowY = HAND_WIDTH / 400f;
        g2d.fillOval((int)os.getX() + (int)(p.getX()*scaleWindowX), (int)(os.getY() - p.getY()*scaleWindowY), scale, scale);
        g2d.drawString("(" + (Math.round(10*p.getX())/10f) + ", " + (Math.round(10*p.getY())/10f) + ")",
                (int)os.getX() + ((int)(p.getX()*scaleWindowX)) + scale + 5, getHeight() - ((int)os.getY()) + scale);

    }

}
