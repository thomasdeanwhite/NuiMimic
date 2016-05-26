package com.sheffield.leapmotion;

import com.leapmotion.leap.Vector;
import com.sheffield.leapmotion.mocks.SeededHand;

/**
 * Created by thomas on 16/05/2016.
 */
public class Quaternion {

    public float w, x, y, z;

    private Quaternion inverse;

    public Quaternion(float w, float x, float y, float z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;

        inverse = new Quaternion(w, -x, -y, -z, this);
    }

    private Quaternion(float w, float x, float y, float z, Quaternion inverse) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;

        this.inverse = inverse;
    }

    public Quaternion inverse() {
        return inverse;
    }

    public float innerProduct(Quaternion q) {
        return x * q.x + y * q.y + z * q.z + w * q.w;
    }

    public void setBasis(SeededHand h){
        //normalise(this);
        //Vector[] vs = toMatrix(false);
        //h.setBasis(rotateVector(vs[0]), vs[1], vs[2]);
        Vector[] vs = toMatrix(true);
        h.setBasis(vs[0], vs[1], vs[2]);
//        Vector[] vs = new Vector[]{rotateVector(Vector.xAxis()), rotateVector(Vector.yAxis()), rotateVector(Vector.zAxis())};
//        h.setBasis(new Vector(vs[0].getX(), vs[1].getX(), vs[2].getX()),
//                new Vector(vs[0].getY(), vs[1].getY(), vs[2].getY()),
//                new Vector(vs[0].getZ(), vs[1].getZ(), vs[2].getZ()));

        h.setRotation(this);

    }

    public Vector rotateVector(Vector v){
        //float a = (float) Math.sin(angle/2);
//        return new Vector((w * v.getX()) + (y * v.getZ()) + (z * v.getY()),
//                (w * v.getY()) + (x * v.getZ()) + (z * v.getX()),
//                (w * v.getZ()) + (y * v.getY()) + (z * v.getX()));

        Quaternion q = new Quaternion(0f, v.getX(), v.getY(), v.getZ());
        q = multiply(q).multiply(inverse);
        return new Vector(q.x, q.y, q.z);
    }

    public Quaternion multiply(Quaternion q){
//        Vector v = new Vector(x, y, z);
//        Vector v2 = new Vector(q.x, q.y, q.z);
//        Vector o = v2.times(w).plus(v.times(q.w).plus(v.cross(v2)));
//        return new Quaternion(w*q.w - v.dot(v2), o.getX(), o.getY(), o.getZ());

        return new Quaternion(
                w * q.w - x * q.x - y * q.y - z * q.z,
                x * q.w + w * q.x - z * q.y + y * q.z,
                y * q.w + z * q.x + w * q.y - x * q.z,
                z * q.w - y * q.x + x * q.y + w * q.z);
    }

    public Vector[] toMatrix(boolean rightHanded) {
        float[][] fs = new float[3][3];

        if (rightHanded) {

            fs[0][0] = 1 - 2 * y * y - 2 * z * z;
            fs[1][1] = 1 - 2 * x * x - 2 * z * z;
            fs[2][2] = 1 - 2 * x * x - 2 * y * y;

            fs[0][1] = 2 * x * y + 2 * z * w;
            fs[0][2] = 2 * x * z - 2 * y * w;
            fs[1][2] = 2 * y * z + 2 * x * w;

            fs[1][0] = 2 * x * y - 2 * z * w;
            fs[2][0] = 2 * x * z + 2 * y * w;
            fs[2][1] = 2 * y * z - 2 * x * w;
        } else {
            fs[0][0] = 1 - 2 * y * y - 2 * z * z;
            fs[1][1] = 1 - 2 * x * x - 2 * z * z;
            fs[2][2] = 1 - 2 * x * x - 2 * y * y;

            fs[0][1] = 2 * x * y - 2 * z * w;
            fs[0][2] = 2 * x * z + 2 * y * w;
            fs[1][2] = 2 * y * z - 2 * x * w;

            fs[1][0] = 2 * x * y + 2 * z * w;
            fs[2][0] = 2 * x * z - 2 * y * w;
            fs[2][1] = 2 * y * z + 2 * x * w;
        }

        Vector[] vs = new Vector[fs.length];

        for (int i = 0; i < fs.length; i++) {
            vs[i] = new Vector(fs[i][0], fs[i][1], fs[i][2]);
        }
        return vs;
    }


    /**
     * Deprecated. Please use toMatrix(boolean rightHanded)
     * @return Quaternion as a right handed basis vector
     */
    @Deprecated
    public Vector[] toMatrix() {
        return toMatrix(true);
    }

    public String toString() {
        return w + (x > 0 ? " + " : " - ") + Math.abs(x) + "i " + (y > 0 ? "+ " : "- ") + Math.abs(y) + "j " + (z > 0 ? "+ " : "- ") + Math.abs(z) + "k";
    }

    public String toCsv(){
        return w+","+x+","+y+","+z;
    }

    public float squareMagnitude (){
        return w*w+x*x+y*y+z*z;
    }

    public Quaternion scale(float f){
        w *= f;
        x *= f;
        y *= f;
        z *= f;
        return this;
    }

    public Quaternion normalise(){
        Quaternion q = new Quaternion(w, x, y, z);
        float sqMag = q.squareMagnitude();
        if (Math.abs(1.0 - sqMag) < 2.107342e-08) {
            float scale = 2.0f / (1.0f + sqMag);

            return q.scale(scale);
        } else {
            return q.scale((float) (1f / Math.sqrt(sqMag)));
        }
    }

    private static Quaternion normalise(Quaternion q){
        float sqMag = q.squareMagnitude();
        if (Math.abs(1.0 - sqMag) < 2.107342e-08) {
            float scale = 2.0f / (1.0f + sqMag);

            return q.scale(scale);
        } else {
            return q.scale((float) (1f / Math.sqrt(sqMag)));
        }
    }

}
