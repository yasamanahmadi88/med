# Oracle Testcontainers Verification

```bash
./mvnw -ntp -Dskip.installnodenpm -Dskip.npm -Poracle-testcontainers verify
```

Activates Spring profiles `test,testcontainers`. `@EmbeddedSQL` ITs start `gvenzl/oracle-free:slim` when Docker is available.

## Schema strategy

`application-testcontainers.yml`:

- `spring.liquibase.enabled=true`
- `spring.liquibase.drop-first=true` — clean schema so `sequence_generator` create cannot hit ORA-00955
- `spring.jpa.hibernate.ddl-auto=update` — after Liquibase, Hibernate creates MEDIATION entity tables/sequences (`USER_SEQ`, `FLOWS_SEQ`, `TBL_*`, …) that are not in Liquibase changelogs

## Local agent limitation

This agent VM has **no Docker**. Oracle verification runs only in GitHub Actions job `oracle-testcontainers`.

## Override image

```bash
TESTCONTAINERS_ORACLE_IMAGE=gvenzl/oracle-free:slim ./mvnw -Poracle-testcontainers verify
```
