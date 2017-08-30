package com.sheffield.leapmotion.controller;

import com.leapmotion.leap.Frame;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.Properties;
import com.sheffield.leapmotion.controller.mocks.SeededFrame;
import com.sheffield.leapmotion.instrumentation.MockSystem;

import java.util.ArrayList;
import java.util.List;
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
    private long maxTime = 0;

    private List<Frame> frameCleanup;

    private int discardedFrames = 0;

    private Thread currentThread;

    public FrameSeedingRunnableQueue(){
        running = false;
    }

    public void start(){

        frameCleanup = new ArrayList<>();

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

            if (currentTime > maxTime){
                maxTime = currentTime;
            }

            FrameSeedingRunnable fsr = frameSeeding.peek();

            if (fsr == null){
                frameSeeding.poll();
                break;
            }

            long seedTime = fsr.getSeedTime();

            int discFrames = -1;

            if (Properties.PROCESS_PLAYBACK){
                fsr = frameSeeding.poll();
            } else {

                while ((seedTime <= currentTime) && frameSeeding.size() > 0) {

                    fsr = frameSeeding.poll();
                    if (fsr != null) {
                        seedTime = fsr.getSeedTime();
                        discFrames++;
                    }

                }

                long timeDiff = seedTime - currentTime;

                //if loop wasn't entered
                if (discFrames == 0 && timeDiff > 0) {
                    try {
                        //sleep for a while
                        Thread.sleep(timeDiff);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (discFrames > 0){
                    discFrames--;
                }

                discardedFrames += discFrames;
            }

            if (fsr != null) {
                try {
                    fsr.run();
//                    fsr.destroy();
                    frameCleanup.add(frameCleanup.size(), fsr.getNext());
                } catch (Throwable t){
                    App.getApp().throwableThrown(t);
                }
            }

            if (frameCleanup.size() > Properties.MAX_LOADED_FRAMES && (seedTime - (frameCleanup.get(0).timestamp()/1000) > 1000)){
                Frame f = frameCleanup.remove(0);
                if (f instanceof SeededFrame){
                    SeededController.getSeededController().registerFrameForDeletion((SeededFrame)f);
                } else {
                    f.delete();
                }
            }

        }
        running = false;
        currentThread = null;
    }

    public void addFrameSeedTask(FrameSeedingRunnable r){

        assert r != null;

        frameSeeding.offer(r);

        if (r.getSeedTime() > maxTime){
            maxTime = r.getSeedTime();
        }

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

    public long lastTimestamp(){
        return maxTime;
    }
}
