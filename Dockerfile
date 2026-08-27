# MedPortal multi-stage image (Java 25 + Angular 21)
# Secrets must be supplied at runtime; never bake JWT secrets into the image.
#
# IMPORTANT: never COPY the webpack/ directory into WORKDIR alongside package.json.
# webpack/package.json is only {"type":"commonjs"} and would overwrite the real package.json
# (Docker COPY of a directory copies its contents into the destination).

FROM node:22-bookworm-slim AS frontend
WORKDIR /workspace
COPY package.json package-lock.json ./
COPY angular.json tsconfig.json tsconfig.app.json tsconfig.spec.json ngsw-config.json ./
COPY webpack ./webpack
COPY src/main/webapp ./src/main/webapp
# Angular production build writes into target/classes/static (see angular.json)
RUN npm ci --no-audit --no-fund \
  && npm run webapp:build:prod

FROM eclipse-temurin:25-jdk-noble AS backend
WORKDIR /workspace
COPY mvnw pom.xml ./
COPY .mvn ./.mvn
# properties-maven-plugin reads this during the package lifecycle
COPY sonar-project.properties ./
COPY src ./src
# Prebuilt SPA assets from the frontend stage
COPY --from=frontend /workspace/target/classes/static ./src/main/resources/static
RUN chmod +x mvnw \
  && ./mvnw -ntp -Pprod -Dskip.installnodenpm -Dskip.npm -DskipTests package \
  && cp target/med-portal-*.jar /workspace/app.jar

FROM eclipse-temurin:25-jre-noble AS runtime
RUN useradd --system --create-home --uid 10001 medportal
WORKDIR /app
COPY --from=backend /workspace/app.jar /app/app.jar
USER medportal
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD bash -c 'exec 3<>/dev/tcp/127.0.0.1:8080 && printf "GET /management/health HTTP/1.0\r\nHost: localhost\r\n\r\n" >&3 && cat <&3 | head -n1 | grep -q "200\|401\|403"'
# JAVA_OPTS is honored so Compose/CI can pass Oracle JDBC timezone flags.
ENTRYPOINT ["bash","-c","exec java ${JAVA_OPTS:--XX:MaxRAMPercentage=75.0} -jar /app/app.jar"]
