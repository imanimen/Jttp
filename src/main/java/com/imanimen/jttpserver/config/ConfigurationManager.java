package com.imanimen.jttpserver.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.imanimen.jttpserver.util.JxUtil;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ConfigurationManager {
    // create singleton of configuration
    private static ConfigurationManager jttpConfigurationManager;
    private static Configuration jttpCurrentConfiguration;



    private ConfigurationManager() {}

    public static ConfigurationManager getInstance() {
        if (jttpConfigurationManager == null)
            jttpConfigurationManager = new ConfigurationManager();
        return jttpConfigurationManager;
    }

    /**
     * Used to load Configuration file by the path provided
     */
    public void loadConfigurationFile(String configFilePath)  {
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(configFilePath);
        } catch (FileNotFoundException e) {
            throw new JttpConfigurationException(e);
        }
        StringBuffer stringBuffer = new StringBuffer();
        int i;
        try {
            while ( ( i = fileReader.read()) != -1) {
                stringBuffer.append((char) i);
            }
        } catch (IOException e) {
            throw new JttpConfigurationException(e);
        }
        JsonNode conf = null;
        try {
            conf = JxUtil.parse(stringBuffer.toString());
        } catch (IOException e) {
            throw new JttpConfigurationException("Error parsing configuration file", e);
        }
        try {
            jttpCurrentConfiguration = JxUtil.fromJson(conf, Configuration.class);
        } catch (JsonProcessingException e) {
            throw new JttpConfigurationException("Error parsing configuration file. internal", e);
        }
    }

    /**
     * Returns the current loaded Configuration
     */
    public Configuration getCurrentConfiguration() {
        if (jttpCurrentConfiguration == null) {
            throw new JttpConfigurationException("No current configuration Set.");
        }
        return jttpCurrentConfiguration;
    }

}
