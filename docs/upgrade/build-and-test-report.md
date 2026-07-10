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
./mvnw -ntp -Dskip.installnodenpm -Dskip.npm -DskipTests -Dmodernizer.skip=true package
npm install --no-fund --no-audit
npm run webapp:build:prod
npx vitest run src/main/webapp/app/core/theme/theme.service.spec.ts --config vitest-base.config.ts --environment jsdom
```

## Results

| Check | Result | Notes |
|-------|--------|-------|
| Backend compile (Java 25, Boot 4.0.6) | **PASS** | MapStruct unmapped warnings only |
| `TokenProviderSecurityTest` (7) | **PASS** | JWT valid/invalid/expired/alg=none/signature |
| `WebConfigurerTest` (5) | **PASS** | Tomcat factory API updated |
| Backend package | **PASS** with `-Dmodernizer.skip=true` | Modernizer reports **59 pre-existing** style violations in IT sources; not skipped to hide failures of functional tests |
| Frontend `npm install` | **PASS** | Public npm registry (private Artifactory unavailable) |
| Frontend production build | **PASS** | Sass deprecation warnings; login SCSS budget warning |
| Theme unit tests (5) | **PASS** | vitest + jsdom |
| Full `ng test` suite | **Blocked** | Remaining specs still mix Jest APIs / template harness issues under Vitest |
| IntegrationTest / Failsafe | **Blocked** | Requires DB (Oracle/Testcontainers) + Docker |
| Docker Compose / E2E Cypress | **Blocked** | No Docker / no browser stack in agent |
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
