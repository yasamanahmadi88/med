# UI Regression Report

## Scope

Preserve MedPortal layout: dark navbar, Bootstrap entity tables/forms, ng-bootstrap dropdowns, toastr, Font Awesome icons, FA/EN i18n.

## Intentional UI change

- Light/dark theme via `data-theme` + CSS variables
- Theme toggle button in Navbar (sun/moon icons, aria-label, keyboard focusable)
- FOUC prevention script in `index.html`

## Unchanged by design

- Navbar structure and menu hierarchy
- Entity list/detail/update templates
- Login page structure
- Admin screens
- BPMN viewer assets

## Automated coverage added

Playwright coverage is configured under `e2e/playwright/` and runs in CI through `.github/workflows/upgrade-verify.yml`.

- Authentication route, failed login, protected-route redirect, logout
- Entity/admin navigation links and lazy route loading with mocked backend APIs
- Theme persistence and responsive navbar behavior

## Viewports to verify (manual / screenshots)

| Viewport | Size | Status |
|----------|------|--------|
| Desktop | 1440×900 | Pending screenshot baseline |
| Laptop | 1280×720 | Pending screenshot baseline |
| Tablet | 768×1024 | Pending screenshot baseline |
| Mobile | 390×844 | Covered by Playwright responsive navbar check; screenshot baseline pending |

## Notes

- Docker/Playwright browser not available in agent environment for screenshot capture
- Theme styles use CSS variables layered on existing Bootstrap classes to minimize visual drift in light mode
- No old-version screenshots were available in this workspace, so visual regression is limited to route/theme behavior and manual review until a baseline is captured.
