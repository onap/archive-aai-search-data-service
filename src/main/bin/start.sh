#!/bin/sh

BASEDIR="/opt/app/search-data-service/"
AJSC_HOME="$BASEDIR"
AJSC_CONF_HOME="$BASEDIR/bundleconfig/"

if [ -z "$CONFIG_HOME" ]; then
	echo "CONFIG_HOME must be set in order to start up process"
	exit 1
fi

if [ -z "$KEY_STORE_PASSWORD" ]; then
	echo "KEY_STORE_PASSWORD must be set in order to start up process"
	exit 1
else
	echo "KEY_STORE_PASSWORD=$KEY_STORE_PASSWORD\n" >> $AJSC_CONF_HOME/etc/sysprops/sys-props.properties
fi

if [ -z "$KEY_MANAGER_PASSWORD" ]; then
	echo "KEY_MANAGER_PASSWORD must be set in order to start up process"
	exit 1
else
	echo "KEY_MANAGER_PASSWORD=$KEY_MANAGER_PASSWORD\n" >> $AJSC_CONF_HOME/etc/sysprops/sys-props.properties
fi

CLASSPATH="$AJSC_HOME/lib/*"
CLASSPATH="$CLASSPATH:$AJSC_HOME/extJars/"
CLASSPATH="$CLASSPATH:$AJSC_HOME/etc/"
PROPS="-DAJSC_HOME=$AJSC_HOME"
PROPS="$PROPS -DAJSC_CONF_HOME=$BASEDIR/bundleconfig/"
PROPS="$PROPS -Dlogback.configurationFile=$BASEDIR/bundleconfig/etc/logback.xml"
PROPS="$PROPS -DAJSC_SHARED_CONFIG=$AJSC_CONF_HOME"
PROPS="$PROPS -DAJSC_SERVICE_NAMESPACE=search-data-service"
PROPS="$PROPS -DAJSC_SERVICE_VERSION=v1"
PROPS="$PROPS -Dserver.port=9509"
PROPS="$PROPS -DCONFIG_HOME=$CONFIG_HOME"
JVM_MAX_HEAP=${MAX_HEAP:-1024}

echo $CLASSPATH

exec java -Xmx${JVM_MAX_HEAP}m $PROPS -classpath $CLASSPATH com.att.ajsc.runner.Runner context=// sslport=9509
