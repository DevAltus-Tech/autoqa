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


ENV JAVA_OPTS="-Xms256m -Xmx512m -Djava.awt.headless=true \
    -Dlogback.configurationFile=/app/etc/logback.xml \
    -Dspring.config.location=file:/app/etc/application.properties \
    -XX:+UnlockExperimentalVMOptions \
    -XX:+EnableJVMCI \
    -XX:+UseJVMCICompiler"
ENV CLASSPATH="/app/lib/*"

EXPOSE 8080
ENTRYPOINT ["java", "-cp", "/app/classes:/app/libs/*", "com.kagr.videos.orderclient.OrderClientMain"]
