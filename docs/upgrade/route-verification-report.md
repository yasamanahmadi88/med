# Route Verification Report

## Method

- Inventory from `app-routing.module.ts`, `entity-routing.module.ts`, `admin-routing.module.ts`, and `navbar.component.html`
- Compared Navbar `routerLink` values to declared routes
- Guard: `UserRouteAccessService` on `/admin/**`

## Results

| Menu / Link | Expected URL | Route exists | Guard | Notes | Status |
|-------------|--------------|--------------|-------|-------|--------|
| Home | `/` | Yes | UI switch | | OK |
| Module | `/module` | Yes | auth UI | | OK |
| Report Logs | `/reportLogs` | Yes | auth UI | i18n title key still copies flow | OK (known) |
| Config | `/config` | Yes | auth UI | | OK |
| Instance | `/instance` | Yes | auth UI | | OK |
| Version | `/version` | Yes | auth UI | | OK |
| Product | `/product` | Yes | auth UI | | OK |
| Flow | `/flow` | Yes | auth UI | | OK |
| User management | `/admin/user-management` | Yes | ADMIN | | OK |
| Metrics | `/admin/metrics` | Yes | ADMIN | | OK |
| Health | `/admin/health` | Yes | ADMIN | | OK |
| Configuration | `/admin/configuration` | Yes | ADMIN | | OK |
| Logs | `/admin/logs` | Yes | ADMIN | | OK |
| API | `/admin/docs` | Yes | ADMIN | | OK |
| Custom Audit | `/custom-audit-event` | Yes | auth UI | | OK |
| Med Authority | `/med-authority` | Yes | auth UI | | OK |
| Resource Authority | `/resource-authority` | Yes | auth UI | under admin menu | OK |
| Login | `/login` | Yes | public | | OK |
| BPMN | `/bpmn` | Yes | none in router | intentional? | Review |
| Theme toggle | n/a | Navbar control | a11y button | Added | OK |

## SPA refresh

`ClientForwardController` retained for non-API paths so deep-link refresh works behind the Spring static/SPA fallback.

## Automated coverage

- Theme unit tests added
- Full Cypress menu click suite requires running backend + seeded DB (blocked in this agent without Oracle/Docker)
