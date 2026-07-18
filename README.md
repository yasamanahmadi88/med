# MedPortal

Enterprise mediation portal — **Angular 21** + **Java 25** / **Spring Boot 4.0.6**.

## Canonical branch

Implementation work lives on **`cursor/medportal-complete-b7d5`** (security + login hardened). See `docs/upgrade/complete-branch-security-login.md`.

## Build & run (local)

```bash
# Backend (requires JDK 25 + Oracle credentials)
export SPRING_DATASOURCE_URL='jdbc:oracle:thin:@//localhost:1521/FREEPDB1'
export SPRING_DATASOURCE_USERNAME=MEDIATION
export SPRING_DATASOURCE_PASSWORD=MEDIATION
export SECURITY_AUTHENTICATION_JWT_BASE64_SECRET="$(openssl rand -base64 64)"
./mvnw -Pdev,webapp

# Frontend only
npm ci && npm start
```

## Docker Compose

```bash
export SECURITY_AUTHENTICATION_JWT_BASE64_SECRET="$(openssl rand -base64 64)"
./scripts/docker-compose-smoke.sh
```

## Upgrade docs

`docs/upgrade/` — migration plan, Liquibase/Oracle RCA, empty-forms/RBAC, human review checklist.
