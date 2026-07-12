# Oracle Testcontainers Verification

## Entity IT profile (Hibernate schema)

```bash
./mvnw -ntp -Dskip.installnodenpm -Dskip.npm -Dskip.unit.tests=true -Poracle-testcontainers verify
```

Profiles: `test,testcontainers`. Liquibase off; Hibernate `ddl-auto=update` + `test-data.sql`. Shared Oracle Free container across DirtiesContext.

## Liquibase production-schema profile

```bash
./mvnw -ntp -Dskip.installnodenpm -Dskip.npm -Dskip.unit.tests=true -Poracle-liquibase-testcontainers verify
```

Profiles: `test,testcontainers,oracleliquibase`.

- Liquibase **enabled** (full `master.xml`)
- Hibernate `ddl-auto=none`
- Failsafe includes only `OracleLiquibase*IT`

See `docs/upgrade/liquibase-oracle-authority-rca.md`.

## Local agent limitation

This agent VM has **no Docker**. Oracle + Compose verification runs in GitHub Actions.

## Override image

```bash
TESTCONTAINERS_ORACLE_IMAGE=gvenzl/oracle-free:slim ./mvnw -Poracle-liquibase-testcontainers verify
```
