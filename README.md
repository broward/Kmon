Startup commands using default config (kafka-1,kafka-2,kafka-3)

    - cd /opt/kmon/target
    - java -cp Kmon-jar-with-dependencies.jar com.nodemy.kafka.Kmon &

    
Startup with an external config file

    - cd /opt/kmon/target
    - java -cp Kmon-jar-with-dependencies.jar com.nodemy.kafka.Kmon configFile=/opt/kmon.properties &

Kmon will load the RMI registry so that it can register itself as a JMX service

Main server class is com.nodemy.kafka.Kmon

Http access is provided by jolokia agent, accessible on http://host:8080/jolokia/list

