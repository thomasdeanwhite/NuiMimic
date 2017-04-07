package com.sheffield.leapmotion.controller;

import com.sheffield.leapmotion.instrumentation.MockSystem;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by thomas on 29/03/2017.
 */
public class FrameSeedingRunnableQueue implements Runnable {

    private Queue<FrameSeedingRunnable> frameSeeding = new
            ConcurrentLinkedQueue<FrameSeedingRunnable>();
    private boolean running;
    private long startTime = -1;

    private int discardedFrames = 0;

    private Thread currentThread;

    public FrameSeedingRunnableQueue(){
        running = false;
    }

    public void start(){

        if (startTime == -1){
            startTime = MockSystem.currentTimeMillis();
        }

        if (!running){
            currentThread = new Thread(this);
            currentThread.start();
        }
    }

    @Override
    public void run() {
        running = true;
        while (frameSeeding.size() > 0){

            //1 second backlog
            long currentTime = (MockSystem.currentTimeMillis() - startTime) - 1000;

            FrameSeedingRunnable fsr = frameSeeding.peek();

            long seedTime = fsr.getSeedTime();

            while (seedTime <= currentTime && frameSeeding.size() > 0) {

                fsr = frameSeeding.poll();
                if (fsr != null){
                    seedTime = fsr.getSeedTime();
                    discardedFrames++;
                }

            }

            discardedFrames--;

            if (fsr != null) {
                fsr.run();
            }
        }
        running = false;
        currentThread = null;
    }

    public void addFrameSeedTask(FrameSeedingRunnable r){

        assert r != null;

        frameSeeding.offer(r);

        if (!running){
            start();
        }
    }

    public int size(){
        return frameSeeding.size();
    }

    public int discardedFrames(){
        return discardedFrames;
    }
}
