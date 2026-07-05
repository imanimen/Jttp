package com.imanimen.jttpserver;

import com.imanimen.jttpserver.config.Configuration;
import com.imanimen.jttpserver.config.ConfigurationManager;
import com.imanimen.jttpserver.core.ServerListenerThread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Driver Class for the Jttp Server
 */
public class JttpServer {
    public static void main(String[] args) {
        System.out.println("Starting JttpServer");
        ConfigurationManager.getInstance().loadConfigurationFile("src/main/resources/jttp.json");
        Configuration conf = ConfigurationManager.getInstance().getCurrentConfiguration();

        System.out.println("Using Port: " + conf.getPort());
        System.out.println("Using WebRoot: " + conf.getWebroot());

        try {
            ServerListenerThread serverListenerThread = new ServerListenerThread(conf.getPort(), conf.getWebroot());
            serverListenerThread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
            // todo: handle later
        }
    }
}
