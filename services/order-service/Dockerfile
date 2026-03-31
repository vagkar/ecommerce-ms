# Stage 1: Build
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

ENV MAVEN_OPTS="-Dhttps.protocols=TLSv1.2,TLSv1.3"

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

COPY src/ src/
RUN for i in 1 2 3; do ./mvnw package -DskipTests -q && break || (echo "Attempt $i failed, retrying in 15s..." && sleep 15); done

# Stage 2: Runtime
FROM eclipse-temurin:25-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]