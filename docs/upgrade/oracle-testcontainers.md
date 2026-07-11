# Oracle Testcontainers Verification

```bash
./mvnw -ntp -Dskip.installnodenpm -Dskip.npm -Poracle-testcontainers verify
```

Activates Spring profiles `test,testcontainers`. `@EmbeddedSQL` ITs start `gvenzl/oracle-free:slim` when Docker is available.

## Schema strategy (IT only)

Oracle ITs intentionally **disable Liquibase** and use Hibernate `create-drop` + `test-data.sql`, matching the H2 path.

Reason: Liquibase’s legacy `jhi_authority` (name as PK) conflicts with `MedAuthorityEntity` (numeric `id`) when Hibernate `ddl-auto=update` tries to alter the Liquibase table (`ORA-01758` / `ORA-02267`).

This still validates:

- Oracle Free Testcontainers startup
- Oracle dialect / JDBC
- Entity mappings and Failsafe ITs on Oracle

It does **not** yet prove full production Liquibase changelogs apply cleanly on Oracle. That requires a dedicated Liquibase↔entity schema alignment (out of scope for the stack upgrade gate).

## Local agent limitation

This agent VM has **no Docker**. Oracle verification runs only in GitHub Actions job `oracle-testcontainers`.

## Override image

```bash
TESTCONTAINERS_ORACLE_IMAGE=gvenzl/oracle-free:slim ./mvnw -Poracle-testcontainers verify
```
