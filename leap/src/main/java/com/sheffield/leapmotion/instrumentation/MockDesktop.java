package com.sheffield.leapmotion.instrumentation;

import com.sheffield.leapmotion.App;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * Created by thomas on 05/12/2016.
 */
public class MockDesktop {

    public static void browse(URI uri)
            throws IOException {
        // do nothing
        App.out.println("Stopped " + uri.getPath() + " from opening.");
    }

    public static boolean isDesktopSupported(){
        return Desktop.isDesktopSupported();
    }

    public static boolean isSupported(Desktop.Action action){
        return Desktop.getDesktop().isSupported(action);
    }

    public static Desktop getDesktop(){
        return Desktop.getDesktop();
    }

    public void edit(File f){

    }

    public void mail(){

    }

    public void mail(URI mailToUri){

    }

    public void open(File f){

    }

    public void print(File f){

    }
}
