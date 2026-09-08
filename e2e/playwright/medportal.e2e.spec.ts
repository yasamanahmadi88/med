import { readFileSync } from 'node:fs';
import { join } from 'node:path';
import {
  adminMenuItems,
  clickNavbarLink,
  entityMenuItems,
  expect,
  expectTheme,
  lazyRoutePaths,
  signIn,
  test,
} from './support/medportal-fixtures';

test.describe('MedPortal authentication', () => {
  test('login page opens with captcha and themed document root', async ({ page, mockApi }) => {
    await mockApi({ account: 'anonymous' });

    await page.goto('/login');

    await expect(page.getByTestId('username')).toBeVisible();
    await expect(page.getByTestId('password')).toBeVisible();
    await expect(page.locator('#userCaptchaInput')).toBeVisible();
    await expect(page.locator('.captcha-preview img')).toBeVisible();
    await expect(page.locator('html')).toHaveAttribute('data-theme', /^(light|dark)$/);
    await expect(page.getByTestId('navbar')).toHaveCount(0);
  });

  test('failed login keeps the user on login and displays the authentication error', async ({ page, mockApi }) => {
    await mockApi({ account: 'anonymous', failLogin: true });

    await page.goto('/login');
    await signIn(page, 'admin', 'bad-password');

    await expect(page).toHaveURL(/\/login$/);
    await expect(page.getByTestId('loginError')).toBeVisible();
  });

  test('successful login opens the dashboard home route', async ({ page, mockApi }) => {
    await mockApi({ account: 'anonymous', authenticateAs: 'admin' });

    await page.goto('/login');
    await signIn(page);

    await expect(page).toHaveURL(/\/$/);
    await expect(page.locator('#home-logged-message')).toContainText('admin');
    await expect(page.getByTestId('navbar')).toBeVisible();
  });

  test('protected route redirects to login and returns after successful login', async ({ page, mockApi }) => {
    await mockApi({ account: 'anonymous', authenticateAs: 'admin' });

    await page.goto('/admin/metrics');
    await expect(page).toHaveURL(/\/login$/);

    await signIn(page);

    await expect(page).toHaveURL(/\/admin\/metrics$/);
    await expect(page.getByTestId('metricsPageHeading')).toBeVisible();
  });

  test('protected entity route redirects unauthenticated users to login', async ({ page, mockApi }) => {
    await mockApi({ account: 'anonymous' });

    await page.goto('/module');

    await expect(page).toHaveURL(/\/login$/);
    await expect(page.getByTestId('username')).toBeVisible();
  });

  test('admin route rejects authenticated users without ROLE_ADMIN', async ({ page, mockApi }) => {
    await mockApi({ account: 'user' });

    await page.goto('/admin/metrics');

    await expect(page).toHaveURL(/\/accessdenied$/);
    await expect(page.locator('.alert-danger')).toContainText('not authorized');
  });

  test('logout clears the authenticated navbar state and opens login', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/module');
    await page.getByTestId('accountMenu').click();
    await page.getByTestId('logout').click();

    await expect(page).toHaveURL(/\/login$/);
    await expect(page.getByTestId('username')).toBeVisible();
    await expect(page.getByTestId('navbar')).toHaveCount(0);
  });
});

test.describe('MedPortal routes and menus', () => {
  test('navbar entity menu links navigate to declared entity routes', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });
    await page.goto('/module');

    for (const item of entityMenuItems) {
      await test.step(`entity menu: ${item.label} -> ${item.path}`, async () => {
        await clickNavbarLink(page, 'entity', item.path);
      });
    }
  });

  test('admin menu links navigate to declared admin and admin-owned entity routes', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });
    await page.goto('/admin/user-management');

    for (const item of adminMenuItems) {
      await test.step(`admin menu: ${item.label} -> ${item.path}`, async () => {
        await clickNavbarLink(page, 'adminMenu', item.path);
      });
    }
  });

  test('lazy routes load directly with mocked backend APIs', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    for (const path of lazyRoutePaths) {
      await test.step(`load ${path}`, async () => {
        await page.goto(path);
        await expect(page).toHaveURL(new RegExp(`${path === '/' ? '\\/$' : `${path.replaceAll('/', '\\/')}$`}`));
        await expect(page.locator('main')).toBeVisible();
      });
    }
  });

  test('unknown route redirects to the 404 page', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.goto('/route-that-does-not-exist');

    await expect(page).toHaveURL(/\/404$/);
    await expect(page.locator('.alert-danger')).toContainText('does not exist');
  });
});

test.describe('MedPortal themes and responsive navigation', () => {
  test('theme toggles light to dark and persists after refresh on an internal page', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });
    await page.addInitScript(() => {
      if (!localStorage.getItem('medportal-theme')) {
        localStorage.setItem('medportal-theme', 'light');
      }
    });

    await page.goto('/module');
    await expectTheme(page, 'light');

    await page.getByTestId('themeToggle').click();
    await expectTheme(page, 'dark');

    await page.reload();
    await expectTheme(page, 'dark');
  });

  test('stored dark theme applies on login and after entering the application', async ({ page, mockApi }) => {
    await mockApi({ account: 'anonymous', authenticateAs: 'admin' });
    await page.addInitScript(() => localStorage.setItem('medportal-theme', 'dark'));

    await page.goto('/login');
    await expectTheme(page, 'dark');

    await signIn(page);
    await expect(page).toHaveURL(/\/$/);
    await expectTheme(page, 'dark');
  });

  test('responsive navbar collapses on mobile and remains available on desktop', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    await page.setViewportSize({ width: 390, height: 844 });
    await page.goto('/module');
    await expect(page.locator('#navbarResponsive')).not.toBeVisible();

    await page.locator('.navbar-toggler').click();
    await expect(page.locator('#navbarResponsive')).toBeVisible();
    await expect(page.getByTestId('entity')).toBeVisible();

    await page.setViewportSize({ width: 1280, height: 800 });
    await expect(page.getByTestId('navbar')).toBeVisible();
    await expect(page.getByTestId('entity')).toBeVisible();
  });
});

test.describe('MedPortal change detection', () => {
  test('renders a list that arrives after the route has already been drawn', async ({ page, mockApi }) => {
    // Every other spec here asserts what a route paints on arrival, which a frozen view still
    // gets right: the component is created and checked once as part of the navigation. This one
    // asserts the pass *after* that — data fetched in ngOnInit and assigned to a plain field.
    // Without zone change detection the rows never appear even though the response arrived.
    await mockApi({ account: 'admin' });
    await page.route('**/api/admin/users?**', route =>
      route.fulfill({
        status: 200,
        headers: { 'content-type': 'application/json', 'x-total-count': '2', link: '' },
        body: JSON.stringify([
          { id: 1, login: 'admin', email: 'admin@localhost', activated: true, langKey: 'en', authorities: ['ROLE_ADMIN'] },
          { id: 2, login: 'test', email: 'test@localhost', activated: true, langKey: 'en', authorities: ['ROLE_USER'] },
        ]),
      }),
    );

    await page.goto('/admin/user-management');

    await expect(page.locator('jhi-user-mgmt table tbody tr')).toHaveCount(2);
    await expect(page.locator('jhi-user-mgmt table tbody')).toContainText('admin@localhost');
    // isLoading is set true before the request and false in the response handler; a stale view
    // leaves the button disabled forever.
    await expect(page.getByRole('button', { name: 'Refresh list' })).toBeEnabled();
  });
});

test.describe('MedPortal entity lists', () => {
  test('renders a sortable table once the collection has rows', async ({ page, mockApi }) => {
    // Until this spec existed every collection in the suite was mocked empty, so the tables —
    // which are behind `*ngIf="…length > 0"` — never rendered. That hid a crash in the sort
    // header: SortByDirective assigned to FaIconComponent.icon, a ModelSignal in
    // @fortawesome/angular-fontawesome 4, which replaced the signal and made the next render
    // throw "this.icon is not a function". Every entity list in the app was affected.
    // The shared `page` fixture fails the test on any console error, so a regression surfaces
    // here rather than silently.
    await mockApi({
      account: 'admin',
      collections: {
        '/api/products': [
          { id: 1, productName: 'Mediation', productDesc: 'First product' },
          { id: 2, productName: 'Billing', productDesc: 'Second product' },
        ],
      },
    });

    await page.goto('/product');

    await expect(page.locator('table tbody tr')).toHaveCount(2);
    await expect(page.locator('table tbody')).toContainText('Mediation');
    // The sort headers render their icon through the directive that used to throw.
    await expect(page.locator('table thead fa-icon').first()).toBeVisible();
  });

  test('sorting by a column keeps the table rendered', async ({ page, mockApi }) => {
    // updateIconDefinition runs again on every predicate/ascending change, so clicking a header
    // exercises the write path, not just the initial one.
    await mockApi({
      account: 'admin',
      collections: { '/api/products': [{ id: 1, productName: 'Mediation', productDesc: 'First product' }] },
    });

    await page.goto('/product');
    await expect(page.locator('table tbody tr')).toHaveCount(1);

    // The product table's sortable columns are Product Name and Product Desc; there is no ID column.
    await page.locator('table thead th[jhisortby]').first().click();

    await expect(page.locator('table tbody tr')).toHaveCount(1);
    await expect(page.locator('table thead fa-icon').first()).toBeVisible();
  });

  test('a list longer than the viewport still scrolls to its last row', async ({ page, mockApi }) => {
    // The BPMN editor route makes the shell a `height: 100vh; overflow: hidden` column so its
    // canvas has a resolved height (`fullscreen-mode` in `layouts/main/main.component.scss`).
    // That rule is the one change in this application that would break every long page at once
    // if its scope ever slipped off the `fullScreen` branch, and it would break them invisibly:
    // the rows are all still rendered and still "visible" to a presence assertion, they are just
    // clipped where the document stops scrolling. So this measures instead.
    const products = Array.from({ length: 60 }, (_, index) => ({
      id: index + 1,
      productName: `Product ${index + 1}`,
      productDesc: `Description ${index + 1}`,
    }));
    await mockApi({ account: 'admin', collections: { '/api/products': products } });
    await page.setViewportSize({ width: 1280, height: 720 });

    await page.goto('/product');
    await expect(page.locator('table tbody tr')).toHaveCount(60);

    // `content/scss` sets `scroll-behavior: smooth` on the document, so a plain `scrollTo` has
    // not landed by the time the next statement reads `scrollY`. Asking for an instant scroll is
    // what makes the reading real rather than always-zero.
    await page.evaluate(() => window.scrollTo({ top: 100_000, behavior: 'instant' as ScrollBehavior }));

    const scrolled = await page.evaluate(() => ({
      scrollY: window.scrollY,
      documentScrollHeight: document.documentElement.scrollHeight,
      documentClientHeight: document.documentElement.clientHeight,
      lastRowBottom: document.querySelector('table tbody tr:last-child')!.getBoundingClientRect().bottom,
      viewportBottom: window.innerHeight,
      footerTop: document.querySelector('jhi-footer')!.getBoundingClientRect().top,
    }));

    // The page is genuinely taller than the screen, and the document actually moved.
    expect(scrolled.documentScrollHeight).toBeGreaterThan(scrolled.documentClientHeight);
    expect(scrolled.scrollY).toBeGreaterThan(0);
    // Scrolled all the way down: the last row and the footer below it are both reachable.
    expect(scrolled.scrollY).toBe(scrolled.documentScrollHeight - scrolled.documentClientHeight);
    expect(scrolled.lastRowBottom).toBeLessThanOrEqual(scrolled.viewportBottom);
    // The footer is the last thing in the document, so at the bottom of the scroll it sits on
    // the bottom edge of the screen (`jhi-footer` renders an empty template today, which is a
    // separate matter — its box is still the end of the page).
    expect(scrolled.footerTop).toBeLessThanOrEqual(scrolled.viewportBottom + 1);
  });
});

test.describe('MedPortal language', () => {
  test('switching to Persian through the navbar retranslates the page and flips it to RTL', async ({ page, mockApi }) => {
    await mockApi({ account: 'admin' });

    // The expected strings are read from the translation sources rather than hard-coded, so this
    // measures the built `i18n/fa.json` against the 28 files that are supposed to produce it: a
    // bundle that is missing, stale or merged wrongly renders something else and fails here.
    // `global.menu.home` is the probe because the navbar carries it on every route.
    const menuHome = (lang: string): string =>
      JSON.parse(readFileSync(join(process.cwd(), `src/main/webapp/i18n/${lang}/global.json`), 'utf8')).global.menu.home;
    const english = menuHome('en');
    const persian = menuHome('fa');
    // Guards the probe itself. A key that happened to be translated identically in both languages
    // could not tell a working language switch apart from one that silently did nothing.
    expect(persian).not.toEqual(english);

    await page.goto('/');
    const homeLink = page.locator('jhi-navbar span[jhiTranslate="global.menu.home"]');
    await expect(homeLink).toHaveText(english);
    await expect(page.locator('html')).toHaveAttribute('dir', 'ltr');

    await page.locator('#languagesnavBarDropdown').click();
    await page.locator('.dropdown-menu a.dropdown-item', { hasText: 'فارسی' }).click();

    // The rendered text changed, and changed to exactly what `i18n/fa/global.json` ships.
    await expect(homeLink).toHaveText(persian);
    // And the direction followed. `MainComponent.updatePageDirection` runs from the
    // `onLangChange` subscription, which ngx-translate only reaches on a bundle that loaded, so
    // this is the assertion that a 404 on `i18n/fa.json` would break.
    await expect(page.locator('html')).toHaveAttribute('dir', 'rtl');
    expect(await page.evaluate(() => document.documentElement.dir)).toBe('rtl');
  });

  test('Persian half-spaces survive the bundle and reach the DOM as U+200C', async ({ page, mockApi }) => {
    // Persian binds the plural suffix ها to its host with a ZERO WIDTH NON-JOINER, not a space.
    // `i18n-parity.spec.ts` pins that in the source files, but a source file is not a screen: the
    // value is deep-merged by `MergeJsonWebpackPlugin` into `i18n/fa.json`, served over HTTP,
    // decoded, interpolated by ngx-translate and written into the DOM. U+200C is a zero-width,
    // non-printing character, which is exactly the kind of byte a re-encoding step drops silently —
    // and dropping it is invisible in a screenshot, because the glyphs either side do not move.
    // So this asserts on the character, in the rendered text, after the whole pipeline.
    const ZWNJ = '\u200C';
    // Read from the same sources the bundle is built from, as the test above does, so this
    // measures the pipeline rather than restating a hard-coded string.
    const faBundle = (file: string) => JSON.parse(readFileSync(join(process.cwd(), `src/main/webapp/i18n/fa/${file}.json`), 'utf8'));
    const menuModule: string = faBundle('global').global.menu.entities.module; // "ماژول\u200Cها"
    const showTemplate: string = faBundle('global').entity.action.show; // "نمایش {{otherEntity}}"
    const configs: string = faBundle('module').medPortalApp.module.configs; // "تنظیم\u200Cها"

    // Guards the probes themselves: a value that lost its ZWNJ in the source would make the DOM
    // assertions below pass against the wrong expectation.
    expect(menuModule).toContain(ZWNJ);
    expect(configs).toContain(ZWNJ);

    await mockApi({
      account: 'admin',
      collections: { '/api/modules': [{ id: 1, name: 'Mediation', description: 'First module' }] },
    });

    await page.goto('/module');
    await page.locator('#languagesnavBarDropdown').click();
    await page.locator('.dropdown-menu a.dropdown-item', { hasText: 'فارسی' }).click();
    await expect(page.locator('html')).toHaveAttribute('dir', 'rtl');

    // 1. A plain plural, straight from the bundle to the navbar.
    await page.locator('#entity-menu').click();
    const moduleLink = page.locator('ul[aria-labelledby="entity-menu"] span[jhiTranslate="global.menu.entities.module"]');
    await expect(moduleLink).toHaveText(menuModule);
    expect(await moduleLink.textContent()).toContain(ZWNJ);
    // The form this change replaced. Asserting its absence is what fails if a build step were to
    // normalise U+200C back to a space rather than drop it outright.
    expect(await moduleLink.textContent()).not.toContain('ماژول ها');

    // 2. A ZWNJ that arrives through interpolation rather than sitting in the template. The
    // plural belongs to the label — `تنظیم\u200Cها` — and `entity.action.show` contributes only the
    // verb, so what reaches the DOM is a joined form the bundle never contained as one string.
    const showConfigs = page.locator('table tbody [data-cy="filterOtherEntityButton"] span').first();
    await expect(showConfigs).toHaveText(showTemplate.replace('{{otherEntity}}', configs));
    const rendered = (await showConfigs.textContent()) ?? '';
    // Exactly one ZWNJ: the one inside the interpolated label. A second would mean the template
    // had re-grown a plural suffix of its own, which is the doubling this change removed.
    expect([...rendered].filter(character => character === ZWNJ)).toHaveLength(1);
    expect(rendered).not.toMatch(new RegExp(`ها[${ZWNJ} ]?ها`));
    // Substitution actually happened — the braces are gone and the entity name is present.
    expect(rendered).not.toContain('{{');
    expect(rendered).toContain(configs);
  });
});
