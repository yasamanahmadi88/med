# CORS and CSRF Behavior

## CORS

`SecurityConfiguration` enables CORS with an explicit local-development allowlist:

- `http://localhost:9000`
- `http://localhost:4200`
- `http://localhost:8100`
- `http://localhost:9060`

Allowed methods are `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, and `OPTIONS`. Allowed request headers include `Authorization`, `Cache-Control`, `Content-Type`, and `X-Requested-With`. Credentials are enabled, so production deployments must use explicit origins from environment-specific configuration and must not combine credentials with wildcard origins.

## CSRF

CSRF protection is disabled because MedPortal uses stateless Bearer JWT authentication in the `Authorization` header. Browsers do not attach this header automatically on cross-site form/image/script requests, so classic cookie-session CSRF does not apply to the current authentication contract.

If MedPortal moves authentication to cookies, CSRF protection must be re-enabled before that change ships.

## JWT secret source

Production JWT signing material must come from `SECURITY_AUTHENTICATION_JWT_BASE64_SECRET` through `jhipster.security.authentication.jwt.base64-secret`. Do not commit production JWT secrets or fallback production defaults.

The seeded JHipster `admin` account is disabled by the `20260517-03-disable-default-admin-password` corrective changeset when it still has the known default password hash.
