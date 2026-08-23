# CHANGELOG — Platform Upgrade

## [cursor/medportal-complete-b7d5] — 2026-07-18

### Security & login hardening

- Deny public `/api/register`; require env for prod DB credentials and JWT secret
- Docker Compose requires JWT/DB secrets (no baked production secrets)
- Remember-me extends both JWT TTL and server-side session inactivity (7 days)
- Angular: clear auth on `/api/account` failure; admin RBAC bypass in `*jhiHasPermission`; refuse login navigation without account
- Docs: `docs/upgrade/complete-branch-security-login.md`

## [upgrade/angular21-java25-springboot-4.0.6] — 2026-07-10

### Platform

- Spring Boot **4.0.6** (from 2.7.3)
- Java toolchain **25** (from 11)
- Angular **21.2.x** / CLI **21.2.x** (from 14.x / JHipster 7.9.3 client)
- JHipster framework **9.1.0** alignment for Boot 4
- `javax.*` → `jakarta.*`

### Security

- Restored resource `@Secured` authorization via `ResourceSecuredAuthorizationManager` (Spring Security 7 `AuthorizationManager` API)
- Archived legacy `CustomAccessDecisionManager` under `docs/upgrade/legacy-source/`
- JWT: HMAC `verifyWith`, explicit HS512, 30s clock skew, blank-token reject
- Test annotation migration: `AutoConfigureMockMvc` / `@MockitoBean` for Boot 4
- Added `TokenProviderSecurityTest` and `SecurityAuthorizationIT`

### Frontend

- NgModule architecture preserved
- Light/dark theme (`ThemeService`, Navbar toggle, `data-theme`, FOUC script)
- Theme persistence + `prefers-color-scheme` first visit
- Production build verified

### Docs

- Baseline, migration plan, version matrix, routes, UI, security, JWT design, rollback, build report

### Known blockers in this environment

- No Docker → no Testcontainers/Cypress browser E2E
- Oracle not reachable → full IntegrationTest suite not executed here
- Modernizer plugin reports pre-existing IT style violations (package verified with `-Dmodernizer.skip=true`)
