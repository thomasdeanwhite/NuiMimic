package com.sheffield.leapmotion.controller.mocks;

import com.leapmotion.leap.Arm;
import com.leapmotion.leap.Bone;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.FingerList;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Matrix;
import com.leapmotion.leap.Pointable;
import com.leapmotion.leap.PointableList;
import com.leapmotion.leap.Tool;
import com.leapmotion.leap.ToolList;
import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.frame.util.BezierHelper;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.frame.util.Quaternion;
import com.sheffield.leapmotion.frame.util.QuaternionHelper;

import java.io.Serializable;
import java.util.ArrayList;

public class SeededHand extends Hand implements Serializable {

    protected FingerList fingerList;
    protected Matrix basis;
    protected Vector direction;
    protected Frame frame;
    protected Vector palmNormal;
    protected Vector palmPosition;
    protected Vector palmVelocity;
    protected float palmWidth;
    protected float timeVisible;
    protected boolean isLeft;

    protected float pinchStrength;
    protected float grabStrength;

    protected Quaternion rotation = Quaternion.IDENTITY;

    protected PointableList pointables;
    protected ToolList tools;

    protected int id;
    protected String uniqueId;
    protected Vector stabilizedPalmPosition;

    public void setRotation(Quaternion q){
        rotation = q;

        Vector[] vs = q.toMatrix(true);
        setBasis(vs[0], vs[1], vs[2]);

        setPalmNormal(q.rotateVector(Vector.yAxis().opposite()));


        if (!Properties.SINGLE_DATA_POOL) {

            for (Finger f : fingerList) {
                if (f instanceof SeededFinger) {
                    SeededFinger sf = (SeededFinger) f;
                    sf.rotation = rotation;
                    sf.normalize();
                }
            }
        }
    }

    public SeededHand copy(){
        SeededHand h = new SeededHand();
        h.basis = basis;
        h.direction = direction;
        h.frame = frame;
        h.palmNormal = palmNormal;
        h.palmPosition = palmPosition;
        h.palmVelocity = palmVelocity;
        h.palmWidth = palmWidth;
        h.timeVisible = timeVisible;
        h.isLeft = isLeft;
        h.id = id;
        h.uniqueId = uniqueId;
        SeededFingerList sfl = new SeededFingerList();
        for (Finger f : fingerList) {
            SeededFinger sf = new SeededFinger();
            SeededFinger of = ((SeededFinger)f);
            for (Bone.Type bt : Bone.Type.values()) {
                SeededBone sb = new SeededBone();
                Bone rb = f.bone(bt);
                SeededBone b = (SeededBone) rb;

                sb.type = bt;
                sb.basis = b.basis;
                sb.center = b.center;
                sb.length = b.length();
                sb.nextJoint = b.nextJoint;
                sb.prevJoint = b.prevJoint;
                sb.rotation = b.rotation;
                sb.width = b.width();
                sf.bones.put(bt, sb);
            }
            sf.rotation = of.rotation;
            sf.type = of.type;
            sf.tipPosition = of.tipPosition;
            sf.tipVelocity = of.tipVelocity;
            sf.hand = h;
            sf.stabilizedTipPosition = of.stabilizedTipPosition;
            sf.nStabilizedTipPosition = of.nStabilizedTipPosition;
            sf.normalize();
            sfl.addFinger(sf);
        }
        h.fingerList = sfl;
        return h;
    }

    public Hand fadeHand(SeededHand hand, float modifier) {
        SeededHand h = new SeededHand();
        h.basis = basis;
        h.direction = direction;
        h.frame = frame;
        h.palmNormal = palmNormal;
        h.palmPosition = palmPosition;
        h.palmVelocity = palmVelocity;
        h.palmWidth = palmWidth;
        h.timeVisible = timeVisible;
        h.isLeft = isLeft;
        h.id = id;
        h.uniqueId = uniqueId;
        SeededFingerList sfl = new SeededFingerList();
        for (Finger f : fingerList) {
            SeededFinger sf = new SeededFinger();
            Finger f2 = null;
            for (Finger f2enum : hand.fingers()) {
                if (f2enum.type() == f.type()) {
                    f2 = f2enum;
                    break;
                }
            }
            for (Bone.Type bt : Bone.Type.values()) {
                SeededBone sb = new SeededBone();
                Bone rb = f.bone(bt);
                Bone rb2 = f2.bone(bt);
                if (!(rb instanceof SeededBone && rb2 instanceof SeededBone)) {
                    continue;
                }
                SeededBone b = (SeededBone) rb;
                SeededBone b2 = (SeededBone) rb2;
                sb.type = bt;
                sb.basis = basis;
                sb.center = b.center.plus(b2.center.minus(b.center).times(modifier));
                sb.length = b.length();
                sb.nextJoint = b.nextJoint.plus(b2.nextJoint.minus(b.nextJoint).times(modifier));
                sb.prevJoint = b.prevJoint.plus(b2.prevJoint.minus(b.prevJoint).times(modifier));
                sb.width = b.width();
                sf.bones.put(bt, sb);
            }
            sf.normalize();
            sf.tipPosition = f.tipPosition().plus(sf.tipPosition().minus(f.tipPosition()).divide(Properties.SWITCH_TIME));
            sf.tipVelocity = f2.tipPosition().minus(sf.tipPosition());
            sf.hand = h;
            sfl.addFinger(sf);
        }
        h.fingerList = sfl;
        return h;
    }

    public Vector fadeVector(Vector prev, Vector next, float modifier){
        return prev.plus(next.minus(prev).times(modifier));
    }



    public ArrayList<Vector> createCenterVector(ArrayList<Finger> fingers, Bone.Type bt){
        ArrayList<Vector> center = new ArrayList<Vector>();
        for (Finger fi : fingers){
            center.add(fi.bone(bt).center());
        }
        return center;
    }

    public ArrayList<Vector> createNextVector(ArrayList<Finger> fingers, Bone.Type bt){
        ArrayList<Vector> center = new ArrayList<Vector>();
        for (Finger fi : fingers){
            center.add(fi.bone(bt).nextJoint());
        }
        return center;
    }

    public ArrayList<Vector> createTipPositionVector(ArrayList<Finger> fingers){
        ArrayList<Vector> center = new ArrayList<Vector>();
        for (Finger fi : fingers){
            center.add(fi.tipPosition());
        }
        return center;
    }


    public ArrayList<Vector> createStabilisedTipPositionVector(ArrayList<Finger> fingers){
        ArrayList<Vector> center = new ArrayList<Vector>();
        for (Finger fi : fingers){
            center.add(fi.stabilizedTipPosition());
        }
        return center;
    }

    public ArrayList<Vector> createPrevVector(ArrayList<Finger> fingers, Bone.Type bt){
        ArrayList<Vector> center = new ArrayList<Vector>();
        for (Finger fi : fingers){
            center.add(fi.bone(bt).prevJoint());
        }
        return center;
    }

    public Hand fadeHand(ArrayList<SeededHand> hands, float modifier) {

        if (hands.size() == 0){
            return this;
        }

        if (hands.contains(this)){
            hands.remove(this);
        }

        SeededHand h = new SeededHand();
        h.basis = basis;
        h.direction = direction;
        h.frame = frame;
        h.palmNormal = palmNormal;
        h.palmPosition = palmPosition;
        h.palmVelocity = palmVelocity;
        h.palmWidth = palmWidth;
        h.timeVisible = timeVisible;
        h.isLeft = isLeft;
        h.id = id;
        h.uniqueId = uniqueId;
        ArrayList<Quaternion> qs = new ArrayList<Quaternion>();

        for (SeededHand hs : hands){
            qs.add(hs.rotation);
        }

        h.rotation = QuaternionHelper.fadeQuaternions(qs, modifier);

        SeededFingerList sfl = new SeededFingerList();
        for (Finger f : fingerList) {
            SeededFinger sf = new SeededFinger();

            ArrayList<Finger> fingers = new ArrayList<Finger>();
            fingers.add(f);
            for (Hand hand : hands) {
                Finger f2 = null;
                for (Finger f2enum : hand.fingers()) {

                    if (f2enum.type() == f.type()) {
                        f2 = f2enum;
                        break;
                    }
                }
                fingers.add(f2);
            }
            for (Bone.Type bt : Bone.Type.values()) {

                SeededBone sb = new SeededBone();

                sb.type = bt;
                sb.basis = basis;
                Bone rb = f.bone(bt);
                SeededBone b = (SeededBone) rb;

                sb.center = BezierHelper.bezier(createCenterVector(fingers, bt), modifier);
                sb.length = b.length();
                sb.nextJoint = BezierHelper.bezier(createNextVector(fingers, bt), modifier);
                sb.prevJoint = BezierHelper.bezier(createPrevVector(fingers, bt), modifier);
                sb.width = b.width();
                sf.bones.put(bt, sb);
            }
            sf.rotation = h.rotation;
            sf.normalize();
            sf.tipPosition = BezierHelper.bezier(createTipPositionVector(fingers), modifier);
            final float lastTipNumber = (fingers.size()-1) * Properties.SWITCH_TIME;
            Vector lastTip = BezierHelper.bezier(createTipPositionVector(fingers), 0f);
            sf.tipVelocity = lastTip.divide(lastTipNumber);
            //sf.stabilizedTipPosition = sf.tipPosition;
            sf.hand = h;

            sf.stabilizedTipPosition = BezierHelper.bezier(createStabilisedTipPositionVector(fingers), modifier);

            sf.type = f.type();
            sfl.addFinger(sf);

        }
        h.fingerList = sfl;
        return h;
    }

    public SeededHand() {
        fingerList = new SeededFingerList();
        basis = Matrix.identity();
        direction = Vector.zAxis();
        palmNormal = Vector.yAxis().times(-1);
        palmPosition = Vector.zero();
        palmVelocity = Vector.zero();
        pointables = new PointableList();
        tools = new ToolList();
        rotation = new Quaternion(-1, 0, 0, 0);
    }

    public SeededHand(FingerList fl) {
        this();
        fingerList = fl;
    }

    public SeededHand(Hand hand) {
        this();
        fingerList.append(hand.fingers());
        isLeft = hand.isLeft();
        palmWidth = hand.palmWidth();
    }

    private SeededHand(String s) {

    }

    public void setBasis(Vector x, Vector y, Vector z) {
        basis.setXBasis(x);
        basis.setYBasis(y);
        setPalmNormal(y.opposite());
        basis.setZBasis(z);
        setDirection(z.opposite());

        for (Finger f : fingers()){
            if (f instanceof SeededFinger){
                SeededFinger sf = (SeededFinger) f;
                sf.basis = basis;
                sf.normalize();
            }
        }

//        basis.setYBasis(z);
//        setPalmNormal(z);
//
//        y = new Vector(y.getX(), y.getY()*-1, y.getZ());
//
//        basis.setZBasis(y.opposite());
//        setDirection(y.opposite());
    }

    public void setOrigin(Vector origin) {
        //basis.setOrigin(origin);
        basis.setOrigin(Vector.zero());
        palmPosition = origin;

        if (!Properties.SINGLE_DATA_POOL) {
            for (Finger f : fingers()) {
                if (f instanceof SeededFinger) {
                    SeededFinger sf = (SeededFinger) f;
                    sf.offset = palmPosition;
                    sf.normalize();
                }
            }
        }
    }

    public void setPalmVelocity (Vector v){
        palmVelocity = v;
    }

    public void setRotation(Vector axis, float rotation) {
        //basis = Matrix.identity();
        basis.setRotation(new Vector(axis.getX(), axis.getY(), axis.getZ()).normalized(), rotation);
        //basis.setRotation(Vector.xAxis(), 1.46f);
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setFingerList(FingerList fl) {
        fingerList = fl;
    }

    @Override
    public Arm arm() {
        // TODO Auto-generated method stub
        return Arm.invalid();
    }

    @Override
    public Matrix basis() {
        // TODO Auto-generated method stub
        return basis;
    }

    @Override
    public float confidence() {
        // TODO Auto-generated method stub
        return 1f;
    }

    @Override
    public synchronized void delete() {
        // TODO Auto-generated method stub
        // super.delete();
    }

    @Override
    public Vector direction() {
        // TODO Auto-generated method stub
        return direction;
        //return basis.transformDirection(direction);
    }

    public void setDirection(Vector d){
        direction = d;
    }

    @Override
    public boolean equals(Hand arg0) {
        if (arg0 instanceof SeededHand) {
            return uniqueId.equals(((SeededHand) arg0).uniqueId);
        }
        return false;
    }

    @Override
    protected void finalize() {
        // TODO Auto-generated method stub
        // super.finalize();
    }

    @Override
    public Finger finger(int arg0) {
        // TODO Auto-generated method stub
        return fingerList.get(arg0);
    }

    @Override
    public FingerList fingers() {
        // TODO Auto-generated method stub
        return fingerList;
    }

    @Override
    public Frame frame() {
        // TODO Auto-generated method stub
        return frame;
    }

    @Override
    public float grabStrength() {
        // TODO Auto-generated method stub
        return grabStrength;
    }

    @Override
    public int id() {
        // TODO Auto-generated method stub
        return id;
    }

    @Override
    public boolean isLeft() {
        // TODO Auto-generated method stub
        return isLeft;
    }

    @Override
    public boolean isRight() {
        // TODO Auto-generated method stub
        return !isLeft;
    }

    @Override
    public boolean isValid() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public Vector palmNormal() {
        // TODO Auto-generated method stub
        return palmNormal;
    }

    public void setPalmNormal(Vector p){
        palmNormal = p;
    }

    @Override
    public Vector palmPosition() {
        // TODO Auto-generated method stub
        return palmPosition;
    }

    @Override
    public Vector palmVelocity() {
        // TODO Auto-generated method stub
        return palmVelocity;
    }

    @Override
    public float palmWidth() {
        // TODO Auto-generated method stub
        return palmWidth;
    }

    @Override
    public float pinchStrength() {
        // TODO Auto-generated method stub
        return pinchStrength;
    }

    @Override
    public Pointable pointable(int arg0) {
        // TODO Auto-generated method stub
        return Pointable.invalid();
    }

    @Override
    public PointableList pointables() {
        // TODO Auto-generated method stub
        SeededPointableList pl = new SeededPointableList();
        for (Finger f : fingerList){//.extended()) {
            pl.addPointable(f);
        }
        return pl;
    }

    @Override
    public float rotationAngle(Frame arg0, Vector arg1) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float rotationAngle(Frame arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Vector rotationAxis(Frame arg0) {
        // TODO Auto-generated method stub
        return Vector.zero();
    }

    @Override
    public Matrix rotationMatrix(Frame arg0) {
        // TODO Auto-generated method stub
        return Matrix.identity();
    }

    @Override
    public float rotationProbability(Frame arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float scaleFactor(Frame arg0) {
        // TODO Auto-generated method stub
        return 1;
    }

    @Override
    public float scaleProbability(Frame arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Vector sphereCenter() {
        // TODO Auto-generated method stub
        return Vector.zero();
    }

    @Override
    public float sphereRadius() {
        // TODO Auto-generated method stub
        return 1f;
    }

    @Override
    public Vector stabilizedPalmPosition() {
        // TODO Auto-generated method stub

        return stabilizedPalmPosition;
    }

    @Override
    public float timeVisible() {
        // TODO Auto-generated method stub
        throw new NotImplementedException();// super.timeVisible();
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return "Seeded Hand " + uniqueId;
    }

    @Override
    public Tool tool(int arg0) {
        // TODO Auto-generated method stub
        return Tool.invalid();
    }

    @Override
    public ToolList tools() {
        // TODO Auto-generated method stub
        return tools;
    }

    @Override
    public Vector translation(Frame arg0) {
        // TODO Auto-generated method stub
        return Vector.zero();
    }

    @Override
    public float translationProbability(Frame arg0) {
        // TODO Auto-generated method stub
        return 0f;
    }

    @Override
    public Vector wristPosition() {
        // TODO Auto-generated method stub
        return basis.transformPoint(Vector.zAxis().times(-1));
    }

}
