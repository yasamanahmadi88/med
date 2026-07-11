# Route / menu / security fix (runtime)

## Symptom

After login, entity menus disappeared or did not navigate, and list APIs returned empty data.
Admin / permission-gated items looked "broken" similarly to NPG's pattern where only the menu item
without `*jhiHasPermission` remained visible.

## Root cause

1. **JWT `PartyId` claim** — `TokenProvider.validateToken()` required a non-blank `PartyId`.
   Seeded users often have `party_id = NULL`. Login still issued a JWT, but every later request
   failed validation → SecurityContext empty → `/api/account` 401 → Angular `account === null`
   → `*ngSwitchCase="true"` menus hidden → `UserRouteAccessService` sent entity routes back to login
   → remaining calls looked like "empty backend data".

2. **Report-logs menu permission** — navbar had gated `/reportLogs` on a resource permission
   (previously even the wrong resource `config`). Entity menus are now consistent: visible when
   authenticated; page-level `*jhiHasPermission` still guards actions inside list/detail views.

3. **Account resource authorities** — `/api/account` now always hydrates `resourceAuthorities` for
   `*jhiHasPermission` (fallback query when PortalUser list is empty).

## Fixes

- Emit `PartyId` as `""` when null; accept blank but present claim
- Correct navbar permission for report logs
- Account endpoint fallback for resource authorities
- IT: null-`party_id` login + `/api/account`; ROLE_USER can `GET /api/modules`

## Note on NPG "File Report Generation Logs"

That label is from `npg-portal-instance-security`, not MedPortal. MedPortal's equivalent menu is
**Report logs** (`/reportLogs`). If you are running NPG, apply the same JWT/`jhiHasPermission`
diagnosis there; this branch fixes MedPortal.
