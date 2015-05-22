package com.nodemy.kafka;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.rmi.registry.Registry;

import javax.management.MBeanServer;

import org.jolokia.jvmagent.JolokiaServer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.nodemy.kafka.Environment;
import com.nodemy.kafka.Kmon;

public class KmonTest {

    private static String[] args = { "config.file=src/test/resources/junit.properties" };
    private static String ip = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        // load registry using KafkaMonitor's method
        Kmon.loadRMIRegistry();

        // get our current ip address
        ip = InetAddress.getLocalHost().getHostAddress();
    }

    @Before
    public void setup() {
        Environment.instance().setConfigFile(args[0]);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        Kmon.setStopServer(true);
    }

    @Test
    public void testRMIRegistry() {

        // should be able to query registry
        Kmon.loadRMIRegistry();
        try {
            Registry registry = java.rmi.registry.LocateRegistry.getRegistry();

            // add one remote object
            registry.bind("test", registry);
            assertTrue("Registry should contain at least one reference",
                    registry.list().length > 0);
        } catch (Exception e) {
            fail("LocateRegistry call failed:" + e.getMessage());
        }
    }

    @Test
    public void testConfigFile() {
        Kmon.setStopServer(true);
        Kmon.main(args);

        assertTrue("Cluster should be for localhost", Environment.instance()
                .getValue(Environment.CLUSTER).equals("localhost"));
    }

    @Test
    public void testJolokiaServer() {
        JolokiaServer server = null;

        // use a port that hopefully won't conflict with Jenkins
        server = Kmon.createJolokiaServer(ip, "9001");
        assertTrue("Jolokia server should not be null", server != null);
        assertTrue("Jolokia url should have host ip in it", server.getUrl()
                .indexOf(ip) > -1);
    }

    @Test
    public void testMBeanServer() {
        Kmon.setStopServer(true);
        Kmon.main(args);
        MBeanServer mbs = null;

        try {
            mbs = Kmon.createMBeanServer();
        } catch (Exception e) {
            fail("testMBeanServer failed in an unpredictable way: "
                    + e.getMessage());
        }

        assertTrue("mbs should not be null", mbs != null);
        assertTrue("mbs should have at least two beans",
                mbs.getMBeanCount() > 1);
    }

    @Test
    public void testBadConfigFile() {
        try {
            String[] args = { "config.file=src/test/resources/badconfig.properties" };
            Kmon.main(args);
        } catch (Exception e) {
            assertTrue(
                    "Env cluster value should fail as null here",
                    Environment.instance().getValue(Environment.CLUSTER) == null);
        }
    }

    @Test
    public void testBadCycleTime() {
        try {
            String[] args = { "config.file=src/test/resources/badcycletime.properties" };
            Kmon.main(args);
            fail("BadCycleTime test should have failed on NumberFormatException");
        } catch (Exception e) {
            assertTrue("Should have thrown a NumberFormatException", e
                    .toString().indexOf("NumberFormatException") > -1);
        }
    }

    @Test
    public void testNoConfigFile() {
        try {
            Environment.instance().setConfigFile("fail here");
            Kmon.main(null);
        } catch (Exception e) {
            assertTrue(
                    "Env cluster value should fail as null here",
                    Environment.instance().getValue(Environment.CLUSTER) == null);
        }
    }

    @Test
    public void testAddMBeanServer() {
        try {
            Kmon.main(args);

            // should be able to add our own mbeanserver to itself
            Kmon.addMBeanServer("localhost", "1099");
            assertTrue("AddMBeanServer call to localhost should succeed", true);
        } catch (Exception e) {
            fail("AddMBeanServer call failed:" + e.getMessage());
        }
    }

    @Test
    public void testFailAddMBeanServer() {
        try {
            Kmon.main(args);

            // should be able to add our own mbeanserver to itself
            Kmon.addMBeanServer("x", "8080");
            fail("AddMBeanServer should have failed on port 8080");
        } catch (Exception e) {
            assertTrue(
                    "FailAddMBeanServer should have thrown an UnknownHostException",
                    e.getMessage().indexOf("UnknownHostException") > -1);
        }
    }
    
    @Test
    public void testConstructor() {
        Kmon monitor = new Kmon();
        assertTrue("Monitor constructor should not return null", monitor != null);
    }
}
