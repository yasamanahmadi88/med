# Change Scope Audit

**Branch:** `upgrade/angular21-java25-springboot-4.0.6`  
**Compared to:** `origin/main`  
**Date:** 2026-07-10

## Why ~1219 files appear in the PR

`main` only contained:

- `README.md`
- `src-for-medPortal.rar` (~3.4 MB archive)

The upgrade branch **materialized the full MedPortal source** that previously lived only inside the RAR (plus root build files from `origin/med-upgrade`). Almost every path under `src/` is therefore an **add**, not an unrelated rewrite of an already-tracked tree.

| Category | Approx. tracked count | Origin | Keep in Git? |
|----------|----------------------:|--------|--------------|
| Angular app (`src/main/webapp/app`, i18n, scss, …) | ~580 | RAR extract | Yes (source) |
| BPMN vendor viewer (`content/bpmnjs/dist/**`) | 256 | RAR extract (prebuilt iframe assets) | **Yes** — runtime dependency of `BpmnComponent` iframe (`content/bpmnjs/dist/index.html`); not Angular CLI output |
| Java main | 197 | RAR + Boot 4 migration | Yes |
| Java tests | 94 | RAR + Boot 4 test API fixes | Yes |
| Resources / Liquibase | 31 | RAR | Yes |
| Docker compose/jib helpers | 11 | RAR | Yes |
| Docs (`docs/**`) | 12+ | This upgrade | Yes |
| Webpack / Maven wrapper / root configs | ~40 | med-upgrade + prior upgrade | Yes |
| `src-for-medPortal.rar` | 1 | main | **Remove from tracking** after extract (redundant; bloated) |

## Generated / cache (must not be committed)

| Path | Status |
|------|--------|
| `/target/` | Ignored (`.gitignore`) — not tracked |
| `/node_modules/` | Ignored — not tracked |
| `/dist/`, `/build/`, `.angular/`, `coverage/` | Added to `.gitignore` in this cleanup |
| Angular hashed bundles under `target/classes/static/` | Build output only — not tracked |

## Files removed from tracking in cleanup

1. `src-for-medPortal.rar` — source already present under `src/`; archive kept out of the branch tip to shrink the PR and avoid dual sources of truth.

## BPMN `dist/` decision

These files look like “build output” but are **checked-in vendor static assets** required at runtime by:

```html
<iframe src="../../../content/bpmnjs/dist/index.html"></iframe>
```

Removing them without an alternate packaging step would break `/bpmn`. They are **not** produced by `ng build` of this workspace. Documented as vendor assets; remain tracked.

## Counts

| Metric | Before cleanup | After cleanup (expected) |
|--------|---------------:|-------------------------:|
| Files vs `main` (name-only) | 1219 | ~1218 (− RAR) |
| Tracked paths on branch | 1221 | ~1220 |

## Unrelated churn

- No wholesale deletion of company domain/security code beyond archiving `CustomAccessDecisionManager` to `docs/upgrade/legacy-source/` (API removed in Spring Security 7).
- Spec files touched only for Jest → Vitest (`vi.fn`) compatibility.
- Prior agent branch had dropped custom security/DB files; this branch restored/migrated them.
