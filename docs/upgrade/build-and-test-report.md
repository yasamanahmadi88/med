# Build and Test Report

## Environment (verified this run)

| Tool | Version |
|------|---------|
| JDK | OpenJDK 25 (`java -version` / CI Temurin 25) |
| Spring Boot parent | **4.0.6** |
| Maven Wrapper | 3.9.x |
| Node | v22.x (CI) |
| npm | 10.x |
| Angular Core / CLI | 21.x |
| Docker | **Not installed** in agent VM; **image build PASS** in CI |
| Oracle DB | CI Testcontainers `gvenzl/oracle-free:slim` |

## CI result (commit `28b3246`, workflow success)

| Job | Result | Counts |
|-----|--------|--------|
| Angular lint, unit, prod build, Playwright | **PASS** | Vitest **629** passed / 0 failed / 0 skipped (126 files); Playwright **14** passed; lint + prod build OK |
| Java 25 Maven unit tests and modernizer | **PASS** | Surefire **80** passed; Modernizer without skip |
| Java 25 H2 integration tests | **PASS** | Failsafe **380** passed / 0 failed / 0 skipped |
| Oracle Testcontainers Maven verify | **PASS** | Failsafe **380** passed / 0 failed / 0 skipped |
| Docker image build | **PASS** | `docker build -t medportal-upgrade-verify:<sha> .` |

## Commands (no skip of quality gates)

```bash
./mvnw -ntp -Dskip.installnodenpm -Dskip.npm -Dskip.integration.tests=true test
./mvnw -ntp -Dskip.installnodenpm -Dskip.npm -Dskip.unit.tests=true verify
./mvnw -ntp -Dskip.installnodenpm -Dskip.npm -Dskip.unit.tests=true -Poracle-testcontainers verify
npm ci && npm run lint && npx ng test --watch=false --coverage=false
npm run webapp:build:prod && npm run e2e:playwright
```

## Fixes that unblocked final CI

1. **HibernateTimeZoneIT** — H2 SqlRowSet path preserved; Oracle uses explicit `TO_CHAR` masks (not NLS default).
2. **Oracle container reuse** — JVM-scoped singleton so `@DirtiesContext` does not restart Oracle Free per IT class.
3. **Workflow concurrency** — cancel superseded runs on the same ref.

## Residual merge risk (honest)

| Gap | Why | Risk |
|-----|-----|------|
| Production Liquibase on Oracle | IT profile disables Liquibase (legacy `jhi_authority` PK vs `MedAuthorityEntity`) | Schema apply on real Oracle still needs a dedicated alignment pass |
| Docker Compose smoke | CI builds image only; agent has no Docker daemon for `compose up` | Runtime compose/healthcheck not exercised in this environment |
| Merge to `main` | Out of scope for this agent | Human review + merge decision |

## Policy

CI on PR #2 is **fully green** at `28b3246`. PR may be marked Ready for Review. Do **not** merge to `main` from this agent.
