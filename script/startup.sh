#!/bin/bash

JAR="Kmon-jar-with-dependencies.jar"
MAIN_CLASS="com.nodemy.kafka.Kmon"
APP_CONFIG="/opt/kmon/src/main/resources/default.properties"
#LOG_CONFIG={{ log_config }}
INSTALL_DIR="/opt/kmon/target"

# ***********************************************
# ***********************************************

ARGS="-Dconfig.file=${APP_CONFIG} "
cd $INSTALL_DIR
exec java -cp $JAR $MAIN_CLASS $ARGS
