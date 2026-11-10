FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q clean package -DskipTests

FROM eclipse-temurin:21-jre
ENV TZ=America/Sao_Paulo
WORKDIR /app
COPY --from=build /app/target/vagaviva-api.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=60", "-XX:TieredStopAtLevel=1", "-Duser.timezone=America/Sao_Paulo", "-jar", "app.jar"]
