package com.sheffield.leapmotion.controller;

import com.leapmotion.leap.Frame;
import com.sheffield.leapmotion.controller.listeners.FrameSwitchListener;

import java.util.Comparator;

/**
 * Created by thomas on 29/03/2017.
 */
public class FrameSeedingRunnable implements Runnable, Comparable {

    private FrameSwitchListener listener;
    private Frame next;
    private Frame last;

    public Frame getNext() {
        return next;
    }

    private long seedTime = 0;

    public FrameSeedingRunnable (FrameSwitchListener fsl,
                                 Frame next,
                                 Frame last,
                                 long seedTime){
        listener = fsl;
        this.next = next;
        this.last = last;

        assert(next.timestamp() > last.timestamp());

        this.seedTime = seedTime;
    }

    @Override
    public void run() {
        listener.onFrameSwitch(last, next);
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof  FrameSeedingRunnable){

                return (int)(seedTime - ((FrameSeedingRunnable)o).seedTime);
        }

        return 1;
    }

    public long getSeedTime (){
            return seedTime;
    }

    public void destroy(){
        next = null;
        last = null;
        listener = null;
    }
}
