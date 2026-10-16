FROM maven:3.9.12-eclipse-temurin-25 AS build

WORKDIR /workspace

COPY pom.xml ./
COPY src ./src

RUN mvn -B -DskipTests package && cp target/*.jar /tmp/app.jar

FROM eclipse-temurin:25-jre

WORKDIR /app

ENV JAVA_TOOL_OPTIONS="-Duser.timezone=America/Bogota"

COPY --from=build /tmp/app.jar app.jar

EXPOSE 10000

ENTRYPOINT ["java", "-jar", "app.jar"]
