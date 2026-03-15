FROM maven:3.9.9-eclipse-temurin-11 AS build

WORKDIR /app

COPY pom.xml .
COPY src src
COPY WebContent WebContent
COPY resources resources

ARG MVN_PROFILE=default
RUN mvn -P ${MVN_PROFILE} -DskipTests package

FROM tomcat:10.1-jdk11-temurin

ARG WAR_NAME=cs122b-project

WORKDIR /app

RUN rm -rf /usr/local/tomcat/webapps/*

COPY --from=build /app/target/${WAR_NAME}.war /usr/local/tomcat/webapps/${WAR_NAME}.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
