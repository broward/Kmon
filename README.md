JMX 2.0 was released as the "OpenDMK" open source project prior to the Sun/Oracle merger ( https://opendmk.java.net/ ).  JMX 1.0 evolved around management of a single java JVM and JMX 2.0 is designed for enterprise management of hundreds of java virtual machines across multiple servers.

Kafka is a natural candidate for JMX 2.0 and in this monitor I use two components, OpenDMK and Jolokia (https://jolokia.org/ ).  There are three java classes (250 lines of code) and a little javascript code. It's fast, simple and easy to maintain, unlike most of the current Kafka monitors which require Play/Akka/Scala/SBT knowledge.

This design is a [refinement of a prototype](https://github.com/broward/MCC) I wrote in 2011. That MCC project is a swing-based JMX client, this project is the corresponding JMX server. I'm using it with Kafka but it's generically applicable to any JMX application.

The JMX 2.0 cascading server virtualizes and aggregates the mbeans of each Kafka server and the Zookeeper server.  The jolokia server then transforms the cascaded beans into a JSON output, which is easily massaged to display relevant information.
