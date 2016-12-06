package com.sheffield.leapmotion.instrumentation;

import com.sheffield.leapmotion.App;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

/**
 * Created by thomas on 05/12/2016.
 */
public class MockDesktop {

    public void browse(URI uri)
            throws IOException {
        // do nothing
        App.out.println("Stopped " + uri.getPath() + " from opening.");
    }
}
