# Human review checklist (upgrade PR)

Reviewer: ________________  Date: ________________

Do **not** merge to `main` until every item is checked or explicitly waived with rationale.

## Security

- [ ] `SecurityConfiguration` path rules and filter order reviewed
- [ ] JWT HS512 secret comes from env/secret manager (no production default)
- [ ] JWT tests cover expired / wrong alg / alg=none / bad signature / 401 vs 403
- [ ] `ResourceSecuredAuthorizationManager` parity with previous access decisions
- [ ] CORS: no `*` with credentials in production
- [ ] CSRF stance documented for bearer-token API
- [ ] Actuator / Swagger exposure acceptable for target environment
- [ ] Secret scan clean (no JWT secrets, DB passwords, API keys in git)

## Database / Liquibase / Oracle

- [ ] Read `docs/upgrade/liquibase-oracle-authority-rca.md`
- [ ] Confirm Authority vs MedAuthority shared-table design is accepted
- [ ] CI job **Oracle Liquibase production-schema verification** green
- [ ] Fresh Oracle + Liquibase + `ddl-auto=none` accepted
- [ ] Upgrade path from legacy name-PK schema accepted
- [ ] Production backup / RMAN plan before applying changelogs
- [ ] Rollback plan documented and understood
- [ ] `20260517_security_default_accounts.xml` behavior accepted (default admin disabled)

## Application / UI

- [ ] Critical routes and menus smoke-tested
- [ ] Light/dark theme persistence checked
- [ ] RTL / Persian UI spot-check (no broken layout)

## Docker / CI

- [ ] Dockerfile multi-stage + non-root user reviewed
- [ ] Docker Compose smoke job green (health + login + JWT)
- [ ] Unicode bidi scan job green
- [ ] Actions versions (checkout/setup-*) acceptable (Node 24 runtime)

## Sign-off

- [ ] Security reviewer
- [ ] Backend / DBA reviewer
- [ ] Frontend reviewer
- [ ] Release owner

Waivers (if any):

| Item | Reason | Owner |
|------|--------|-------|
|      |        |       |
