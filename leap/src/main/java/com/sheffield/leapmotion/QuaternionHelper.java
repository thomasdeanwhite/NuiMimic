package com.sheffield.leapmotion;

import com.leapmotion.leap.Vector;

import java.util.ArrayList;

/**
 * Created by thomas on 13/05/2016.
 */
public class QuaternionHelper {

    public static class Quaternion {
        public float w, x, y, z;

        public Quaternion (float w, float x, float y, float z){
            this.w = w;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Quaternion inverse () {
            return new Quaternion(w, -x, -y, -z);
        }

        public float innerProduct (Quaternion q){
            return x*q.x + y*q.y + z*q.z + w*q.w;
        }

        public Vector[] toMatrix(){
            float[][] fs = new float[3][3];

            fs[0][0] = 1 - 2*y*y - 2*z*z;
            fs[1][1] = 1 - 2*x*x - 2*z*z;
            fs[2][2] = 1 - 2*x*x - 2*y*y;

            fs[0][1] = 2*x*y + 2*z*w;
            fs[0][2] = 2*x*z - 2*y*w;
            fs[1][2] = 2*y*z + 2*x*w;

            fs[1][0] = 2*x*y - 2*z*w;
            fs[2][0] = 2*x*z + 2*y*w;
            fs[2][1] = 2*y*z - 2*x*w;

            Vector[] vs = new Vector[fs.length];

            for (int i = 0; i < fs.length; i++){
                vs[i] = new Vector(fs[i][0], fs[i][1], fs[i][2]);
            }
            return vs;
        }


    }

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

    public static float angleBetween(Quaternion q1, Quaternion q2){
        return q1.innerProduct(q2);
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
                q1.z * ra + q2.z * rb);



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

        return new Quaternion(w,x,y,z);
    }

}
