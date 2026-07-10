# Migration Plan — Angular 21 / Java 25 / Spring Boot 4.0.6

## Principles

- Preserve portal UI, menus, routes, and API contracts.
- Keep custom resource-based authorization (`CustomAccessDecisionManager` behavior).
- Atomic, reversible commits per stage.
- No force-push, no secret commits, no skipped tests to fake green builds.
- Spring Boot upgrades are staged: 2.7.x → 3.x → 3.5.x → **4.0.6** (exact).
- Angular upgrades are staged major-by-major: 14 → … → **21**.

## Stage A — Baseline (this commit series)

1. Assemble source from RAR + `med-upgrade` roots.
2. Reconstruct missing `package.json`.
3. Publish baseline / route inventory docs.
4. Attempt baseline builds/tests; document blockers.

## Stage B — Backend

1. Align to latest Spring Boot **2.7.x** if needed (already on 2.7.3).
2. Prepare Security for Boot 3 (lambda DSL already partially present).
3. Migrate to Spring Boot **3.x** + `javax` → `jakarta`.
4. Climb to latest compatible **3.5.x**.
5. Remove deprecated APIs.
6. Upgrade to Spring Boot **4.0.6** exactly.
7. Set Java toolchain to **25**.
8. Re-implement method security as Spring Security 6+ `AuthorizationManager` while preserving resource/verb rules.
9. Restore `DatabasePortalConfiguration` with Jakarta Persistence.

## Stage C — Frontend

1. Upgrade Angular majors 14→15→16→17→18→19→20→21 (or equivalent verified platform jump with per-major commits when `ng update` path is blocked by missing lockfile).
2. Keep NgModule architecture unless a migration forces a narrow change.
3. Fix SCSS imports, builders, polyfills, third-party libs (`ngx-*`, Bootstrap, Font Awesome).
4. Verify routing/menus; add route tests.
5. Add light/dark theme with Navbar toggle + persistence.

## Stage D — Security & Tests

1. JWT validation hardening (alg allowlist, exp, signature, no `alg=none`).
2. CORS from configuration; document CSRF decision (Bearer JWT → CSRF disabled with rationale).
3. Security integration tests (401/403, bad tokens, CORS).
4. Frontend route/theme/auth tests; Playwright or Cypress smoke if feasible.

## Stage E — Packaging

1. Update Docker base images (Java 25, Node for Angular 21) when Docker available.
2. Document CI recommendations (no pipeline files in baseline).
3. Final build/test report + rollback guide + CHANGELOG.

## Rollback

See `docs/upgrade/rollback-guide.md`. Primary rollback: reset/checkout previous commit on this branch or revert merge of the PR; never force-push `main`.
