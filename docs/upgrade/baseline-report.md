# MedPortal Upgrade Baseline Report

**Date:** 2026-07-10  
**Branch:** `upgrade/angular21-java25-springboot-4.0.6`  
**Base commit (main):** `85819ff` — *Add files via upload*  
**Default branch:** `main`  
**Remote:** `https://github.com/yasamanahmadi88/med`

## 0. Git / Repository Protection

| Item | Value |
|------|-------|
| Working tree at start | Clean on `main` |
| Submodules | None |
| Nested repos | None |
| Layout | Single monolith (JHipster), not a multi-repo monorepo |
| Source delivery | `src-for-medPortal.rar` on `main`; root build files on `origin/med-upgrade` |
| Prior upgrade attempt | `origin/cursor/jdk25-angular21-upgrade-7bc7` (single commit; dropped 2 custom security/DB files) |
| Uncommitted user changes | None |
| Upgrade branch | Created from `main`, then baseline assembled |

### Baseline assembly sources (verified)

1. Root configs from `origin/med-upgrade` (`pom.xml`, `angular.json`, `.yo-rc.json`, Docker stubs, ESLint/Prettier, tsconfig)
2. Application `src/` extracted from `src-for-medPortal.rar`
3. `webpack/` extracted from `webpack.rar` on `origin/med-upgrade`
4. Maven wrapper taken from prior upgrade branch (wrapper only; not application code)
5. **`package.json` was missing** from all branches except the prior upgrade target; reconstructed from JHipster 7.9.3 defaults + imports found in source (`ngx-toastr`, `bpmn-js`, Font Awesome, ng-bootstrap)

## 1. Architecture Summary

- **Type:** JHipster 7.9.3 monolith (`applicationType: monolith`)
- **Auth:** JWT (`authenticationType: jwt`)
- **Frontend:** Angular (NgModule architecture), Bootstrap 5, ng-bootstrap, Font Awesome, ngx-webstorage, ngx-toastr, bpmn-js assets
- **Backend:** Spring Boot + Spring Security + JPA + Liquibase + Ehcache
- **Database:** Oracle (dev + prod JDBC URLs in YAML)
- **i18n:** English + Persian (`en`, `fa`)
- **Package:** `com.behsa.medportal`
- **Ports:** Backend `8080`, Angular dev `4200`

## 2. Version Matrix (pre-upgrade)

| Component | Current (detected) | Target | Status |
|-----------|-------------------:|-------:|--------|
| Angular | 14.2.0 (reconstructed; JHipster 7.9.3) | 21 | Pending |
| Angular CLI | 14.2.1 (reconstructed) | 21 | Pending |
| Node.js | >=16.17.0 (pom/node.version `v16.17.0`) | Compatible with Angular 21 (20+) | Pending |
| TypeScript | 4.8.2 (reconstructed) | Compatible with Angular 21 | Pending |
| RxJS | 7.5.6 (reconstructed) | Compatible | Pending |
| Zone.js | 0.11.6 (reconstructed) | Compatible | Pending |
| Java | 11 (`pom.xml`) | 25 | Pending |
| Spring Boot | **2.7.3** (exact) | **4.0.6** | Pending |
| Spring Security | Managed by JHipster BOM 7.9.3 / Boot 2.7.3 | BOM-managed for Boot 4.0.6 | Pending |
| JHipster | 7.9.3 | Framework 9.x compatible with Boot 4 | Pending |
| Maven | Wrapper 3.9.16 available; project requires >=3.2.5 | Compatible | Pending |
| Hibernate | 5.6.10.Final | Boot-managed (Jakarta) | Pending |
| Liquibase | 4.15.0 | Boot-managed | Pending |
| Oracle JDBC | ojdbc8 (profiles) | ojdbc11 / BOM-compatible | Pending |
| JJWT | via JHipster deps | 0.12.x for Boot 3/4 | Pending |

## 3. Frontend Inventory

| Area | Finding |
|------|---------|
| Workspace | Single app `med-portal` in `angular.json` |
| Source root | `src/main/webapp` |
| Builder | `@angular-builders/custom-webpack` |
| UI libs | Bootstrap SCSS, `@ng-bootstrap/ng-bootstrap`, Font Awesome — **no Angular Material / PrimeNG / Tailwind** |
| Package manager | npm (`.npmrc` originally pointed at private Artifactory) |
| Routing | `AppRoutingModule` + lazy `AdminRoutingModule`, `AccountModule`, `LoginModule`, `EntityRoutingModule` |
| Guards | `UserRouteAccessService` |
| Interceptors | `httpInterceptorProviders` under `app/core/interceptor` |
| State | No NgRx; services + ngx-webstorage |
| JWT storage | `localStorage` / `sessionStorage` via ngx-webstorage (`authenticationToken`) |
| Environments | Webpack custom env; prior upgrade added `environments/*.ts` |
| Theme | None (light only) |

## 4. Backend Inventory

| Area | Finding |
|------|---------|
| Entry | `com.behsa.medportal.MedPortalApp` |
| Build | Maven (`pom.xml`), no Gradle |
| Security | `SecurityConfiguration` (filter chain), JWT filter/provider, captcha, XSS filters, `SecurityCache` |
| **Custom authZ** | `CustomAccessDecisionManager` + `MethodSecurityConfiguration` (resource+verb checks via `@Secured("resourceName")`) |
| DB config | `DatabaseConfiguration` + **`DatabasePortalConfiguration`** (primary DS/EMF) |
| API style | Spring MVC REST under `/api/**` |
| Migrations | Liquibase changelogs under `src/main/resources/config/liquibase` |
| Cache | Ehcache |
| OpenAPI | Swagger UI assets present; api-docs profile |
| Actuator | `/management/**` |
| Tests | ~40 `*Test.java`, ~21 `*IT.java`, ~125 `*.spec.ts` |
| Docker | `Dockerfile`, `docker-compose.yml`, `src/main/docker/*` |
| CI/CD | No GitHub Actions / Jenkinsfile found in baseline |

## 5. Critical Gaps / Risks Before Upgrade

1. **`package.json` / lockfile missing** on `main` and `med-upgrade` — baseline frontend install uses reconstructed deps; lockfile integrity starts after first successful `npm install`.
2. **Private npm registry** in original `.npmrc` — switched to `registry.npmjs.org` for this branch (documented).
3. **Oracle dependency** — local full-stack smoke may need Testcontainers/H2 for tests; prod URLs point to internal hosts.
4. **Prior upgrade branch** dropped `CustomAccessDecisionManager.java` and `DatabasePortalConfiguration.java` and simplified method security — **must be preserved/migrated**, not discarded.
5. **`.yo-rc.json` contains `jwtSecretKey`** — pre-existing in repo history; no new secrets will be added.
6. **Java 25 / Docker** — JDK 25 installed in agent environment; Docker CLI not available in this environment.

## 6. Baseline Test Plan Status

| Check | Result |
|-------|--------|
| Frontend install | Pending (after baseline commit) |
| Frontend prod build | Pending |
| Frontend unit tests | Pending |
| Backend compile (Java 11 toolchain) | Pending |
| Backend tests | Pending (Oracle/H2 constraints expected) |
| Docker Compose | **Blocked** — Docker not installed in agent environment |
| App runtime smoke | Pending / may be blocked without DB |

Pre-existing failures will be recorded separately from upgrade-induced failures in `docs/upgrade/build-and-test-report.md`.
