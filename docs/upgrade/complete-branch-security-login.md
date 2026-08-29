# Complete branch: security & login

Status: **merged into `main`**. This work originally landed on the
`cursor/medportal-complete-b7d5` branch, which has since been merged and deleted; `main` is
now the canonical branch. This document is kept as a record of what the change contained.

## Login path (working end-to-end)

1. `POST /api/authenticate` — captcha + rate limit + Spring auth + JWT + server session cache.
2. Angular `LoginService` stores JWT, then `AccountService.identity(true)` loads `/api/account`.
3. `/api/account` hydrates `resourceAuthorities` (from `PortalUser` or DB fallback).
4. Menus use `*jhiHasPermission`; `ROLE_ADMIN` bypasses resource RBAC (aligned with backend).
5. `POST /api/auth/logout` clears server session + client auth state.

## Fixes included on this branch

| Area | Change |
|------|--------|
| JWT `PartyId` | Empty string when null (prevents post-login API/menu failure) |
| Account RBAC | `resourceAuthorities` always populated for UI |
| Empty forms | Schema via `hibernate.default_schema`; admin RBAC bypass |
| FE logout | Clears `_loggedInUser` / auth on identity failure |
| FE permission | Requires authenticated user; admin bypass |
| Login UX | Refuses navigation if account fetch fails after JWT |
| Register | `/api/register` → `denyAll` |
| Secrets | Prod DB + JWT from env; Docker Compose requires JWT/DB password |
| Remember-me | Longer JWT TTL **and** matching server-side inactivity (7 days) |

## Known operational limits

- `SecurityCache` / captcha buckets are in-memory (single instance). HA needs a shared store.
- Production readiness still requires green CI + `human-review-checklist.md` sign-off.
