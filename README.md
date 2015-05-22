JMX 2.0 was released prior to the Sun/Oracle merger as the OpenDMK open source project ( https://opendmk.java.net/ ). Originally, JMX evolved around management of a single java JVM and JMX 2.0 was designed for true enterprise management of hundreds of java virtual machines across multiple servers.

Kafka is a natural candidate for JMX 2.0 and in this project I use two components, OpenDMK and Jolokia (https://jolokia.org/ ) to produce a simple Kafka monitor. There's about 250 lines of java code and some html/json code for the UI.

The end result is very fast and easy to modify, unlike most of the current Kafka monitors which require Play/Akka/Scala/SBT knowledge and substantially more code.

﻿Startup commands using default config (kafka-1,kafka-2,kafka-3)

    - cd /opt/kmon/target
    - java -cp Kmon-jar-with-dependencies.jar com.nodemy.kafka.Kmon &

    
Startup with an external config file

    - cd /opt/kmon/target
    - java -cp Kmon-jar-with-dependencies.jar com.nodemy.kafka.Kmon configFile=/opt/kmon.properties &

Kmon will load the RMI registry so that it can register itself as a JMX service

Main server class is com.nodemy.kafka.Kmon

Http access is provided by jolokia agent, accessible on http://host:8080/jolokia/list

