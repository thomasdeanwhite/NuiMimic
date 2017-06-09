package com.sheffield.leapmotion.runtypes.web;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by thomas on 09/06/17.
 */
public class TestWebServer {

    String key;

    @Before
    public void setup(){
        key = "dGhlIHNhbXBsZSBub25jZQ==";
    }

    @Test
    public void testKeyDerivation(){
        assertEquals("s3pPLMBiTxaQ9kYGzzhZRbK+xOo=", WebServer.deriveHttpKey(key));
    }

    @Test
    public void testEndHandshake(){
        assertTrue(WebServer.getHttpHeaders(key).endsWith("\r\n\r\n"));
    }
}
