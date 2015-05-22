package com.nodemy.kafka;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Development environment singleton
 * 
 * @author broward
 *
 */
public class Environment extends Properties {
    private static final long serialVersionUID = 1L;
    private static Environment instance = null;
    private static String configFile = null;
    public static final String KAFKA = "kafka.monitor";
    public static final String CLUSTER = "kafka.cluster";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String CYCLETIME = "cycletime";
    private static Logger logger = Logger
            .getLogger(Environment.class.getName());

    protected Environment() {
        super();
        loadFile();
    }

    /**
     * load the current configuration file
     */
    private void loadFile() {
        this.clear();
        InputStream input = null;

        try {
            // Load an external config file if it was set
            if (Environment.configFile != null) {
                input = new FileInputStream(Environment.configFile);

                // otherwise load our default resource file
            } else {
                input = this.getClass().getClassLoader()
                        .getResourceAsStream("default.properties");
            }

            this.load(input);
        } catch (Exception e) {
            logger.log(Level.FINE, "...exception loading config file", e);
        }
    }
    
    public static Environment instance() {
        if (instance == null) {
            instance = new Environment();
        }
        return instance;
    }

    public String getValue(String key) {
        return (String) this.getProperty(key);
    }

    public String[] getList(String key) {
        return getProperty(key).split(",");
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        Environment.configFile = configFile;
        loadFile();
    }
}
