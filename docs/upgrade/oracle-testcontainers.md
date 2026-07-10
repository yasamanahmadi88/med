# Oracle Testcontainers Verification

Oracle-backed integration tests are configured through the opt-in Maven profile:

```bash
./mvnw -ntp -Dskip.installnodenpm -Dskip.npm -Poracle-testcontainers verify
```

The profile activates Spring profiles `test,testcontainers`. Integration tests annotated with `@EmbeddedSQL` start an Oracle Free container through Testcontainers when Docker is available.

## Container image

Default image:

```text
gvenzl/oracle-free:slim
```

Override in CI or locally if needed:

```bash
TESTCONTAINERS_ORACLE_IMAGE=gvenzl/oracle-free:23-slim-faststart ./mvnw -Poracle-testcontainers verify
```

or:

```bash
./mvnw -Dtestcontainers.oracle.image=gvenzl/oracle-free:23-slim-faststart -Poracle-testcontainers verify
```

## Local agent limitation

This Cursor Cloud agent does not have Docker available, so Oracle Testcontainers cannot be executed here. CI must run the Oracle-backed Maven verification on a runner with Docker.

## Current schema caveat

The H2 integration path currently fails during Hibernate validation because the local test schema does not include all `MEDIATION.*` tables referenced by entity mappings. The Oracle Testcontainers profile is intended to make that gap visible on Docker-enabled CI and should not be reported green until CI has actually completed successfully.
