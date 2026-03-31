# Stage 1: Build
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Force TLSv1.2/1.3 — fixes SSL handshake errors with Maven Central on some Docker setups
ENV MAVEN_OPTS="-Dhttps.protocols=TLSv1.2,TLSv1.3"

# Copy Maven wrapper and POM
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
# Fix Windows line endings (\r\n → \n) so the script runs on Linux
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Copy source and build the JAR (skip tests — tests run in CI, not in Docker build)
COPY src/ src/
RUN for i in 1 2 3; do ./mvnw package -DskipTests -q && break || (echo "Attempt $i failed, retrying in 15s..." && sleep 15); done

# Stage 2: Runtime — only the JDK and the JAR, no build tools
FROM eclipse-temurin:25-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]