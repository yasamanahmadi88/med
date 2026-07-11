# Build and Test Report

## Environment (verified this run)

| Tool | Version |
|------|---------|
| JDK | OpenJDK 25 (`java -version`) |
| Spring Boot parent | **4.0.6** (`./mvnw help:evaluate -Dexpression=project.parent.version`) |
| Maven Wrapper | 3.9.x |
| Node | v22.x |
| npm | 10.x |
| Angular Core / CLI | 21.x (see `package.json` / `npx ng version`) |
| Docker | **Not installed** in agent VM |
| Oracle DB | Not reachable locally (CI Testcontainers job) |

## Commands executed

```bash
./mvnw -ntp -Dskip.installnodenpm -Dskip.npm -Dskip.integration.tests=true test
./mvnw -ntp -Dskip.installnodenpm -Dskip.npm -Dskip.unit.tests=true verify
# Frontend (prior + CI): npm ci && npm run lint && npx ng test --watch=false && npm run webapp:build:prod && npm run e2e:playwright
```

## Results (this continuation)

| Check | Result | Notes |
|-------|--------|-------|
| Backend unit tests (Surefire) | **PASS** | No modernizer skip |
| Backend Failsafe H2 ITs | **PASS** | **380** completed, **0** failures, **0** errors, **0** skipped |
| Modernizer | **PASS** (CI `maven-default`) | Without `-Dmodernizer.skip` |
| Frontend Vitest (prior/local) | **PASS** | 629 passed / 0 failed / 0 skipped |
| Frontend lint + prod build (prior) | **PASS** | |
| Playwright E2E (prior/local) | **PASS** | 14 tests; also in CI |
| Oracle Testcontainers | **CI only** | No Docker in agent |
| Docker image build | **CI only** | Dockerfile copies `sonar-project.properties`; webpack COPY fixed earlier |

## Fixes in this continuation

1. **Failsafe vs Surefire** — `skip.unit.tests` / `skip.integration.tests` properties; CI H2 job uses `-Dskip.unit.tests=true` (not ineffective `-Dsurefire.skip`).
2. **Test seed** — `test-data.sql` grants VIEW/CREATE/EDIT/DELETE on all `@Secured` resources for ROLE_USER/ROLE_ADMIN.
3. **Hikari pool** — test pool size raised from 1 → 10 to avoid deadlock with `@Transactional` + `LoggerService.add` `REQUIRES_NEW` / auth audit.
4. **LoggerService** — audit logging no longer `orElseThrow` when JWT/`PortalUser` absent under `@WithMockUser`.
5. **ExceptionTranslator** — Jackson-3-safe problem payload map (keeps `error.http.*` / `error.validation`); removed unsafe Jackson-2 Problem modules from `JacksonConfiguration`.
6. **Auth ITs** — captcha fields + `CaptchaValidationService` mock; `POST /api/captcha-validate` + `permitAll`.
7. **ConfigDTO** — `@JsonProperty("pValue")` for Jackson 3 bean naming.
8. **DomainUserDetailsService** — restore `UserNotActivatedException`; IT emails use valid domains for commons-validator.
9. **MailServiceIT** — explicit `ArgumentCaptor` (Boot 4 `@Captor` not processed with `@MockitoBean`).
10. **HibernateTimeZoneIT** — normalize H2 ISO timestamp strings.
11. **Dockerfile** — copy `sonar-project.properties` for `properties-maven-plugin`.
12. **bucket4j / user-session** — shared defaults in test `application.yml`.

## Residual merge risk (do **not** claim full success until CI green)

| Gap | Why | Command / infra |
|-----|-----|------------------|
| Oracle Testcontainers verify | Agent has no Docker; CI must pass | CI job `oracle-testcontainers` |
| Production Liquibase on Oracle | IT profile uses Hibernate create-drop (Liquibase↔MedAuthority PK conflict) | Separate schema-alignment work; see `oracle-testcontainers.md` |
| Docker compose smoke | No Docker daemon in agent | CI `docker-image` builds image; compose smoke still manual/CI follow-up |
| PR Ready for Review | Requires **all** workflow jobs green | Watch Actions on PR #2; then undraft |

## Policy

Project is **not** declared “بدون مشکل” until GitHub Actions on PR #2 is fully green (frontend, maven-default, maven-h2-it, oracle-testcontainers, docker-image).
