package com.nodemy.kafka;

import java.io.IOException;
import java.net.BindException;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;

import org.jolokia.jvmagent.JolokiaServer;
import org.jolokia.jvmagent.JolokiaServerConfig;

import com.sun.jdmk.remote.cascading.CascadingService;

/**
 * This MBeanServer is a central access point which aggregates many other
 * MBeanServers together, in order to monitor all our Kafka queues.
 * 
 * @author broward
 *
 */
public class Kmon {
    private static MBeanServer mbs = null;
    private static CascadingService cascade = null;
    private static String serviceURL = JMXConstants.buildJMXUrl("localhost",
            "1099");
    private static Map<String, Object> props = new HashMap<String, Object>();
    private static ObjectName pattern = null;
    private static boolean stopServer = false;
    private static Logger logger = Logger.getLogger(Kmon.class
            .getName());
    @SuppressWarnings("unused")
    private static JolokiaServer jolokiaServer = null;

    protected Kmon() {
    }

    public static void main(String[] args) {

        parseArgs(args);

        // load the RMI registry
        loadRMIRegistry();

        // create jolokia agent
        jolokiaServer = createJolokiaServer("0.0.0.0", "8080");

        try {
            // create our master mbean server
            createMBeanServer();

            // create cascading service to merge mbean servers
            createCascader();

            logger.log(Level.INFO,
                    "Server loading, bean count is " + mbs.getMBeanCount());

            // gather Kafka mBeans for each server, jmx port of 9999
            pattern = new ObjectName("kafka.*:*");
            String[] kafkaInstances = Environment.instance().getList(
                    Environment.CLUSTER);
            for (String instance : kafkaInstances) {
                addMBeanServer(instance, "9999");
            }

            // gather Zookeeper mBeans for each server, jmx port of 9998
            pattern = new ObjectName("org.apache.ZooKeeper*:*");
            for (String instance : kafkaInstances) {
                addMBeanServer(instance, "9998");
            }

        } catch (MalformedObjectNameException | InstanceAlreadyExistsException
                | IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
        }

        logger.log(Level.INFO,
                "Server ready, bean count is " + mbs.getMBeanCount());

        boolean running = true;
        String cycleTime = Environment.instance().getValue(
                Environment.CYCLETIME);

        // start our server
        while (running) {
            try {
                Thread.sleep(Integer.parseInt(cycleTime));
                running = !stopServer;
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, e.getMessage());
            }
        }
    }

    protected static void addMBeanServer(String host, String port)
            throws InstanceAlreadyExistsException, IOException {
        String temp = JMXConstants.buildJMXUrl(host, port);
        logger.log(Level.INFO, "Adding mbeanserver from " + temp);
        JMXServiceURL url = new JMXServiceURL(temp);
        String mountId = cascade.mount(url, props, pattern, host);
        logger.log(Level.FINE, "...mounted with id " + mountId);
    }

    interface CascaderMXBean {
        void refresh();

        SortedMap<String, String> getCascadeResults();
    }

    /**
     * Cascading bean interface and class
     * 
     * @author horbr002
     *
     */
    public static class CascaderImpl implements CascaderMXBean {

        private SortedMap<String, String> cascadeResults;

        CascaderImpl(CascadingService cascade) {
            Kmon.cascade = cascade;
        }

        public SortedMap<String, String> getCascadeResults() {
            return cascadeResults;
        }

        @Override
        public void refresh() {
        }
    }

    /**
     * Create jolokai agent so we can view mBeans via HTTP/json
     */
    protected static JolokiaServer createJolokiaServer(String address,
            String port) {
        JolokiaServer server = null;

        try {
            Map<String, String> jolokiaConfiguration = new HashMap<String, String>();
            jolokiaConfiguration.put("host", address);
            jolokiaConfiguration.put("port", port);
            JolokiaServerConfig jolokiaServerConfig = new JolokiaServerConfig(
                    jolokiaConfiguration);
            server = new JolokiaServer(jolokiaServerConfig, true);
            server.start();
        } catch (BindException e1) {
            logger.log(Level.FINE,
                    "...exception loading jolokia agent, already loaded", e1);
        } catch (IOException e) {
            logger.log(Level.FINE,
                    "...exception loading jolokia agent, already loaded", e);
        }

        return server;
    }

    /**
     * Create RMI registry so we can register as an RMI service
     */
    protected static void loadRMIRegistry() {
        Registry registry = null;

        try {
            logger.log(Level.FINE, "creating rmi registry");
            registry = java.rmi.registry.LocateRegistry
                    .createRegistry(Registry.REGISTRY_PORT);
        } catch (Exception e) {
            try {
                if (registry == null) {
                    logger.log(Level.FINE, "retrieving existing rmi registry");
                    registry = java.rmi.registry.LocateRegistry
                            .getRegistry(Registry.REGISTRY_PORT);
                }
            } catch (Exception e2) {
                logger.log(Level.FINE, "... error retrieving rmi registry", e);
            }
            logger.log(Level.FINE, "error creating rmi registry");
        }
    }

    /**
     * Create our master mbeanserver
     * 
     * @throws IOException
     * @throws MalformedURLException
     * @throws MalformedObjectNameException
     * @throws NotCompliantMBeanException
     * @throws MBeanRegistrationException
     * @throws InstanceAlreadyExistsException
     */
    protected static MBeanServer createMBeanServer() throws IOException,
            InstanceAlreadyExistsException, MBeanRegistrationException,
            NotCompliantMBeanException, MalformedObjectNameException {

        mbs = MBeanServerFactory.createMBeanServer("Kmon");
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("java.naming.factory.initial",
                "com.sun.jndi.rmi.registry.RegistryContextFactory");
        map.put(RMIConnectorServer.JNDI_REBIND_ATTRIBUTE, "true");
        JMXConnectorServer connector;

        connector = JMXConnectorServerFactory.newJMXConnectorServer(
                new JMXServiceURL(serviceURL), map, mbs);

        // register the connector server as an MBean
        mbs.registerMBean(connector, new ObjectName(
                "kafkaMon.connector:type=KafkaConnector,name=KafkaConnector"));

        // start the connector server
        connector.start();
        return mbs;
    }

    /**
     * Create cascading service to merge mbean servers
     * 
     * @throws MalformedObjectNameException
     * @throws NotCompliantMBeanException
     * @throws MBeanRegistrationException
     * @throws InstanceAlreadyExistsException
     */
    private static void createCascader() throws MalformedObjectNameException,
            InstanceAlreadyExistsException, MBeanRegistrationException,
            NotCompliantMBeanException {

        cascade = new CascadingService(mbs);
        CascaderImpl cascader = new CascaderImpl(cascade);
        cascader.refresh();
        ObjectName cascaderName;
        cascaderName = new ObjectName(
                "Kmon.cascader:type=KmonCascader,name=KmonCascader");
        mbs.registerMBean(cascader, cascaderName);
    }

    /**
     * Parse our command line arguments, looking for an external config file
     * 
     * @return
     */
    protected static void parseArgs(String[] args) {

        // allow external config files to be used
        if (args != null) {
            for (String arg : args) {
                if ((arg != null) && (arg.indexOf("config.file") > -1)) {
                    int index = arg.indexOf("=");
                    Environment.instance().setConfigFile(
                            arg.substring(index + 1, arg.length()).trim());
                }
            }
        }

        // Add JMX login credentials in case we need them.
        String user = Environment.instance().getValue(Environment.USER);
        String password = Environment.instance().getValue(Environment.PASSWORD);
        props.put(JMXConnector.CREDENTIALS, new String[] { user, password });
    }

    /**
     * 
     * So we can turn the server on/off programmatically
     **/
    public static boolean isStopServer() {
        return stopServer;
    }

    public static void setStopServer(boolean stopServer) {
        Kmon.stopServer = stopServer;
    }
}
