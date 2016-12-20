package com.sheffield.leapmotion.instrumentation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventListener;

/**
 * Created by thomas on 12/20/2016.
 */
public class MockTimer extends javax.swing.Timer {

    /**
     * Creates a {@code Timer} and initializes both the initial delay and
     * between-event delay to {@code delay} milliseconds. If {@code delay}
     * is less than or equal to zero, the timer fires as soon as it
     * is started. If <code>listener</code> is not <code>null</code>,
     * it's registered as an action listener on the timer.
     *
     * @param delay    milliseconds for the initial and between-event delay
     * @param listener an initial listener; can be <code>null</code>
     * @see #addActionListener
     * @see #setInitialDelay
     * @see #setRepeats
     */
    public MockTimer(int delay, ActionListener listener) {
        super(delay, listener);
    }

    @Override
    public void addActionListener(ActionListener listener) {
        super.addActionListener(listener);
    }

    @Override
    public void removeActionListener(ActionListener listener) {
        super.removeActionListener(listener);
    }

    @Override
    public ActionListener[] getActionListeners() {
        return super.getActionListeners();
    }

    @Override
    protected void fireActionPerformed(ActionEvent e) {
        super.fireActionPerformed(e);
    }

    @Override
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        return super.getListeners(listenerType);
    }

    @Override
    public void setDelay(int delay) {
        super.setDelay(delay);
    }

    @Override
    public int getDelay() {
        return super.getDelay();
    }

    @Override
    public void setInitialDelay(int initialDelay) {
        super.setInitialDelay(initialDelay);
    }

    @Override
    public int getInitialDelay() {
        return super.getInitialDelay();
    }

    @Override
    public void setRepeats(boolean flag) {
        super.setRepeats(flag);
    }

    @Override
    public boolean isRepeats() {
        return super.isRepeats();
    }

    @Override
    public void setCoalesce(boolean flag) {
        super.setCoalesce(flag);
    }

    @Override
    public boolean isCoalesce() {
        return super.isCoalesce();
    }

    @Override
    public void setActionCommand(String command) {
        super.setActionCommand(command);
    }

    @Override
    public String getActionCommand() {
        return super.getActionCommand();
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public boolean isRunning() {
        return super.isRunning();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public void restart() {
        super.restart();
    }
}
