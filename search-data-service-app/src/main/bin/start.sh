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

# Changes related to:AAI-2179
# Change aai search-data  container processes to run as non-root on the host
USER_ID=${LOCAL_USER_ID:-9001}
GROUP_ID=${LOCAL_GROUP_ID:-9001}
SD_LOGS=/var/log/onap/AAI-SDB

if [ $(cat /etc/passwd | grep aaiadmin | wc -l) -eq 0 ]; then

        groupadd aaiadmin -g ${GROUP_ID} || {
                echo "Unable to create the group id for ${GROUP_ID}";
                exit 1;
        }
        useradd --shell=/bin/bash -u ${USER_ID} -g ${GROUP_ID} -o -c "" -m aaiadmin || {
                echo "Unable to create the user id for ${USER_ID}";
                exit 1;
        }
fi;

chown -R aaiadmin:aaiadmin ${MICRO_HOME}
chown -R aaiadmin:aaiadmin ${AJSC_HOME}
chown -R aaiadmin:aaiadmin ${SD_LOGS}

find ${MICRO_HOME}  -name "*.sh" -exec chmod +x {} +

gosu aaiadmin ln -s /logs $MICRO_HOME/logs
JAVA_CMD="exec gosu aaiadmin java";
###
PROPS="-DAJSC_HOME=$AJSC_HOME"
PROPS="$PROPS -DAJSC_CONF_HOME=$AJSC_CONF_HOME"
PROPS="$PROPS -Dlogging.config=$BASEDIR/bundleconfig/etc/logback.xml"
PROPS="$PROPS -DCONFIG_HOME=$CONFIG_HOME"
PROPS="$PROPS -DKEY_STORE_PASSWORD=$KEY_STORE_PASSWORD"

if [ ! -z "$TRUST_STORE_PASSWORD" ]; then
   PROPS="$PROPS -DTRUST_STORE_PASSWORD=${TRUST_STORE_PASSWORD}"
fi

if [ ! -z "$TRUST_STORE_LOCATION" ]; then
   PROPS="$PROPS -DTRUST_STORE_LOCATION=${TRUST_STORE_LOCATION}"
fi

JVM_MAX_HEAP=${MAX_HEAP:-1024}

${JAVA_CMD}   $PROPS -jar $BASEDIR/search-data-service-package.jar
