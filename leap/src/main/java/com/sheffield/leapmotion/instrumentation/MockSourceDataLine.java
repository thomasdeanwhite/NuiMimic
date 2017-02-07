package com.sheffield.leapmotion.instrumentation;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Control;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Created by Thomas on 07-02-2017.
 */
public class MockSourceDataLine implements SourceDataLine {


    AudioFormat af;

    public MockSourceDataLine(AudioFormat f){
        af = f;
    }


    @Override
    public void open(AudioFormat format, int bufferSize) throws LineUnavailableException {

    }

    @Override
    public void open(AudioFormat format) throws LineUnavailableException {

    }

    @Override
    public int write(byte[] b, int off, int len) {
        return 0;
    }

    @Override
    public void drain() {

    }

    @Override
    public void flush() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public AudioFormat getFormat() {
        return null;
    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public int available() {
        return 0;
    }

    @Override
    public int getFramePosition() {
        return 0;
    }

    @Override
    public long getLongFramePosition() {
        return 0;
    }

    @Override
    public long getMicrosecondPosition() {
        return 0;
    }

    @Override
    public float getLevel() {
        return 0;
    }

    @Override
    public Line.Info getLineInfo() {
        return null;
    }

    @Override
    public void open() throws LineUnavailableException {

    }

    @Override
    public void close() {

    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public Control[] getControls() {
        return new Control[0];
    }

    @Override
    public boolean isControlSupported(Control.Type control) {
        return false;
    }

    @Override
    public Control getControl(Control.Type control) {
        return null;
    }

    @Override
    public void addLineListener(LineListener listener) {

    }

    @Override
    public void removeLineListener(LineListener listener) {

    }
}
