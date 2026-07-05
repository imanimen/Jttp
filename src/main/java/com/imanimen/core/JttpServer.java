package com.imanimen.core;

import com.imanimen.core.config.Configuration;
import com.imanimen.core.config.ConfigurationManager;

/**
 * Driver Class for the Jttp Server
 */
public class JttpServer {
    public static void main(String[] args) {
        System.out.println("Starting JttpServer");
        ConfigurationManager.getInstance().loadConfigurationFile("src/main/resources/jttp.json");
        Configuration conf = ConfigurationManager.getInstance().getCurrentConfiguration();
        System.out.println("Current configuration: " + conf);
        System.out.println("Using Port: " + conf.getPort());
        System.out.println("Using WebRoot: " + conf.getWebroot());
    }
}
