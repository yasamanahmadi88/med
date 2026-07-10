import { expect, Page, Request, Route, test as base } from '@playwright/test';

type Role = 'anonymous' | 'user' | 'admin';

type ResourceAuthority = {
  resource?: { name?: string };
  verb?: string;
};

type Account = {
  activated: boolean;
  authorities: string[];
  email: string;
  firstName: string | null;
  langKey: string;
  lastName: string | null;
  login: string;
  imageUrl: string | null;
  resourceAuthorities: ResourceAuthority[];
};

type MockOptions = {
  account?: Role;
  authenticateAs?: Role;
  failLogin?: boolean;
};

type MockState = {
  account: Account | null;
  authenticateAs: Account;
  failLogin: boolean;
};

const jsonHeaders = { 'content-type': 'application/json' };

export const entityMenuItems = [
  { label: 'Module', path: '/module' },
  { label: 'logs', path: '/reportLogs' },
  { label: 'Config', path: '/config' },
  { label: 'Instance', path: '/instance' },
  { label: 'Version', path: '/version' },
  { label: 'Product', path: '/product' },
  { label: 'Flow', path: '/flow' },
] as const;

export const adminMenuItems = [
  { label: 'User management', path: '/admin/user-management' },
  { label: 'Metrics', path: '/admin/metrics' },
  { label: 'Health', path: '/admin/health' },
  { label: 'Configuration', path: '/admin/configuration' },
  { label: 'Logs', path: '/admin/logs' },
  { label: 'API', path: '/admin/docs' },
  { label: 'Audit Event', path: '/custom-audit-event' },
  { label: 'Authority', path: '/med-authority' },
  { label: 'Resource', path: '/resource' },
  { label: 'Resource Authority', path: '/resource-authority' },
] as const;

export const lazyRoutePaths = [
  '/',
  '/module',
  '/config',
  '/instance',
  '/version',
  '/product',
  '/flow',
  '/reportLogs',
  '/custom-audit-event',
  '/med-authority',
  '/resource',
  '/resource-authority',
  '/admin/user-management',
  '/admin/metrics',
  '/admin/health',
  '/admin/configuration',
  '/admin/logs',
  '/admin/docs',
] as const;

export const test = base.extend<{ mockApi: (options?: MockOptions) => Promise<MockState> }>({
  page: async ({ page }, use) => {
    const unexpectedConsole: string[] = [];
    const failedRequests: string[] = [];

    page.on('console', message => {
      if (message.type() !== 'error') {
        return;
      }
      const text = message.text();
      if (text.includes('User has not any of required authorities')) {
        return;
      }
      if (text.includes('Failed to load resource: the server responded with a status of 401')) {
        return;
      }
      unexpectedConsole.push(text);
    });

    page.on('pageerror', error => {
      unexpectedConsole.push(error.message);
    });

    page.on('requestfailed', request => {
      if (isAllowedRequestFailure(request)) {
        return;
      }
      failedRequests.push(`${request.method()} ${request.url()} ${request.failure()?.errorText ?? ''}`.trim());
    });

    await use(page);

    expect(unexpectedConsole, 'unexpected browser console/page errors').toEqual([]);
    expect(failedRequests, 'unexpected failed browser requests').toEqual([]);
  },

  mockApi: async ({ page }, use) => {
    await use(async (options: MockOptions = {}) => installMedPortalApiMocks(page, options));
  },
});

export { expect } from '@playwright/test';

export async function signIn(page: Page, username = 'admin', password = 'admin'): Promise<void> {
  await page.getByTestId('username').fill(username);
  await page.getByTestId('password').fill(password);
  await page.locator('#userCaptchaInput').fill('ABC123');
  await page.getByTestId('submit').click();
}

export async function expectTheme(page: Page, theme: 'light' | 'dark'): Promise<void> {
  await expect(page.locator('html')).toHaveAttribute('data-theme', theme);
}

export async function clickNavbarLink(page: Page, menuTestId: string, path: string): Promise<void> {
  await page.getByTestId(menuTestId).click();
  await page.locator(`a.dropdown-item[href="${path}"]`).click();
  await expect(page).toHaveURL(new RegExp(`${escapeRegExp(path)}(?:[?#].*)?$`));
}

async function installMedPortalApiMocks(page: Page, options: MockOptions): Promise<MockState> {
  const state: MockState = {
    account: accountFor(options.account ?? 'anonymous'),
    authenticateAs: accountFor(options.authenticateAs ?? 'admin') ?? adminAccount(),
    failLogin: options.failLogin ?? false,
  };

  await page.route('**/management/**', route => handleManagementRoute(route));
  await page.route('**/api/**', route => handleApiRoute(route, state));

  return state;
}

async function handleApiRoute(route: Route, state: MockState): Promise<void> {
  const request = route.request();
  const url = new URL(request.url());
  const path = url.pathname;

  if (path === '/api/account') {
    if (!state.account) {
      await delay();
      await route.fulfill({ status: 401, headers: jsonHeaders, body: '{}' });
      return;
    }
    await delay();
    await fulfillJson(route, state.account);
    return;
  }

  if (path === '/api/authenticate') {
    if (state.failLogin) {
      await route.fulfill({ status: 401, headers: jsonHeaders, body: '{"detail":"Bad credentials"}' });
      return;
    }
    state.account = state.authenticateAs;
    await fulfillJson(route, { id_token: 'e2e-jwt-token' });
    return;
  }

  if (path === '/api/auth/logout') {
    state.account = null;
    await route.fulfill({ status: 204, body: '' });
    return;
  }

  if (path === '/api/captcha-endpoint') {
    await delay();
    await fulfillJson(route, { captchaId: 'captcha-e2e', captchaImageUrl: '/api/captcha-image/captcha-e2e.svg' });
    return;
  }

  if (path.startsWith('/api/captcha-image/')) {
    await route.fulfill({
      status: 200,
      headers: { 'content-type': 'image/svg+xml' },
      body: '<svg xmlns="http://www.w3.org/2000/svg" width="120" height="40"><text x="8" y="25">ABC123</text></svg>',
    });
    return;
  }

  if (path === '/api/authorities') {
    await fulfillJson(route, ['ROLE_ADMIN', 'ROLE_USER']);
    return;
  }

  if (path === '/api/admin/users') {
    await fulfillJson(route, [], paginationHeaders());
    return;
  }

  if (isCollectionEndpoint(path)) {
    await fulfillJson(route, [], paginationHeaders());
    return;
  }

  if (request.method() === 'GET') {
    await fulfillJson(route, {});
    return;
  }

  await route.fulfill({ status: 204, body: '' });
}

async function handleManagementRoute(route: Route): Promise<void> {
  const path = new URL(route.request().url()).pathname;

  if (path === '/management/info') {
    await fulfillJson(route, {
      activeProfiles: ['dev', 'api-docs'],
      'display-ribbon-on-profiles': 'dev',
    });
    return;
  }

  if (path === '/management/health') {
    await fulfillJson(route, { status: 'UP', components: { db: { status: 'UP' } } });
    return;
  }

  if (path === '/management/jhimetrics') {
    await fulfillJson(route, {
      jvm: {},
      processMetrics: {
        'system.cpu.usage': 0.1,
        'system.cpu.count': 2,
        'process.cpu.usage': 0.05,
        'process.start.time': Date.now() - 60_000,
        'process.uptime': 60_000,
      },
    });
    return;
  }

  if (path === '/management/threaddump') {
    await fulfillJson(route, { threads: [] });
    return;
  }

  if (path === '/management/configprops') {
    await fulfillJson(route, { contexts: { medPortal: { beans: {} } } });
    return;
  }

  if (path === '/management/env') {
    await fulfillJson(route, { propertySources: [] });
    return;
  }

  if (path === '/management/loggers') {
    await fulfillJson(route, { levels: ['TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR', 'OFF'], loggers: { ROOT: { effectiveLevel: 'INFO' } } });
    return;
  }

  await fulfillJson(route, {});
}

function accountFor(role: Role): Account | null {
  if (role === 'anonymous') {
    return null;
  }

  const baseAccount: Account = {
    activated: true,
    authorities: ['ROLE_USER'],
    email: `${role}@localhost`,
    firstName: role,
    langKey: 'en',
    lastName: 'E2E',
    login: role,
    imageUrl: null,
    resourceAuthorities: [
      { resource: { name: 'config' }, verb: 'view' },
      { resource: { name: 'config' }, verb: 'edit' },
    ],
  };

  if (role === 'admin') {
    return { ...baseAccount, authorities: ['ROLE_ADMIN', 'ROLE_USER'], login: 'admin', email: 'admin@localhost' };
  }

  return baseAccount;
}

function adminAccount(): Account {
  return accountFor('admin')!;
}

function isCollectionEndpoint(path: string): boolean {
  return [
    '/api/modules',
    '/api/configs',
    '/api/instances',
    '/api/versions',
    '/api/products',
    '/api/flows',
    '/api/logs',
    '/api/logs/summary',
    '/api/logs/search',
    '/api/custom-audit-events',
    '/api/med-authorities',
    '/api/resources',
    '/api/resource-authorities',
  ].some(endpoint => path === endpoint || path.startsWith(`${endpoint}/`));
}

function paginationHeaders(): Record<string, string> {
  return {
    ...jsonHeaders,
    'x-total-count': '0',
    link: '',
  };
}

async function fulfillJson(route: Route, json: unknown, headers: Record<string, string> = jsonHeaders): Promise<void> {
  await route.fulfill({
    status: 200,
    headers,
    body: JSON.stringify(json),
  });
}

async function delay(ms = 25): Promise<void> {
  await new Promise(resolve => setTimeout(resolve, ms));
}

function isAllowedRequestFailure(request: Request): boolean {
  return request.resourceType() === 'websocket' || request.url().includes('/sockjs-node/');
}

function escapeRegExp(value: string): string {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}
