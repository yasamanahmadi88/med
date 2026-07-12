# Route Inventory (pre-upgrade)

Source of truth: Angular routing modules + Navbar links. Status reflects baseline before platform upgrade.

## App shell

| URL | Component / Module | Guard | Permission / Role | Lazy | Menu | APIs (typical) | Pre-upgrade status |
|-----|-------------------|-------|-------------------|------|------|----------------|--------------------|
| `/` | `HomeModule` via entity parent + home | optional auth UI | authenticated UI | partial | Home | `/api/account` | Present |
| `/login` | `LoginModule` | none | public | yes | Account | `POST /api/authenticate` | Present |
| `/account/*` | `AccountModule` | module-level | user | yes | Account | `/api/account` | Present |
| `/admin/*` | `AdminRoutingModule` | `UserRouteAccessService` | `ROLE_ADMIN` | yes | Administration | `/management/*`, `/api/admin/*` | Present |
| `/bpmn` | `BpmnComponent` | none declared | open in router | no | (direct) | local bpmn assets | Present |
| `**` / error | `ErrorComponent` (`errorRoute`) | none | public | no | n/a | n/a | Present |

## Entity routes (`EntityRoutingModule`)

| URL | Lazy module | Guard | Menu (Navbar) | Notes | Status |
|-----|-------------|-------|---------------|-------|--------|
| `/med-authority` | `MedAuthorityModule` | entity routes / auth | Administration → Med Authority | | Present |
| `/resource` | `ResourceModule` | auth | (entities) | | Present |
| `/resource-authority` | `ResourceAuthorityModule` | auth | (entities) | | Present |
| `/module` | `ModuleModule` | auth | Entities → Module | | Present |
| `/config` | `ConfigModule` | auth | Entities → Config | | Present |
| `/instance` | `InstanceModule` | auth | Entities → Instance | | Present |
| `/version` | `VersionModule` | auth | Entities → Version | | Present |
| `/product` | `ProductModule` | auth | Entities → Product | | Present |
| `/flow` | `FlowModule` | auth | Entities → Flow | | Present |
| `/reportLogs` | `LogsModule` | auth | Entities → Report Logs | pageTitle reuses flow i18n key | Present |
| `/custom-audit-event` | `CustomAuditEventModule` | auth | Administration → Custom Audit | | Present |

## Admin child routes

| URL | Module | Menu | Status |
|-----|--------|------|--------|
| `/admin/user-management` | `UserManagementModule` | User management | Present |
| `/admin/metrics` | `MetricsModule` | Metrics | Present |
| `/admin/health` | `HealthModule` | Health | Present |
| `/admin/configuration` | `ConfigurationModule` | Configuration | Present |
| `/admin/logs` | `LogsModule` | Logs | Present |
| `/admin/docs` | `DocsModule` | API | Present |

## Navbar mapping notes

- Entity links use relative `routerLink` values (`module`, `config`, …) under authenticated navbar.
- Admin links use `admin/...` paths.
- Access control for REST uses `@Secured` resource names + `CustomAccessDecisionManager` (verb from HTTP mapping).

## Known risks

- Direct refresh of deep links depends on Spring `ClientForwardController` / SPA fallback.
- `/bpmn` has no `UserRouteAccessService` in `app-routing.module.ts`.
- `/reportLogs` i18n title key appears incorrect (copy of flow title).
