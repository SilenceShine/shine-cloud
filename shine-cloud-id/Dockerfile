FROM openjdk:17-jdk-alpine3.14
# COPY --from=hengyunabc/arthas:latest /opt/arthas /opt/arthas

MAINTAINER  xuansy
EXPOSE 10200
ARG jar
COPY ${jar}  app.jar
CMD ["java", "-Dfile.encoding=utf-8", "-jar", "app.jar"]