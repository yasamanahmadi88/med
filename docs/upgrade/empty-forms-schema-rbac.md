# Empty entity forms despite DB data

## Symptoms

- Tables such as `TBL_MODULES` / `TBL_CONFIGS` have rows in Oracle
- UI entity lists/forms stay empty after login

## Causes addressed

1. **Schema mismatch** — entities were hard-coded to `MEDIATION.*` while Liquibase created
   unqualified `TBL_*` in the JDBC user's schema (e.g. `medportal`). Queries hit the wrong place.
   - Entities no longer hard-code `schema = "MEDIATION"`.
   - Dev/prod set `hibernate.default_schema: ${MEDPORTAL_DB_SCHEMA:MEDIATION}` so the app still
     reads the historical `MEDIATION` owner by default.
   - If the JDBC user **owns** the tables, start with `MEDPORTAL_DB_SCHEMA=` (empty).

2. **RBAC `@Secured("module")` etc.** — without `jhi_resource_authority` rows matching the user's
   roles, list APIs returned 403 and the Angular list left the grid blank.
   - `ROLE_ADMIN` now bypasses resource-verb checks (operators can open forms after upgrade).
   - Non-admin users still need resource permissions; ensure Liquibase `20260711_002` seeds ran
     or grant rows in `jhi_resource` / `jhi_resource_authority`.

## Operator checklist

1. Confirm which schema holds the rows: `SELECT COUNT(*) FROM MEDIATION.TBL_MODULES` vs
   `SELECT COUNT(*) FROM TBL_MODULES`.
2. Set `MEDPORTAL_DB_SCHEMA` accordingly (`MEDIATION` or empty).
3. Login as an activated `ROLE_ADMIN` (default admin may be disabled by `20260517_*`).
4. In browser Network tab, `/api/modules` (etc.) should be **200** with a JSON array.
5. If 403 for a non-admin user, grant VIEW on that resource in Resource Authority UI/DB.
