FROM maven:3.9.9-eclipse-temurin-11 AS build

WORKDIR /build

COPY pom.xml .
COPY src src
COPY WebContent WebContent
COPY resources resources

ARG MVN_PROFILE=default
RUN mvn -P ${MVN_PROFILE} -DskipTests package

FROM tomcat:10.1-jdk11-temurin

ARG WAR_NAME=cs122b-project

RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=build /build/target/${WAR_NAME}.war /usr/local/tomcat/webapps/ROOT.war
