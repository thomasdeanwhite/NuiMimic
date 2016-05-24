package com.sheffield.leapmotion;

import com.leapmotion.leap.Vector;

/**
 * Created by thomas on 16/05/2016.
 */
public class Quaternion {
    public float w, x, y, z;

    private float angle = 0;
    private Vector axis;

    public Quaternion(float w, float x, float y, float z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;

        if (w > 1){
            normalise();
        }

        float s = (float) Math.sqrt(1- w * w);

        if (Math.abs(s) > 0.0001){
            s = 1f / s;
            x /= s;
            y /= s;
            z /= s;
        } else {
            s = 1f / s;
        }


        angle = (float)Math.PI/2f + 2f * (float)Math.acos(w);
        axis = new Vector(x*s, y*s, z*s);
    }

    public float getAngle(){
        return angle;
    }

    public Vector getAxis(){
        return axis;
    }

    public Quaternion inverse() {
        return new Quaternion(w, -x, -y, -z);
    }

    public float innerProduct(Quaternion q) {
        return x * q.x + y * q.y + z * q.z + w * q.w;
    }

    public Vector rotateVector(Vector v){
        //float a = (float) Math.sin(angle/2);
        return new Vector((w * v.getX()) + (y * v.getZ()) + (z * v.getY()),
                (w * v.getY()) + (x * v.getZ()) + (z * v.getX()),
                (w * v.getZ()) + (y * v.getY()) + (z * v.getX()));
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
        return w + " + " + x + "i + " + y + "j + " + z + "k";
    }

    public float squareMagnitude (){
        return w*w+x*x+y*y+z*z;
    }

    public Quaternion scale(float f){
        return new Quaternion(w * f, x * f, y * f, z * f);
    }

    public Quaternion normalise(){
        float sqMag = squareMagnitude();
        if (Math.abs(1.0 - sqMag) < 2.107342e-08) {
            float scale = 2.0f / (1.0f + sqMag);

            return scale(scale);
        } else {
            return scale((float) (1f / Math.sqrt(sqMag)));
        }
    }

}
