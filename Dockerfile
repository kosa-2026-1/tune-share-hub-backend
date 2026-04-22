ARG JAVA_VERSION=21

FROM eclipse-temurin:${JAVA_VERSION}-jdk AS build
WORKDIR /workspace

COPY . .

RUN set -eux; \
    if [ -f "./gradlew" ]; then \
      chmod +x ./gradlew; \
      ./gradlew bootJar -x test; \
      JAR_PATH="$(find build/libs -type f -name '*.jar' ! -name '*plain.jar' | head -n 1)"; \
    elif [ -f "./mvnw" ]; then \
      chmod +x ./mvnw; \
      ./mvnw -DskipTests package; \
      JAR_PATH="$(find target -type f -name '*.jar' | head -n 1)"; \
    else \
      echo "No Gradle or Maven wrapper found. Add gradlew or mvnw before building this image."; \
      exit 1; \
    fi; \
    test -n "$JAR_PATH"; \
    cp "$JAR_PATH" /workspace/app.jar

FROM eclipse-temurin:${JAVA_VERSION}-jre
WORKDIR /app

RUN addgroup --system spring && adduser --system --ingroup spring spring

COPY --from=build /workspace/app.jar /app/app.jar

USER spring
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
