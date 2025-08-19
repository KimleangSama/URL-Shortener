FROM eclipse-temurin:21-alpine
LABEL maintainer="Kimleang Kea"

# Install curl for healthcheck
RUN apk add --no-cache curl

# Copy JAR into image
COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]

# Use shell form so || works
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

VOLUME /tmp
