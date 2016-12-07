package com.sheffield.leapmotion.frame.playback;

import com.sheffield.output.Csv;

import java.util.ArrayList;
import java.util.EmptyStackException;

/**
 * Created by thoma on 07/04/2016.
 */
public class ClusterPlayback {
    private ArrayList<NGramLog> ngLogs;
    private String hand = null;
    public Csv getCsv() {
        return new Csv();
    }
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

    public boolean willExpire(int animTime){
        if (ngLogs.size() > 0){
            if (animTime >= ngLogs.get(0).timeSeeded){
                return true;
            }
        }
        return false;
    }

    public void next(){
        ngLogs.remove(0);
    }

    public int length (){
        return ngLogs.size();
    };
}
