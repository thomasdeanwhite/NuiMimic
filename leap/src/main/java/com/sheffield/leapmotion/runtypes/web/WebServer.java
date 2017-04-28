package com.sheffield.leapmotion.runtypes.web;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Listener;
import com.sheffield.leapmotion.App;
import com.sheffield.leapmotion.controller.mocks.HandFactory;
import com.sheffield.leapmotion.util.AppStatus;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by thomas on 24/04/17.
 */
public class WebServer extends Listener implements Runnable {

    private ServerSocket server;

    private HashMap<Socket, PrintWriter> clients = new HashMap<Socket, PrintWriter>();

    public WebServer() {
    }

    @Override
    public void run() {
        //6347 is the leapmotion server port
        try {
            server = new ServerSocket(6437);


            while (App.getApp().status() != AppStatus.FINISHED) {
                Socket client = server.accept();
                clients.put(client, new PrintWriter(client.getOutputStream(), true));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFrame(Controller controller) {
        for (Socket s : clients.keySet()){
            clients.get(s).print(HandFactory.toJavaScript(controller.frame()));
        }
    }
}
