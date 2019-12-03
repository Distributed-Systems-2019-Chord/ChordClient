FROM maven:3.6.3-jdk-11-slim AS build
WORKDIR /home/ChordAkka
COPY pom.xml pom.xml
run mvn install
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:11.0.5-jre-stretch
ENV CHORD_NODE_TYPE=central
WORKDIR /app/
COPY --from=build /home/ChordAkka/target/ /app/
CMD java -jar chord-1.0-allinone.jar -Dconfig.resource=/centralNode.conf