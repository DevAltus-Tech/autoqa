FROM openjdk:21-jdk-slim

# Install curl
RUN apt-get update && apt-get install -y curl


# Set the working directory to /app
WORKDIR /app


# Copy the current directory contents into the container at /app
COPY target/*.jar /app/libs/order-generator.jar
COPY target/libs/* /app/libs/
COPY src/main/resources/application.yaml /app/etc/
COPY src/main/resources/logback.xml /app/etc/
COPY target/version.txt /app/etc/


# The java options
ENV JAVA_OPTS="-Xms128m -Xmx256m -Djava.awt.headless=true \
    -Dlogback.configurationFile=/app/etc/logback.xml \
    -Dspring.config.location=file:/app/etc/application.yaml \
    -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
ENV CLASSPATH="/app/classes:/app/libs/*"



# ensure we have a healthcheck
HEALTHCHECK --interval=1s --timeout=2s --retries=10 CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080
EXPOSE 5005
ENTRYPOINT exec java $JAVA_OPTS -jar /app/libs/order-generator.jar
