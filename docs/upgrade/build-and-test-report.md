# Build and Test Report

## Environment

| Tool | Version |
|------|---------|
| JDK | OpenJDK 25.0.3 (`JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64`) |
| Node | v22.14.0 |
| npm | 10.9.7 |
| Maven Wrapper | 3.9.16 |
| Docker | **Not installed** in agent environment |
| Oracle DB | Not reachable (internal host in YAML) |

## Commands executed (selected)

```bash
./mvnw -ntp -Dskip.installnodenpm -Dskip.npm -DskipTests compile
./mvnw -ntp -Dskip.installnodenpm -Dskip.npm -Dtest=TokenProviderSecurityTest,WebConfigurerTest test
./mvnw -ntp -Dskip.installnodenpm -Dskip.npm -Dtest=TokenProviderSecurityTest,JWTFilterTest,ResourceSecuredAuthorizationManagerTest test
./mvnw -ntp -Dskip.installnodenpm -Dskip.npm -DskipTests test-compile
./mvnw -ntp -Dskip.installnodenpm -Dskip.npm -DskipTests package
npx playwright test
npm install --no-fund --no-audit
npm run lint
npx ng test --watch=false --coverage=false
npm run webapp:build:prod
npx vitest run src/main/webapp/app/core/theme/theme.service.spec.ts --config vitest-base.config.ts --environment jsdom
```

## Results

| Check | Result | Notes |
|-------|--------|-------|
| Backend compile (Java 25, Boot 4.0.6) | **PASS** | MapStruct unmapped warnings only |
| Security unit tests (27) | **PASS** | `TokenProviderSecurityTest`, `JWTFilterTest`, `ResourceSecuredAuthorizationManagerTest` |
| `WebConfigurerTest` (5) | **PASS** | Tomcat factory API updated |
| Backend Modernizer | **PASS** without skip | `./mvnw modernizer:modernizer` and package without `-Dmodernizer.skip` |
| Frontend `npm install` | **PASS** | Public npm registry (private Artifactory unavailable) |
| Frontend lint | **PASS** | `npm run lint` |
| Full Angular/Vitest suite | **PASS** | 629 passed, 0 failed, 0 skipped |
| Frontend production build | **PASS** | `npm run webapp:build:prod`; Sass deprecation warnings may remain |
| Playwright E2E | **PASS locally (14)** | `npx playwright test`; also wired in CI |
| IntegrationTest / Failsafe (local H2) | **Blocked** | Context reaches Hibernate validation, then fails because local `schema-test.sql` lacks `MEDIATION.TBL_FLOWS` and related mapped tables |
| Oracle Testcontainers | **Configured; CI required** | Local agent has no Docker; CI runner must execute `./mvnw -Poracle-testcontainers verify` |
| Docker image build | **Configured; CI required** | Local agent has no Docker; workflow builds when `Dockerfile` exists |
| Live login smoke | **Blocked** | No DB |

## Version verification

| Component | Verified value |
|-----------|----------------|
| Spring Boot parent | 4.0.6 |
| `java.version` | 25 |
| `@angular/core` | 21.2.14 |
| `@angular/cli` | 21.2.12 |

## Pre-existing vs upgrade-induced

- Missing original `package.json` on `main` — reconstructed then replaced by Angular 21 lockfile
- Modernizer violations largely in legacy IT code carried from prior upgrade attempt
- bpmn-js vendor TS under `content/bpmnjs` excluded from unit-test tsconfig
