package com.sheffield.leapmotion.instrumentation;

import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;
import java.util.Random;

/**
 * Created by thomas on 20/04/2016.
 */
public class MockJOptionPane {

    public static Random r = new Random();

    public static String randomString(){
        return new BigInteger(130, r).toString(32);
    }

    public static int showConfirmDialog(Component p, Object m){
        switch (r.nextInt(3)){
            case 1:
                return JOptionPane.CANCEL_OPTION;
            case 2:
                return JOptionPane.NO_OPTION;
            default:
                return JOptionPane.YES_OPTION;
        }
    }

    public static int showConfirmDialog(Component p, Object m, String t, int o){
        return showConfirmDialog(p, m);
    }

    public static int showConfirmDialog(Component p, Object m, String t, int o, int mt){
        return showConfirmDialog(p, m);
    }

    public static int showConfirmDialog(Component p, Object m, String t, int o, int mt, Icon i){
        return showConfirmDialog(p, m);
    }

    public static String showInputDialog(Object m){
        return randomString();
    }

    public static String showInputDialog(Component c, Object m){
        return showInputDialog(m);
    }

    public static String showInputDialog(Component c, Object m, Object is){
        return showInputDialog(c, m);
    }

    public static String showInputDialog(Component c, Object m, String t, int mt){
        return showInputDialog(c, m);
    }

    public static Object showInputDialog(Component c, Object m, String t, int mt,
                                  Icon i, Object[] sv, Object isv){
        if (sv != null && sv.length > 0){
            return sv[r.nextInt(sv.length)];
        }
        return showInputDialog(c, m);
    }

    public static String showInputDialog(Object m, Object iv){
        return randomString();
    }

    public static void setMessageDialog(Component c, Object m){
        // do nothing
    }

    public static void setMessageDialog(Component c, Object m, String t, int mt){
        setMessageDialog(c, m);
    }

    public static void setMessageDialog(Component c, Object m, String t, int mt, Icon i){
        setMessageDialog(c, m);
    }

    public static int setOptionDialog(Component c, Object m, String t, int ot, int mt,
                                 Icon i, Object[] opts, Object iv){
        return r.nextInt(opts.length);
    }

}
