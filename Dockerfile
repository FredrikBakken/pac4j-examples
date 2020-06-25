FROM openjdk:8

# Install maven
RUN apt-get update
RUN apt-get install -y maven

COPY ./code /usr/src/

WORKDIR /usr/src

# Prepare by downloading dependencies
RUN ["mvn", "dependency:resolve"]
RUN ["mvn", "verify"]

# Adding source, compile and package into a fat jar
RUN ["mvn", "package"]

EXPOSE 4567
CMD ["java", "-jar", "target/pac4jexamples-jar-with-dependencies.jar"]
