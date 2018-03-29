#!/bin/sh

BASEDIR="/opt/app/search-data-service/"

if [ -z "$CONFIG_HOME" ]; then
	echo "CONFIG_HOME must be set in order to start up process"
	exit 1
fi

if [ -z "$KEY_STORE_PASSWORD" ]; then
	echo "KEY_STORE_PASSWORD must be set in order to start up process"
	exit 1
else
	echo "server.ssl.key-store-password=$KEY_STORE_PASSWORD" >> $BASEDIR/config/application.properties
fi

## tomcat_keystore to p12
## keytool -importkeystore -deststorepass onapSecret -destkeypass onapSecret -srckeystore /opt/app/search-data-service/config/auth/tomcat_keystore -destkeystore /opt/app/search-data-service/config/auth/onap.p12 -deststoretype PKCS12 -srcstorepass onapSecret
keytool -importkeystore -noprompt -deststorepass onapSecret -destkeypass onapSecret -srckeystore /opt/app/search-data-service/config/auth/tomcat_keystore -destkeystore /opt/app/search-data-service/config/auth/onap.p12 -deststoretype PKCS12 -srcstorepass onapSecret

## import into cacerts
## keytool -importkeystore -deststorepass changeit -destkeypass changeit -destkeystore $JAVA_HOME/jre/lib/security/cacerts -srckeystore /opt/app/search-data-service/config/auth/onap.p12 -srcstoretype PKCS12 -srcstorepass onapSecret  -alias tomcat
keytool -importkeystore -noprompt -deststorepass changeit -destkeypass changeit -destkeystore $JAVA_HOME/jre/lib/security/cacerts -srckeystore /opt/app/search-data-service/config/auth/onap.p12 -srcstoretype PKCS12 -srcstorepass onapSecret  -alias tomcat



PROPS="$PROPS -Dlogback.configurationFile=$BASEDIR/bundleconfig/etc/logback.xml"
#PROPS="$PROPS -Dserver.port=9509"
PROPS="$PROPS -DCONFIG_HOME=$CONFIG_HOME"
JVM_MAX_HEAP=${MAX_HEAP:-1024}

java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000 $PROPS -jar $BASEDIR/search-data-service-1.2.0-SNAPSHOT.jar --spring.config.location=$BASEDIR/config/application.properties
