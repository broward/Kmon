package com.nodemy.kafka;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.nodemy.kafka.Environment;

public class EnviromentTest {
    private static String[] args = { "src/test/resources/junit.properties" };

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Environment.instance().setConfigFile(args[0]);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testConstructor() {
        String configFile = Environment.instance().getConfigFile();
        assertTrue("configFile should not be null", configFile != null);
        assertTrue("configFile should be 'localhost'",
                configFile.equals(args[0]));
    }

    @Test
    public void testClusterVariable() {
        String[] cluster = Environment.instance().getList(Environment.CLUSTER);
        assertTrue("kafka cluster env variable should not be null",
                cluster != null);
        assertTrue("kafka cluster env variable should be 'localhost'",
                cluster[0].equals("localhost"));
    }

    @Test
    public void testUserVariable() {
        String user = Environment.instance().getValue(Environment.USER);
        assertTrue("jmx user env variable should not be null", user != null);
        assertTrue("jmx user env variable should be 'localhost'",
                user.equals("root"));
    }

}
