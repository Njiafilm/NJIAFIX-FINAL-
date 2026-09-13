# --- Hatua ya 1: Jenga (build) mradi kwa Maven ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Nakili pom.xml kwanza ili Docker ihifadhi (cache) dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Nakili source code kisha jenga .jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# --- Hatua ya 2: Endesha (runtime) - image ndogo, JRE tu ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/target/njiafix-java.jar app.jar

# Render inatoa PORT yake yenyewe kupitia environment variable
ENV SPRING_PROFILES_ACTIVE=render
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
