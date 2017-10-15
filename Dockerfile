FROM ubuntu:16.04

MAINTAINER Chernov Andrey

RUN apt-get -y update

USER root

RUN apt-get install -y openjdk-8-jdk-headless

RUN apt-get install -y maven

#
# Сборка проекта
#

ENV WORK /opt
ADD pom.xml $WORK/HighloadMaven/pom.xml
ADD src/    $WORK/HighloadMaven/src/

RUN mkdir -p /var/www/html

WORKDIR $WORK
RUN mvn package

EXPOSE 80

CMD java -Xmx300M -Xms300M -jar $WORK/HighloadMaven/target/spring-postgresql-1.0-SNAPSHOT.jar
