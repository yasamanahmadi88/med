# Build and Test Report

## Environment

| Tool | Version |
|------|---------|
| JDK | OpenJDK 25.0.3 (`JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64`) |
| Node | v22.14.0 |
| npm | 10.9.7 |
| Maven Wrapper | 3.9.16 |
| Docker | **Not installed** in agent environment |
| Oracle DB | Not reachable locally (use CI Testcontainers) |

## Commands executed (this continuation)

```bash
npx ng test --watch=false --coverage=false --include='**/main.component.spec.ts'
./mvnw -ntp -Dskip.installnodenpm -Dskip.npm -Dtest=TokenProviderSecurityTest,JWTFilterTest,ResourceSecuredAuthorizationManagerTest,WebConfigurerTest test
./mvnw -ntp -Dskip.installnodenpm -Dskip.npm test-compile failsafe:integration-test failsafe:verify -Dit.test=AccountResourceIT
```

## Results

| Check | Result | Notes |
|-------|--------|-------|
| Backend compile (Java 25, Boot 4.0.6) | **PASS** | MapStruct unmapped warnings only |
| Security unit tests (32) | **PASS** | TokenProvider + JWTFilter + ResourceSecuredAuthorizationManager + WebConfigurer |
| Backend Modernizer | **PASS** without skip | Previously fixed; still required in CI |
| Frontend MainComponent Vitest | **PASS** | 10/10 after null-safe `router.url` |
| Full Angular/Vitest suite (prior) | **PASS** | 629 passed, 0 failed, 0 skipped |
| Frontend lint / prod build (prior) | **PASS** | |
| Playwright E2E (prior) | **PASS locally (14)** | Wired in CI |
| `AccountResourceIT` (H2) | **PASS** | 32/32 after schema mapping, seed data, security PathPattern, password policy, Jackson Instant |
| Full Failsafe suite (all `*IT`) | **Not fully re-run this turn** | `AccountResourceIT` green; remaining ITs should run in CI `maven-h2-it` / Oracle jobs |
| Oracle Testcontainers | **CI required** | No Docker in agent |
| Docker image build | **CI required** | Dockerfile fixed (webpack `package.json` overwrite) |

## Fixes in this continuation

1. **MainComponent** — `updateLayoutState` null-safe; MockRouter provides `url`.
2. **Dockerfile** — do not `COPY webpack` into WORKDIR (overwrote root `package.json` with `{"type":"commonjs"}`).
3. **JPA `@Table`** — use `schema = "MEDIATION", name = "TBL_*"`; H2 URL `INIT=CREATE SCHEMA IF NOT EXISTS MEDIATION`; test DDL `create-drop` + `test-data.sql`.
4. **Security** — PathPattern-safe `/app/**`; public register/reset/auth; exclude Zalando Problem security/Jackson autoconfig incompatible with Boot 4 / Security 7.
5. **AccountResourceIT** — passwords meet policy; reset keys 20-char alnum; reset-init reads raw body; invalid reset key expects 400 per controller contract.
6. **CI** — split unit+modernizer vs H2 failsafe vs Oracle Testcontainers vs Docker.

## Residual merge risk (do not claim full success until CI green)

| Gap | Why | Command / infra |
|-----|-----|------------------|
| Full `./mvnw clean verify` locally | Agent has no Oracle; full IT matrix not re-executed end-to-end this turn | CI `maven-h2-it` + `oracle-testcontainers` |
| Docker build/compose smoke | No Docker daemon | CI `docker-image` job |
| Oracle Liquibase on real XE/Free | Needs Testcontainers pull | `./mvnw -Poracle-testcontainers verify` on runner with Docker |
| PR Ready for Review | Requires all workflow jobs green | Watch Actions on PR #2 |

## Version verification

| Component | Verified value |
|-----------|----------------|
| Spring Boot parent | 4.0.6 |
| `java.version` | 25 |
| `@angular/core` | 21.2.14 |
| `@angular/cli` | 21.2.12 |
