# Legacy source archived during Spring Boot 4 migration

| File | Reason |
|------|--------|
| `CustomAccessDecisionManager.java` | Uses removed Spring Security `AccessDecisionManager` / `ConfigAttribute` APIs. Behavior preserved by `ResourceSecuredAuthorizationManager` + `MethodSecurityConfiguration`. |

Do not delete these files without review; they document pre-migration authorization behavior.
