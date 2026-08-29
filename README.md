# MedPortal

Enterprise mediation portal — **Angular 21** + **Java 25** / **Spring Boot 4.0.6**.

## Canonical branch

**`main`** is the single source of truth. It carries the Angular 21 / Java 25 / Spring Boot 4 stack,
the ported BPMN editor, and the security + login hardening.

There is no separate implementation branch any more: the former `cursor/medportal-complete-b7d5`
branch has been merged into `main` and deleted. Its security and login work is described in
[`docs/upgrade/complete-branch-security-login.md`](docs/upgrade/complete-branch-security-login.md),
kept as a record of what landed.

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

`docs/upgrade/` — [`complete-branch-security-login.md`](docs/upgrade/complete-branch-security-login.md)
records the security and login hardening that landed with the platform upgrade.
`CHANGELOG-UPGRADE.md` lists the upgrade itself release by release.

## Security docs

`docs/security/` — [`deferred-remediations.md`](docs/security/deferred-remediations.md)
covers the four findings from the security review that are deliberately not fixed in code
because they need an infrastructure or product decision first: per-JVM session state,
per-request authority lookups, the ineffective XSS input filter, and `unsafe-inline` in the
production CSP.
