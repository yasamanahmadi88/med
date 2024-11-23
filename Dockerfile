FROM node:18-buster-slim as node

COPY package.json /


COPY package.json /

RUN npm i --force
RUN npm run webapp:prod

./mvnw -P'prod,no-liquibase' verify -DskipTests





