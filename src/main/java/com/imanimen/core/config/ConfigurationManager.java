package com.imanimen.core.config;

public class ConfigurationManager {
    // create singleton of configuration
    private static ConfigurationManager jttpConfigurationManager;
    private static Configuration jttpCurrentConfiguration;



    private ConfigurationManager() {}

    public static ConfigurationManager getInstance() {
        if (jttpConfigurationManager == null) {
            jttpConfigurationManager = new ConfigurationManager();
            return jttpConfigurationManager;
        }
    }

    /**
     * Used to load Configuration file by the path provided
     */
    public void loadConfigurationFile(String configFilePath) {}

    /**
     * Returns the current loaded Configuration
     */
    public void getCurrentConfiguration() {

    }

}
