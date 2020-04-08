FROM @aai.docker.namespace@/aai-common-@aai.base.image@:@aai.base.image.version@

ARG MICRO_HOME=/opt/app/search-data-service
ARG BIN_HOME=$MICRO_HOME/bin
#AAI-2179 Additional optional steps for Ubuntu
RUN if  ((uname -v | grep -i "ubuntu") || ( cat /etc/*release|grep -i "ubuntu")      ) ; then \
   #####################################
   #if JAVA doesnt exist then install it:
   ######################################
   if type  java  2>/dev/null; then \
     echo "skipping java installation"; \
   else \
     apt-get update && apt-get install -y software-properties-common; \
     ## sudo -E is required to preserve the environment. If you remove that line, it will most like freeze at this step
     sudo -E add-apt-repository ppa:openjdk-r/ppa && apt-get update && apt-get install -y openjdk-8-jdk ; \
     ## Setup JAVA_HOME, this is useful for docker commandline
     JAVA_HOME=usr/lib/jvm/java-8-openjdk-amd64; \
     export JAVA_HOME ; \
   fi; \
   ##########################################
   #Install gosu if not present
   ##########################################
   if type  gosu   2>/dev/null; then \
     echo "skipping gosu installation"; \
   else \
     set -x ; \
     add-apt-repository -y ppa:tianon/gosu; \
     sudo apt-get update; \
     sudo apt-get install gosu; \
   fi;\
fi

# Build up the deployment folder structure
RUN mkdir -p $MICRO_HOME $USERS_HOME /logs
WORKDIR $MICRO_HOME
RUN mkdir -p $MICRO_HOME/bundleconfig/etc
RUN mkdir -p $BIN_HOME
# copy everything from search-data-service-app/target/docker/nexus3.onap.org/10003/onap/search-data-service/latest/build/maven/search-data-service-app
COPY /maven/search-data-service-app/ .
RUN chmod -R 755 $BIN_HOME/* $MICRO_HOME/*
RUN ln -snf /logs $MICRO_HOME/logs

EXPOSE 9509 9509

CMD ["/opt/app/search-data-service/bin/start.sh"]