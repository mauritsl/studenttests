FROM alpine:3.9

ADD pom.xml /app/
ADD src /app/src

WORKDIR /app

RUN apk add --update openjdk8 maven \
 && cd /app \
 && mvn install \
 && mv target/edwards-0.0.1-jar-with-dependencies.jar edwards.jar

ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk

CMD ["java", "-jar", "edwards.jar", "/data", "/test"]
