# Security Audit Report — MedPortal Upgrade

**Branch:** `upgrade/angular21-java25-springboot-4.0.6`  
**Date:** 2026-07-10  
**Stack after upgrade:** Spring Boot 4.0.6 / Spring Security 7 / JJWT 0.12.6 / Angular 21

## Findings

| ID | Severity | Location | Risk | Change | Proof | Residual |
|----|----------|----------|------|--------|-------|----------|
| SEC-01 | High | Prior upgrade dropped `CustomAccessDecisionManager` | Resource `@Secured("name")` checks would fall back to role matching and fail-open/deny incorrectly | Restored behavior via `ResourceSecuredAuthorizationManager` + custom `@Secured` advisor | Compile + method-security wiring; unit path covered by authorization manager | Full IT needs DB |
| SEC-02 | High | JWT validation | Weak/alg confusion / incomplete claims | `Jwts.parser().verifyWith(hmacKey)` rejects `alg=none` and non-HMAC; explicit `signWith(key, Jwts.SIG.HS512)`; required issuer/audience; 30s clock skew; blank token and missing critical claims rejected | `TokenProviderSecurityTest` expanded for valid/expired/nbf/wrong signature/wrong alg/alg=none/wrong issuer/audience/empty subject/incomplete claims | Rotate signing keys with `kid` in a follow-up |
| SEC-03 | Medium | Token storage (FE) | XSS → token theft via localStorage/sessionStorage | Documented; kept ngx-webstorage contract to avoid breaking SPA | See JWT design doc | Prefer HttpOnly cookies in a follow-up |
| SEC-04 | Medium | CSRF | Cookie auth CSRF | CSRF disabled with rationale: Bearer JWT in Authorization header (stateless) | `SecurityConfiguration` | Re-enable CSRF if cookie session is introduced |
| SEC-05 | Medium | CORS | Over-permissive origins | Explicit localhost origins only; credentials allowed; no `*` with credentials | `SecurityAuthorizationIT` CORS cases (require full context) | Move origins to env config for prod |
| SEC-06 | Medium | Actuator | Info disclosure | Health/info require ADMIN; sensitive endpoints `denyAll` | Security filter chain rules | Confirm prod profile |
| SEC-07 | Low | `.yo-rc.json` jwtSecretKey / seeded admin | Secret in repo history and known JHipster seed password | No new JWT secrets committed; prod JWT secret comes from `SECURITY_AUTHENTICATION_JWT_BASE64_SECRET`; corrective Liquibase changeset disables seeded admin if its default hash remains | Review + `20260517-03-disable-default-admin-password` | Rotate any historical secrets outside git |
| SEC-08 | Info | Password encoding | Weak encoder choice | `BCryptPasswordEncoder` retained | SecurityConfiguration | Consider `DelegatingPasswordEncoder` later |

## CSRF decision

Authentication uses **Bearer JWT** in the `Authorization` header (not cookie session). CSRF is disabled because browsers do not automatically attach Authorization headers on cross-site requests. If authentication moves to cookies, CSRF protection must be re-enabled.

## Token storage decision

Frontend continues to store JWT in **localStorage/sessionStorage** via ngx-webstorage to preserve the existing SPA contract. XSS remains the primary residual risk; mitigate with CSP, dependency hygiene, and a future HttpOnly cookie migration.

## CORS decision

CORS allows only explicit localhost development origins and the headers/methods needed by the SPA, including `Authorization`. Credentials are enabled, so production configuration must remain origin-specific and must not use `*`.

## Tests added

- `TokenProviderSecurityTest` — valid / missing / malformed / expired / nbf / wrong signature-secret / wrong alg / alg=none / wrong issuer-audience / empty subject / incomplete claims / auth load
- `JWTFilterTest` — Bearer-only extraction, malformed/invalid bearer, missing/empty/non-Bearer Authorization, query-string token ignored
- `SecurityAuthorizationIT` — 401/403/CORS/actuator/header parsing/query-string rejection (needs IntegrationTest DB)
- `ResourceSecuredAuthorizationManagerTest` — role-only and resource+verb authorization decisions
- Frontend `ThemeService` specs (a11y theme toggle companion)
