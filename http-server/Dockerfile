FROM openjdk:8-jre
MAINTAINER laazy laaazy@sjtu.edu.cn
ENV REFRESH_AT 2018_8_12

ENV JAVA_OPTS -Xmx512m -Xms128m
#ENTRYPOINT ["/usr/bin/java", "${JAVA_OPTS}", "-jar", "/usr/share/myservice/myservice.jar"]
ENTRYPOINT java ${JAVA_OPTS} -jar /usr/share/myservice/myservice.jar

# Add Maven dependencies (not shaded into the artifact; Docker-cached)
# ADD target/lib           /usr/share/myservice/lib
# Add the service itself
ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/myservice/myservice.jar
