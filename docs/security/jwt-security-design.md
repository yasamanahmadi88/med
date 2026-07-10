# JWT Security Design

## Issuer role

MedPortal backend is both **token issuer** and **resource server** (classic JHipster JWT).

## Token format

- Algorithm: **HS512** (explicit at sign time)
- Key: Base64 secret from `jhipster.security.authentication.jwt.base64-secret` (env/config)
- Claims: `sub` (login), `auth` (authorities CSV historically), `PartyId`, `exp`
- Validation parser: `Jwts.parser().verifyWith(secretKey).clockSkewSeconds(30)`
  - Rejects `alg=none`
  - Rejects asymmetric algorithms against HMAC key
  - Enforces signature + expiration

## Authentication flow

1. `POST /api/authenticate` → credentials (+ captcha where configured)
2. Response `id_token` + `Authorization` header
3. Angular stores token in local/session storage
4. `AuthInterceptor` sends `Authorization: Bearer <token>`
5. `JWTFilter` validates token; `TokenProvider.getAuthentication` reloads **active** user authorities from DB (not solely from token claims)

## Refresh / revocation

- Access token lifetime from JHipster properties
- Logout hits `/api/auth/logout` and clears client storage
- Server-side blacklist / session cache exists (`SecurityCache`, `BlacklistedToken`) — retain and monitor

## Hardening applied in this upgrade

- Explicit HS512 signing
- HMAC `verifyWith` parser
- Clock skew 30s
- Null/blank token short-circuit
- Resource method security preserved for `@Secured("resourceName")`

## Follow-ups

- Add `iss` / `aud` if multiple services share tokens
- Rotate signing keys with `kid`
- Prefer refresh-token rotation for long sessions
- Migrate SPA storage to HttpOnly Secure SameSite cookies when UX allows
