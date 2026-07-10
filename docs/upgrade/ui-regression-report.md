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

## Viewports to verify (manual / Cypress)

| Viewport | Size | Status |
|----------|------|--------|
| Desktop | 1440×900 | Pending runtime browser |
| Laptop | 1280×720 | Pending |
| Tablet | 768×1024 | Pending |
| Mobile | 390×844 | Pending |

## Notes

- Docker/Playwright browser not available in agent environment for screenshot capture
- Theme styles use CSS variables layered on existing Bootstrap classes to minimize visual drift in light mode
