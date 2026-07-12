# Rollback Guide

## Do not

- Force-push `main`
- `git reset --hard` on shared branches without team agreement
- Delete Liquibase history

## Rollback options

### A. Reject the PR

Leave `main` unchanged. Delete or archive branch `upgrade/angular21-java25-springboot-4.0.6` after review.

### B. Revert on the upgrade branch

```bash
git checkout upgrade/angular21-java25-springboot-4.0.6
git log --oneline
git revert <commit-sha>   # prefer revert over reset for shared history
```

### C. Return to baseline commit on this branch

Baseline assembly commit message: `chore: capture project baseline before platform upgrade`

```bash
git checkout upgrade/angular21-java25-springboot-4.0.6
git revert --no-commit HEAD~N..HEAD   # choose range after baseline
# or create a new branch from the baseline SHA for a hotfix line
```

### D. Runtime rollback

1. Redeploy previous artifact built from `main` / last known good tag
2. Keep DB migrations forward-only; do not edit old changelogs
3. Rotate JWT secret if a leaked build config was deployed

### E. Database backup before Liquibase authority alignment

Before applying `20260711_001` / `002` / `003` on a non-empty Oracle:

1. Take RMAN or `expdp` of the application schema (include `JHI_*`, `TBL_*`, sequences)
2. Store the dump outside the app host with retention matching change window
3. If upgrade fails mid-flight: restore dump, redeploy previous app version
4. Do **not** hand-edit `DATABASECHANGELOG` or reverse-engineer dropped legacy tables

See also `docs/upgrade/liquibase-oracle-authority-rca.md`.

## Compatibility notes

- Spring Boot 4 / Jakarta binaries are not hot-swappable with Boot 2.7 artifacts
- Frontend Angular 21 build is not compatible with Angular 14 runtime assumptions
- Always roll backend + frontend together
