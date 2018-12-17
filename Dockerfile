#
# Grakn Dockerfile
#
# https://github.com/bfergerson/grakn-docker-toolbox
#
FROM maven:3.5-jdk-8

#USER root

LABEL maintainer="github.com/bfergerson"

ARG GRAKN_VERSION=1.4.2

ENV GRAKN_HOME=/opt/grakn

RUN mkdir -p $GRAKN_HOME && \
    wget https://github.com/graknlabs/grakn/releases/download/v${GRAKN_VERSION}/grakn-core-${GRAKN_VERSION}.zip && \
    unzip grakn-core-${GRAKN_VERSION}.zip -d $GRAKN_HOME && cp -Rf $GRAKN_HOME/grakn-core-${GRAKN_VERSION}/* $GRAKN_HOME
  
#RUN curl -fsSLO https://get.docker.com/builds/Linux/x86_64/docker-17.04.0-ce.tgz \
#  && tar xzvf docker-17.04.0-ce.tgz \
#  && mv docker/docker /usr/local/bin \
#  && rm -r docker docker-17.04.0-ce.tgz

ENV PATH=$PATH:$GRAKN_HOME
WORKDIR $GRAKN_HOME

COPY cassandra.yaml $GRAKN_HOME/services/cassandra
# COPY grakn-docker-entrypoint /usr/local/bin

COPY simple-graph /usr/share/simple-graph

# Grakn Server
EXPOSE 4567
# Thrift client API
EXPOSE 9160
# Grakn gRPC
EXPOSE 48555

#ENTRYPOINT [ "grakn-docker-entrypoint" ]
