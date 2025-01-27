FROM openjdk:21-jdk-slim
LABEL authors="kgoldstein"
# Install curl
RUN apt-get update && apt-get install -y curl

ENV VERSION="$(cat /app/version.txt)"

WORKDIR /app
RUN mkdir -p /app/lib /app/etc
COPY target/classes /app/classes
COPY target/libs/* /app/libs/
COPY src/main/resources/application.yaml /app/etc/
COPY src/main/resources/logback.xml /app/etc/
COPY target/version.txt /app/etc/


# The java options
ENV JAVA_OPTS="-Xms128m -Xmx256m -Djava.awt.headless=true \
    -Dlogback.configurationFile=/app/etc/logback.xml \
    -Dspring.config.location=file:/app/etc/application.yaml \
    -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=0.0.0.0:5005"
ENV CLASSPATH="/app/classes:/app/libs/*"


EXPOSE 8080
EXPOSE 5005
ENTRYPOINT exec java $JAVA_OPTS -cp ${CLASSPATH} com.kagr.videos.orderclient.OrderClientMain
