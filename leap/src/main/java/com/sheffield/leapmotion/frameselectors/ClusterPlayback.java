package com.sheffield.leapmotion.frameselectors;

import java.util.ArrayList;
import java.util.EmptyStackException;

/**
 * Created by thoma on 07/04/2016.
 */
public class ClusterPlayback {
    private ArrayList<NGramLog> ngLogs;
    private String hand = null;

    public ClusterPlayback(ArrayList<NGramLog> ngLogs){
        this.ngLogs = ngLogs;
    }

    public NGramLog getCurrentNGramLog(int animTime){
        if (ngLogs.size() > 0){
            if (animTime >= ngLogs.get(0).timeSeeded){
                ngLogs.remove(0);
            }
            return ngLogs.get(0);
        }
        throw new EmptyStackException();
    }
}
