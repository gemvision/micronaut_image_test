FROM openjdk:15-alpine
COPY target/micronaut_image_test-*.jar /app.jar
CMD java  ${JAVA_OPTS} -XX:+UseZGC -Xmx410m -Xms410m  -jar  /app.jar