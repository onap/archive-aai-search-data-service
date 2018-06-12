#!/bin/sh

BASEDIR="/opt/app/search-data-service"
AJSC_HOME="$BASEDIR"
AJSC_CONF_HOME="$AJSC_HOME/bundleconfig/"

if [ -z "$CONFIG_HOME" ]; then
	echo "CONFIG_HOME must be set in order to start up process"
	exit 1
fi

if [ -z "$KEY_STORE_PASSWORD" ]; then
	echo "KEY_STORE_PASSWORD must be set in order to start up process"
	exit 1
fi

PROPS="-DAJSC_HOME=$AJSC_HOME"
PROPS="$PROPS -DAJSC_CONF_HOME=$AJSC_CONF_HOME"
PROPS="$PROPS -Dlogging.config=$BASEDIR/bundleconfig/etc/logback.xml"
PROPS="$PROPS -DCONFIG_HOME=$CONFIG_HOME"
PROPS="$PROPS -DKEY_STORE_PASSWORD=$KEY_STORE_PASSWORD"

JVM_MAX_HEAP=${MAX_HEAP:-1024}

java $PROPS -jar $BASEDIR/search-data-service-package.jar