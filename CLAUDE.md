# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

MedPortal — a JHipster-originated (v7.9.3) monolith, since manually upgraded off the generator to
**Angular 21** + **Java 25** / **Spring Boot 4.0.6**. Backend: `com.behsa.medportal`, Maven build.
Frontend: Angular app under `src/main/webapp`, served embedded from the Spring Boot jar in
production. Database: Oracle (`devDatabaseType: oracle`), Liquibase-managed schema.

`main` is the single source of truth (see README.md). There is no separate long-lived
implementation branch.

## Build & run

```bash
# Backend (requires JDK 25 + Oracle credentials)
export SPRING_DATASOURCE_URL='jdbc:oracle:thin:@//localhost:1521/FREEPDB1'
export SPRING_DATASOURCE_USERNAME=MEDIATION
export SPRING_DATASOURCE_PASSWORD=MEDIATION
export SECURITY_AUTHENTICATION_JWT_BASE64_SECRET="$(openssl rand -base64 64)"
./mvnw -Pdev,webapp

# Frontend only, against a running backend
npm ci && npm start          # ng serve --hmr on port 9060 (see angular.json)
```

Docker Compose smoke test: `export SECURITY_AUTHENTICATION_JWT_BASE64_SECRET="$(openssl rand -base64 64)"; ./scripts/docker-compose-smoke.sh`.

### Packaging

- `npm run java:jar:dev` / `npm run java:jar:prod` — build a runnable jar (`-Pdev,webapp` / `-Pprod`).
- `npm run java:war:dev` / `:prod` — WAR packaging (`-Pwar`).
- `npm run java:docker:dev` / `:prod` — jib-built Docker image (`-DskipTests jib:dockerBuild`).
- The Maven `webapp` profile drives the Angular production build into `target/classes/static/`
  as part of `./mvnw -Pprod` (or `-Pdev,webapp` for a dev-profile backend serving a built frontend).

## Tests

### Backend (JUnit, under `src/test/java`)

```bash
./mvnw test                                   # full backend unit test run
./mvnw test -Dtest=BpmnElementAccessServiceTest         # single test class
./mvnw test -Dtest=BpmnElementAccessServiceTest#methodName   # single test method
npm run backend:unit:test                     # same, with logging suppressed (CI-style)
npm run backend:nohttp:test                   # checkstyle:check
npm run backend:doc:test                      # javadoc:javadoc
```

`checkstyle.xml` governs Java style; it runs as part of `ci:backend:test` and Maven `verify`.

### Frontend (Vitest via Angular's unit-test builder)

```bash
npm test                                      # ng test --coverage (runs `npm run lint` first via pretest)
npx ng test --include='**/bpmn-editor.component.spec.ts'   # single spec file
npm run test:watch                            # watch mode
```

Test runner config lives in `angular.json` under `projects.med-portal.architect.test`
(`runner: vitest`, `tsConfig: tsconfig.spec.json`) — there is no separate `vitest.config.ts`.

### E2E (Playwright)

```bash
npm run e2e                    # playwright test — spins up ng serve on 127.0.0.1:9060 itself (see playwright.config.ts)
npm run e2e:playwright:ui      # interactive UI mode
npm run ci:e2e:dev             # full stack (mvnw app:start) + e2e, used in CI
```

Specs live in `e2e/playwright`. `data-cy` is the configured `testIdAttribute`.

### Lint / format

```bash
npm run lint            # eslint .
npm run lint:fix
npm run prettier:check  # covers md/json/yml/js/ts/java/html/css/scss under src, webpack, .blueprint
npm run prettier:format
```

Prettier: `printWidth: 140`, single quotes, `arrowParens: avoid`; Java files get `tabWidth: 4`
via an override (`.prettierrc`). `npm test`'s `pretest` hook already runs lint — don't assume a
green `npm test` implies lint passed without it, and don't skip lint separately when running the
full test script.

## Architecture

### Backend layering

Standard JHipster layering under `src/main/java/com/behsa/medportal/`:
`domain` (JPA `*Entity`) → `repository` (Spring Data) → `service` (+ `service/dto/*DTO`,
`service/mapper` for entity↔DTO) → `web/rest/*Resource` (REST controllers). Also:
`security` (JWT/session/captcha), `config` (Spring config, Liquibase, CORS, security headers),
`filter` (servlet filters), `aop`, `management` (actuator customizations), `vaidators`
(bean validators — note the existing misspelling, don't "fix" it in unrelated changes).

Liquibase changelogs live under `src/main/resources/config/liquibase`, indexed from
`master.xml`. Schema-affecting backend changes need a new changelog file referenced there.

### RBAC / resource-authority model

This app has two independent, non-overlapping authorization systems — know which one a change
belongs to before touching it:

1. **API resource authorization** (`MedAuthorityEntity` / `ResourceEntity` /
   `ResourceAuthorityEntity`): a `MedAuthorityEntity` (role, optionally hierarchical via
   `parent`) is joined to a `ResourceEntity` (an API endpoint/URI) through
   `ResourceAuthorityEntity`, which also carries a `Verb`. `PortalUser` (extends Spring
   Security's `User`) carries the resolved `resourceAuthorities` on the principal.
   `TokenProvider.getAuthentication` rebuilds this from the DB on every request (no cache) —
   see "Known limitations" below before changing that path. `ROLE_ADMIN` bypasses resource RBAC
   entirely, both backend and frontend (`*jhiHasPermission` directive).
2. **BPMN palette/element access** (`BpmnElementEntity` / `BpmnElementGroupEntity`,
   resolved by `BpmnElementAccessService`): resolves **Portal Owner → Element Groups →
   BPMN Elements**. Neither the authenticated user's role nor the Flow's Product participates in
   this resolution — it is deliberately owner-scoped only, per the class javadoc. Don't wire
   role or product checks into this path without confirming that's an intended behavior change.

`FlowEntity` belongs to a `ProductEntity` (joined by `product_name`, not by id). `ModuleEntity`
groups `ConfigEntity` rows and models a deployable unit (DNS name, ports, logging).

### BPMN editor (Angular)

`src/main/webapp/app/bpmn-editor/` is a substantial Angular port of a bpmn-js-based visual
editor (see recent commits: "complete Vue-to-Angular editor parity", "enforce product-based
element access and flow persistence"). Structure:

- `additional-modules/` — custom bpmn-js/diagram-js modules: `ElementAccess` (palette/replace-menu
  filtering driven by the access model above), `Palette` (`RewritePalette`), `Renderer`,
  `Rules`, `ColorPicker`, `ElementFactory`.
- `components/` — `designer` (hosts the bpmn-js `Modeler`), `flow` (`flow-bpmn-editor`, the page
  that loads/saves a `FlowEntity`'s XML), `toolbar`, `panel`/`module-panel` (properties panel),
  `context-menu`, `palette`, `settings`.
- `services/bpmn-element-access.service.ts` — frontend counterpart of
  `BpmnElementAccessResource`/`BpmnElementAccessService`; gates which palette entries/elements a
  user can place based on the resolved owner access DTO.
- `services/role-modules.service.ts` — separate role→module resolution used elsewhere in the
  editor; don't conflate with element access.
- `moddle-extensions/`, `types/bpmn-moddle/` — custom BPMN moddle schema extensions and their
  TS types.
- `module-properties/schemas/` — JSON-schema-driven property panel definitions per BPMN element
  type.

When changing what the palette shows or what a user can drop on the canvas, the flow generally
runs backend (`BpmnElementAccessResource` → `BpmnElementAccessService` →
`BpmnElementAccessDTO`) → frontend (`bpmn-element-access.service.ts` →
`ElementAccessPaletteFilter.ts` / `ElementAccessReplaceMenuFilter.ts` →
`rewritePaletteProvider.ts`). Check all four layers for consistency, not just one.

### Login/session flow

1. `POST /api/authenticate` — captcha + rate limit + Spring auth + JWT + server-side session
   entry (`SecurityCache`).
2. Angular `LoginService` stores the JWT, then `AccountService.identity(true)` loads
   `/api/account`, which hydrates `resourceAuthorities` for the UI.
3. `*jhiHasPermission` gates menus/routes off those authorities; `ROLE_ADMIN` bypasses it.
4. `POST /api/auth/logout` clears both the server session and client auth state.

`JWTFilter` requires a live entry in `SecurityCache` in addition to a validly signed token —
a correctly signed, unexpired JWT with no matching session is rejected. This is intentional
(supports server-side revocation) but means session state is per-JVM; see below.

## Known architectural limitations (deliberately not fixed — see docs/security/deferred-remediations.md)

Before "fixing" any of these, read that document — each one is a recorded, reasoned decision,
not an oversight:

- `SecurityCache` (sessions, login rate-limit buckets, captcha state) is per-JVM
  (`ConcurrentHashMap`), so this app cannot run on more than one node without sticky sessions or
  a shared store. A restart logs everyone out.
- Every authenticated request re-resolves the user and their resource authorities from the
  database (no caching) — freshness over throughput, by design; see the doc for the
  recommended fix (delete the `findOne` N+1 loop in `ResourceAuthorityQueryService`, don't add
  a cache without also addressing invalidation).
- `XssSanitizingFilter`/`XssRequestWrapper` only touch `getParameter*`, which this JSON REST API
  never uses via `@RequestBody` — treat it as effectively inert, not as XSS coverage. The actual
  unescaped sink is `TranslateDirective`'s `innerHTML` assignment.
- Production CSP still allows `script-src 'unsafe-inline'` because of Angular's critical-CSS
  inlining (`inlineCritical`) and two hand-written inline `<script>` blocks in `index.html`.

## Other docs worth knowing about

- `docs/upgrade/complete-branch-security-login.md` — record of the security/login hardening
  that landed with the Angular 21/Java 25/Spring Boot 4 upgrade (JWT `PartyId` handling, account
  RBAC hydration, remember-me TTL, `/api/register` locked to `denyAll`, etc.).
- `CHANGELOG-UPGRADE.md` — the platform upgrade itself, release by release.
- `sonar-project.properties` — SonarQube analysis config, if running static analysis locally.
