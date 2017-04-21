package com.sheffield.leapmotion.controller;

import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;
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
            long currentTime = (MockSystem.currentTimeMillis() - startTime) - Properties.DELAY_TIME;

            FrameSeedingRunnable fsr = frameSeeding.peek();

            if (fsr == null){
                frameSeeding.poll();
                break;
            }

            long seedTime = fsr.getSeedTime();

            int discFrames = -1;

            while ((seedTime <= currentTime || Properties.PROCESS_PLAYBACK) && frameSeeding.size() > 0) {

                fsr = frameSeeding.poll();
                if (fsr != null){
                    seedTime = fsr.getSeedTime();
                    discFrames++;
                }

                if (Properties.PROCESS_PLAYBACK){
                    break;
                }

            }

            long timeDiff = seedTime - currentTime;

            //if loop wasn't entered
            if (discFrames == 0 && timeDiff > 0 && !Properties
                    .PROCESS_PLAYBACK) {
                try {
                    //sleep for a while
                    Thread.sleep(timeDiff/2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (discFrames > 0){
                discFrames--;
            }

            discardedFrames += discFrames;

            if (fsr != null) {
                try {
                    fsr.run();
                } catch (Throwable t){
                    App.getApp().throwableThrown(t);
                }
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
