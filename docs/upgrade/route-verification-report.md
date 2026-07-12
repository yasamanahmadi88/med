# Route Verification Report

## Method

- Inventory from `app-routing.module.ts`, `entity-routing.module.ts`, `admin-routing.module.ts`, and `navbar.component.html`
- Compared Navbar `routerLink` values to declared routes
- Guard: `UserRouteAccessService` on `/admin/**`

## Results

| Menu / Link        | Expected URL             | Route exists   | Guard          | Notes                            | Status     |
| ------------------ | ------------------------ | -------------- | -------------- | -------------------------------- | ---------- |
| Home               | `/`                      | Yes            | UI switch      |                                  | OK         |
| Module             | `/module`                | Yes            | auth UI        |                                  | OK         |
| Report Logs        | `/reportLogs`            | Yes            | auth UI        | i18n title key still copies flow | OK (known) |
| Config             | `/config`                | Yes            | auth UI        |                                  | OK         |
| Instance           | `/instance`              | Yes            | auth UI        |                                  | OK         |
| Version            | `/version`               | Yes            | auth UI        |                                  | OK         |
| Product            | `/product`               | Yes            | auth UI        |                                  | OK         |
| Flow               | `/flow`                  | Yes            | auth UI        |                                  | OK         |
| User management    | `/admin/user-management` | Yes            | ADMIN          |                                  | OK         |
| Metrics            | `/admin/metrics`         | Yes            | ADMIN          |                                  | OK         |
| Health             | `/admin/health`          | Yes            | ADMIN          |                                  | OK         |
| Configuration      | `/admin/configuration`   | Yes            | ADMIN          |                                  | OK         |
| Logs               | `/admin/logs`            | Yes            | ADMIN          |                                  | OK         |
| API                | `/admin/docs`            | Yes            | ADMIN          |                                  | OK         |
| Custom Audit       | `/custom-audit-event`    | Yes            | auth UI        |                                  | OK         |
| Med Authority      | `/med-authority`         | Yes            | auth UI        |                                  | OK         |
| Resource Authority | `/resource-authority`    | Yes            | auth UI        | under admin menu                 | OK         |
| Login              | `/login`                 | Yes            | public         |                                  | OK         |
| BPMN               | `/bpmn`                  | Yes            | none in router | intentional?                     | Review     |
| Theme toggle       | n/a                      | Navbar control | a11y button    | Added                            | OK         |

## SPA refresh

`ClientForwardController` retained for non-API paths so deep-link refresh works behind the Spring static/SPA fallback.

## Automated coverage

- Playwright E2E added under `e2e/playwright/` and configured by `playwright.config.ts`
- Backend calls are mocked at the browser network layer with `page.route` for `/api/**`, `/management/**`, and `/v3/api-docs`
- Angular dev server is launched by Playwright with BrowserSync disabled: `DISABLE_BROWSER_SYNC=true npm run webapp:dev -- --host 127.0.0.1 --port 9060 --no-hmr`

| Playwright test                                                              | Coverage                                                                     | Result |
| ---------------------------------------------------------------------------- | ---------------------------------------------------------------------------- | ------ |
| `login page opens with captcha and themed document root`                     | `/login`, captcha API mock, login layout, `data-theme`                       | Passed |
| `failed login keeps the user on login and displays the authentication error` | `/api/authenticate` 401, captcha reload                                      | Passed |
| `successful login opens the dashboard home route`                            | JWT login, `/api/account`, home/dashboard                                    | Passed |
| `protected route redirects to login and returns after successful login`      | Stored protected URL redirect for `/admin/metrics`                           | Passed |
| `protected entity route redirects unauthenticated users to login`            | `UserRouteAccessService` on `/module`                                        | Passed |
| `admin route rejects authenticated users without ROLE_ADMIN`                 | Insufficient role redirect to `/accessdenied`                                | Passed |
| `logout clears the authenticated navbar state and opens login`               | Account menu logout flow                                                     | Passed |
| `navbar entity menu links navigate to declared entity routes`                | Entity menu URLs from `navbar.component.html` and `entity-routing.module.ts` | Passed |
| `admin menu links navigate to declared admin and admin-owned entity routes`  | Admin menu URLs from `navbar.component.html` and `admin-routing.module.ts`   | Passed |
| `lazy routes load directly with mocked backend APIs`                         | Direct lazy route loading for entity/admin routes                            | Passed |
| `unknown route redirects to the 404 page`                                    | Wildcard route to `/404`                                                     | Passed |
| `theme toggles light to dark and persists after refresh on an internal page` | Theme toggle, `localStorage`, `data-theme`                                   | Passed |
| `stored dark theme applies on login and after entering the application`      | Theme persistence across login/internal pages                                | Passed |
| `responsive navbar collapses on mobile and remains available on desktop`     | Mobile toggler and desktop navbar visibility                                 | Passed |

Final local run: `npx playwright test` -> 14 passed, 0 failed.
