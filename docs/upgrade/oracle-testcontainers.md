# Oracle Testcontainers Verification

```bash
./mvnw -ntp -Dskip.installnodenpm -Dskip.npm -Poracle-testcontainers verify
```

Activates Spring profiles `test,testcontainers`. `@EmbeddedSQL` ITs start `gvenzl/oracle-free:slim` when Docker is available.

## Schema ownership

`application-testcontainers.yml` sets:

- `spring.jpa.hibernate.ddl-auto=none`
- `spring.liquibase.enabled=true`

Hibernate must **not** create sequences/tables before Liquibase (otherwise `ORA-00955` on `sequence_generator`).

## Local agent limitation

This agent VM has **no Docker**. Oracle verification runs only in GitHub Actions job `oracle-testcontainers`.

## Override image

```bash
TESTCONTAINERS_ORACLE_IMAGE=gvenzl/oracle-free:slim ./mvnw -Poracle-testcontainers verify
```
