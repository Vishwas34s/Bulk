FROM maven:3.8.3-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim
COPY  --from=build /target/Bulk-0.0.1-SNAPSHOT.jar Bulk.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/Bulk.jar"]