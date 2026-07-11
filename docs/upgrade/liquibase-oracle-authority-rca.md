# Liquibase ↔ Oracle Authority RCA

## Root cause

Two JPA types mapped to `jhi_authority` while Liquibase still created the **legacy JHipster** shape:

| Layer | Shape |
|-------|--------|
| Liquibase `00000000000000_initial_schema` | PK = `name`; `jhi_user_authority.authority_name` |
| `Authority` (Spring Security roles) | PK = numeric `id` + `name` |
| `MedAuthorityEntity` (resource RBAC) | PK = numeric `id` + `name` + `display_name` + `parent_id` |
| `User.authorities` join | `authority_id` → `jhi_authority.id` |

Hibernate `ddl-auto=update` against Liquibase tables then failed on Oracle (`ORA-01758` / PK alter). IT profile previously disabled Liquibase to work around this.

## Are Authority and MedAuthority independent concepts?

**Product design:** they share identity by **name**.

- `ROLE_USER` / `ROLE_ADMIN` are Spring Security authorities.
- The same names are MedAuthority rows used by `ResourceAuthorityQueryService.findByNameIn(...)`.
- Therefore they are **not** two unrelated domains accidentally colliding; they are one physical authority catalog with two JPA projections (`Authority` thin, `MedAuthorityEntity` full).

Splitting into `jhi_authority` (name PK) + `jhi_med_authority` would require duplicating ROLE_* rows and rewriting joins/queries. That was rejected in favor of **aligning Liquibase to the numeric-id model the application already uses**.

## Fix (forward-only)

1. Keep historical `00000000000000_initial_schema.xml` unchanged.
2. `20260711_001_align_authority_numeric_pk.xml` migrates name-PK → id-PK and `authority_name` → `authority_id`.
3. `20260711_002_domain_schema.xml` creates resources, audit, MEDIATION `TBL_*`, sequences.
4. `20260711_003_test_accounts.xml` (context `test`) seeds `liquibaseit` for verification only.
5. Profile `oracle-liquibase-testcontainers`: Liquibase ON, `ddl-auto=none`.

## Upgrade / rollback notes

- **Upgrade:** deploy app with new changelogs; Liquibase migrates legacy authority tables in place and preserves user↔role links.
- **Rollback:** restore DB from pre-migration backup; do not reverse-edit published changesets.
- **Backup plan:** take RMAN/export backup before production Liquibase upgrade; keep previous app version artifact for emergency redeploy with restored DB.
