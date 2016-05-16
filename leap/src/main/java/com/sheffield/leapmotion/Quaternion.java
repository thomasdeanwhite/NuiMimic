package com.sheffield.leapmotion;

import com.leapmotion.leap.Vector;

/**
 * Created by thomas on 16/05/2016.
 */
public class Quaternion {
    public float w, x, y, z;

    public Quaternion(float w, float x, float y, float z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
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

    public Vector[] toMatrix() {
        float[][] fs = new float[3][3];

        fs[0][0] = 1 - 2 * y * y - 2 * z * z;
        fs[1][1] = 1 - 2 * x * x - 2 * z * z;
        fs[2][2] = 1 - 2 * x * x - 2 * y * y;

        fs[0][1] = 2 * x * y + 2 * z * w;
        fs[0][2] = 2 * x * z - 2 * y * w;
        fs[1][2] = 2 * y * z + 2 * x * w;

        fs[1][0] = 2 * x * y - 2 * z * w;
        fs[2][0] = 2 * x * z + 2 * y * w;
        fs[2][1] = 2 * y * z - 2 * x * w;

        Vector[] vs = new Vector[fs.length];

        for (int i = 0; i < fs.length; i++) {
            //checks for -0
//            if (Float.floatToIntBits(fs[i][0]) == 0x80000000){
//                fs[i][0] = 0;
//            }
//            if (Float.floatToIntBits(fs[i][1]) == 0x80000000){
//                fs[i][1] = 0;
//            }
//            if (Float.floatToIntBits(fs[i][2]) == 0x80000000){
//                fs[i][2] = 0;
//            }
            vs[i] = new Vector(fs[i][0], fs[i][1], fs[i][2]);
        }
        return vs;
    }

    public String toString() {
        return w + "," + x + "," + y + "," + z;
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
