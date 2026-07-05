package com.imanimen.jttpserver;

import com.imanimen.jttpserver.config.Configuration;
import com.imanimen.jttpserver.config.ConfigurationManager;
import com.imanimen.jttpserver.core.ServerListenerThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;

/**
 * Driver Class for the Jttp Server
 */
public class JttpServer {
    private final static Logger LOGGER = LoggerFactory.getLogger(JttpServer.class);

    public static void main(String[] args) {

        LOGGER.info("Jttp Server Started at {}", new Date().toString());
        ConfigurationManager.getInstance().loadConfigurationFile("src/main/resources/jttp.json");
        Configuration conf = ConfigurationManager.getInstance().getCurrentConfiguration();

        try {
            ServerListenerThread serverListenerThread = new ServerListenerThread(conf.getPort(), conf.getWebroot());
            serverListenerThread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
            // todo: handle later
        }
    }
}
