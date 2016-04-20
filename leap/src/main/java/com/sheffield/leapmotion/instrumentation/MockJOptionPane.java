package com.sheffield.leapmotion.instrumentation;

import javax.swing.*;
import java.awt.*;
import java.math.BigInteger;
import java.util.Random;

/**
 * Created by thomas on 20/04/2016.
 */
public class MockJOptionPane {

    static Random r = new Random();

    static String randomString(){
        return new BigInteger(130, r).toString(32);
    }

    static int showConfirmDialog(Component p, Object m){
        switch (r.nextInt(3)){
            case 1:
                return JOptionPane.CANCEL_OPTION;
            case 2:
                return JOptionPane.NO_OPTION;
            default:
                return JOptionPane.YES_OPTION;
        }
    }

    static int showConfirmDialog(Component p, Object m, String t, int o){
        return showConfirmDialog(p, m);
    }

    static int showConfirmDialog(Component p, Object m, String t, int o, int mt){
        return showConfirmDialog(p, m);
    }

    static int showConfirmDialog(Component p, Object m, String t, int o, int mt, Icon i){
        return showConfirmDialog(p, m);
    }

    static String showInputDialog(Object m){
        return randomString();
    }

    static String showInputDialog(Component c, Object m){
        return showInputDialog(m);
    }

    static String showInputDialog(Component c, Object m, Object is){
        return showInputDialog(c, m);
    }

    static String showInputDialog(Component c, Object m, String t, int mt){
        return showInputDialog(c, m);
    }

    static Object showInputDialog(Component c, Object m, String t, int mt,
                                  Icon i, Object[] sv, Object isv){
        if (sv.length > 0){
            return sv[r.nextInt(sv.length)];
        }
        return showInputDialog(c, m);
    }

    static String showInputDialog(Object m, Object iv){
        return randomString();
    }

    static void setMessageDialog(Component c, Object m){
        // do nothing
    }

    static void setMessageDialog(Component c, Object m, String t, int mt){
        setMessageDialog(c, m);
    }

    static void setMessageDialog(Component c, Object m, String t, int mt, Icon i){
        setMessageDialog(c, m);
    }

    static int setOptionDialog(Component c, Object m, String t, int ot, int mt,
                                 Icon i, Object[] opts, Object iv){
        return r.nextInt(opts.length);
    }

}
