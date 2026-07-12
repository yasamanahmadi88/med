# Build and Test Report (gates continuation)

## CI head

- Branch: `upgrade/angular21-java25-springboot-4.0.6`
- Commit: `ed7fb6a` (`fix(oracle): Instant mapping, SPA permitAll, Compose smoke gates`)
- Workflow run: https://github.com/yasamanahmadi88/med/actions/runs/29142525084
- Result: **all jobs success**

| Job | Result |
|-----|--------|
| Unicode bidi and invisible control scan | success |
| Angular lint, unit, prod build, Playwright | success |
| Java 25 Maven unit tests and modernizer | success |
| Java 25 H2 integration tests | success |
| Oracle Testcontainers Maven verify | success |
| Oracle Liquibase production-schema verification | success |
| Docker image build | success |
| Docker Compose smoke test | success |

## What the Liquibase gate proved

- Empty Oracle Free + full `master.xml` with Liquibase ON and `ddl-auto=none`
- Application context + repositories + login/JWT (`liquibaseit` / `user`)
- Second Liquibase run idempotent
- Upgrade path from legacy name-PK authority schema

## What Compose smoke proved

- Dockerfile present (missing = fail)
- `docker compose config` / build / up / healthy
- Health endpoint reachable
- Home / index HTTP 200
- Unauthenticated `/api/account` → 401
- Login + JWT `/api/account` → 200
- `docker compose down -v`

## Actions versions

- `actions/checkout@v6` (Node 24)
- `actions/setup-node@v6` (Node 24)
- `actions/setup-java@v5` (Node 24)

## Liquibase authority alignment

See `docs/upgrade/liquibase-oracle-authority-rca.md`.

## Residual fixes in `ed7fb6a`

- SPA static paths + `anyRequest().permitAll()` so `/` is not 401
- Oracle Instant mapping (`preferred_instant_jdbc_type=TIMESTAMP`, `timezoneAsRegion=false`)
- Dockerfile honors `JAVA_OPTS` for Compose JDBC flags

## Production-ready?

**Not** Production-ready until human checklist in
`docs/upgrade/human-review-checklist.md` is signed by reviewers.
Automated gates on this head are green; human sign-off remains open.
