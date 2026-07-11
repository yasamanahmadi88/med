# Build and Test Report (gates continuation)

## CI gates added this iteration

| Job | Purpose |
|-----|---------|
| Unicode bidi and invisible control scan | Blocks Trojan Source / dangerous controls; allows ZWNJ in fa i18n |
| Oracle Liquibase production-schema verification | Liquibase ON, `ddl-auto=none`, fresh + upgrade ITs |
| Docker Compose smoke test | compose config/build/up, health, login, JWT, down -v |
| Docker image build | **Fails** if Dockerfile missing |

## Actions versions

- `actions/checkout@v6` (Node 24)
- `actions/setup-node@v6` (Node 24)
- `actions/setup-java@v5` (Node 24)

## Liquibase authority alignment

See `docs/upgrade/liquibase-oracle-authority-rca.md`.

## Production-ready?

Only after **all** CI jobs on the PR head are green **and** human checklist in
`docs/upgrade/human-review-checklist.md` is signed. Until then: **not** Production-ready.
