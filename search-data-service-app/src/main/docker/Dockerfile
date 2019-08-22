FROM @aai.docker.namespace@/aai-common-@aai.base.image@:@aai.base.image.version@

ARG MICRO_HOME=/opt/app/search-data-service
ARG BIN_HOME=$MICRO_HOME/bin

# Build up the deployment folder structure
RUN mkdir -p $MICRO_HOME $USERS_HOME /logs
WORKDIR $MICRO_HOME
COPY /maven/@project.artifactId@/ .
RUN chmod 755 $BIN_HOME/* \
    && ln -snf /logs $MICRO_HOME/logs

EXPOSE 9509 9509

CMD ["/opt/app/search-data-service/bin/start.sh"]
