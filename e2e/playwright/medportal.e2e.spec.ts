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
});
