package com.sheffield.leapmotion.controller;

import com.sheffield.leapmotion.instrumentation.MockSystem;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by thomas on 29/03/2017.
 */
public class FrameSeedingRunnableQueue implements Runnable {

    private Queue<FrameSeedingRunnable> frameSeeding = new
            PriorityQueue<FrameSeedingRunnable>();
    private boolean running;
    private long startTime = 0;

    private Thread currentThread;

    public FrameSeedingRunnableQueue(){
        running = false;
        startTime = MockSystem.currentTimeMillis();
    }

    public void start(){
        if (!running){
            currentThread = new Thread(this);
            currentThread.start();
        }
    }

    @Override
    public void run() {
        running = true;
        while (frameSeeding.size() > 0){
            if (frameSeeding.peek().getSeedTime() < (startTime - MockSystem
                    .currentTimeMillis())) {
                frameSeeding.poll().run();
            }
        }
        running = false;
        currentThread = null;
    }

    public void addFrameSeedTask(FrameSeedingRunnable r){
        frameSeeding.offer(r);

        if (!running){
            start();
        }
    }
}
