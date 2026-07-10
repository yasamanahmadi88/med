# MedPortal multi-stage image (Java 25 + Angular 21)
# Secrets must be supplied at runtime; never bake JWT secrets into the image.

FROM node:22-bookworm-slim AS frontend
WORKDIR /workspace
COPY package.json package-lock.json ./
COPY angular.json tsconfig.json tsconfig.app.json tsconfig.spec.json ngsw-config.json webpack ./ 
COPY webpack ./webpack
COPY src/main/webapp ./src/main/webapp
RUN npm ci --no-audit --no-fund \
  && npm run webapp:build:prod

FROM eclipse-temurin:25-jdk-noble AS backend
WORKDIR /workspace
COPY mvnw pom.xml ./
COPY .mvn ./.mvn
COPY src ./src
COPY --from=frontend /workspace/target/classes/static ./src/main/webapp/static-built
# Prefer Maven webapp packaging; skip npm inside Maven when static assets are prebuilt
RUN chmod +x mvnw \
  && ./mvnw -ntp -Pprod,webapp -Dskip.installnodenpm -Dskip.npm -DskipTests -Dmodernizer.skip=true package \
  && cp target/med-portal-*.jar /workspace/app.jar

FROM eclipse-temurin:25-jre-noble AS runtime
RUN useradd --system --create-home --uid 10001 medportal
WORKDIR /app
COPY --from=backend /workspace/app.jar /app/app.jar
USER medportal
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD bash -c 'exec 3<>/dev/tcp/127.0.0.1/8080 && printf "GET /management/health HTTP/1.0\r\nHost: localhost\r\n\r\n" >&3 && cat <&3 | head -n1 | grep -q "200\|401\|403"'
ENTRYPOINT ["java","-jar","/app/app.jar"]
