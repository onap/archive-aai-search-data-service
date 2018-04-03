#!/bin/sh

BASEDIR="/opt/app/search-data-service"

if [ -z "$CONFIG_HOME" ]; then
	echo "CONFIG_HOME must be set in order to start up process"
	exit 1
fi

if [ -z "$KEY_STORE_PASSWORD" ]; then
	echo "KEY_STORE_PASSWORD must be set in order to start up process"
	exit 1
else
	## Extract java jar to DEOBFUSCATE the password.
	cd $BASEDIR
	jar xf search-data-service-package.jar
	sudo java -cp ./BOOT-INF/lib/jetty-util-9.4.8.v20171121.jar org.eclipse.jetty.util.security.Password $KEY_STORE_PASSWORD > pass.txt 2>> pass.txt
	PASS=`sed "2q;d" pass.txt`
	sudo rm pass.txt
	cd $CURR_D
fi

## tomcat_keystore to p12
keytool -importkeystore -noprompt -deststorepass $PASS -destkeypass $PASS -srckeystore $BASEDIR/config/auth/tomcat_keystore -destkeystore $BASEDIR/config/auth/onap.p12 -deststoretype PKCS12 -srcstorepass $PASS

## import into cacerts
sudo keytool -importkeystore -noprompt -deststorepass changeit -destkeypass changeit -destkeystore /$JAVA_HOME/jre/lib/security/cacerts -srckeystore $BASEDIR/config/auth/onap.p12 -srcstoretype PKCS12 -srcstorepass $PASS -alias tomcat

PROPS="$PROPS -Dlogback.configurationFile=$BASEDIR/bundleconfig/etc/logback.xml"
PROPS="$PROPS -DCONFIG_HOME=$CONFIG_HOME"
PROPS="$PROPS -DKEY_STORE_PASSWORD=$KEY_STORE_PASSWORD"
JVM_MAX_HEAP=${MAX_HEAP:-1024}

java $PROPS -jar $BASEDIR/search-data-service-package.jar