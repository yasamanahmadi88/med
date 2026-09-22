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
  /**
   * Rows to return for a collection endpoint, keyed by its path (e.g. `/api/products`).
   * Collections default to empty, which is why no list in this suite rendered a row for a long
   * time — and why a crash in the sort-header icon went unseen until one did.
   */
  collections?: Record<string, unknown[]>;
};

type MockState = {
  account: Account | null;
  authenticateAs: Account;
  failLogin: boolean;
  collections: Record<string, unknown[]>;
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
      unexpectedConsole.push(`${page.url()} :: ${text}`);
    });

    page.on('pageerror', error => {
      unexpectedConsole.push(`${page.url()} :: ${error.message}`);
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
  await page.locator(`a.dropdown-item[href$="${path}"]`).click();
  await expect(page).toHaveURL(new RegExp(`${escapeRegExp(path)}(?:[?#].*)?$`));
}

async function installMedPortalApiMocks(page: Page, options: MockOptions): Promise<MockState> {
  const state: MockState = {
    account: accountFor(options.account ?? 'anonymous'),
    authenticateAs: accountFor(options.authenticateAs ?? 'admin') ?? adminAccount(),
    failLogin: options.failLogin ?? false,
    collections: options.collections ?? {},
  };

  await page.route('**/management/**', route => handleManagementRoute(route));
  await page.route('**/v3/api-docs', route =>
    fulfillJson(route, {
      openapi: '3.0.1',
      info: { title: 'MedPortal API', version: 'e2e' },
      paths: {},
    }),
  );
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

  if (path === '/api/bpmn-element-access/current') {
    await fulfillJson(route, bpmnElementAccess());
    return;
  }

  if (path === '/api/admin/users') {
    await fulfillJson(route, [], paginationHeaders());
    return;
  }

  if (isCollectionEndpoint(path)) {
    const rows = state.collections[path] ?? [];
    await fulfillJson(route, rows, paginationHeaders(rows.length));
    return;
  }

  if (request.method() === 'GET') {
    await fulfillJson(route, {});
    return;
  }

  await route.fulfill({ status: 204, body: '' });
}

/**
 * Stands in for GET /api/bpmn-element-access/current.
 *
 * The editor refuses to seed a diagram until this resolves, so an unhandled route left
 * #designer-container unrendered and every editor spec failing. Mirrors the seeded catalog
 * minus the two codes BpmnElementAccessService withholds (CDR_PARSER, CSV_TRANSFORMER):
 * a diagram that already contains them still opens, but neither is offered for placing.
 */
function bpmnElementAccess(): unknown {
  const bpmnNs = 'http://www.omg.org/spec/BPMN/20100524/MODEL';
  const catalog: [string, string, string, string, string | null, number][] = [
    ['BPMN_START_EVENT', 'bpmn:StartEvent', bpmnNs, 'startEvent', 'create.start-event', 10],
    ['BPMN_END_EVENT', 'bpmn:EndEvent', bpmnNs, 'endEvent', 'create.end-event', 20],
    ['BPMN_TASK', 'bpmn:Task', bpmnNs, 'task', null, 30],
    ['BPMN_SERVICE_TASK', 'bpmn:ServiceTask', bpmnNs, 'serviceTask', null, 31],
    ['BPMN_SEND_TASK', 'bpmn:SendTask', bpmnNs, 'sendTask', null, 32],
    ['BPMN_RECEIVE_TASK', 'bpmn:ReceiveTask', bpmnNs, 'receiveTask', null, 33],
    ['BPMN_USER_TASK', 'bpmn:UserTask', bpmnNs, 'userTask', null, 34],
    ['BPMN_MANUAL_TASK', 'bpmn:ManualTask', bpmnNs, 'manualTask', null, 35],
    ['BPMN_SCRIPT_TASK', 'bpmn:ScriptTask', bpmnNs, 'scriptTask', null, 36],
    ['BPMN_BUSINESS_RULE_TASK', 'bpmn:BusinessRuleTask', bpmnNs, 'businessRuleTask', null, 37],
    ['BPMN_EXCLUSIVE_GATEWAY', 'bpmn:ExclusiveGateway', bpmnNs, 'exclusiveGateway', null, 40],
    ['BPMN_PARALLEL_GATEWAY', 'bpmn:ParallelGateway', bpmnNs, 'parallelGateway', null, 41],
    ['BPMN_INCLUSIVE_GATEWAY', 'bpmn:InclusiveGateway', bpmnNs, 'inclusiveGateway', null, 42],
    ['BPMN_COMPLEX_GATEWAY', 'bpmn:ComplexGateway', bpmnNs, 'complexGateway', null, 43],
    ['BPMN_EVENT_BASED_GATEWAY', 'bpmn:EventBasedGateway', bpmnNs, 'eventBasedGateway', null, 44],
    ['BPMN_SUB_PROCESS', 'bpmn:SubProcess', bpmnNs, 'subProcess', null, 50],
    ['BPMN_CALL_ACTIVITY', 'bpmn:CallActivity', bpmnNs, 'callActivity', null, 51],
    ['BPMN_BOUNDARY_EVENT', 'bpmn:BoundaryEvent', bpmnNs, 'boundaryEvent', null, 60],
    ['BPMN_INTERMEDIATE_CATCH_EVENT', 'bpmn:IntermediateCatchEvent', bpmnNs, 'intermediateCatchEvent', null, 61],
    ['BPMN_INTERMEDIATE_THROW_EVENT', 'bpmn:IntermediateThrowEvent', bpmnNs, 'intermediateThrowEvent', null, 62],
    ['MERGER', 'Merger:Merger', 'Merger', 'merger', 'create.merger-module', 100],
    ['FRAGMENTER', 'Fragmenter:Fragmenter', 'Fragmenter', 'fragmenter', 'create.fragmenter-module', 110],
    ['KAFKA_RECEIVER', 'KafkaReceiver:KafkaReceiver', 'KafkaReceiver', 'kafkaReceiver', 'create.KafkaReceiver-module', 120],
    [
      'KAFKA_TRANSMITTER',
      'KafkaTransmitter:KafkaTransmitter',
      'KafkaTransmitter',
      'kafkaTransmitter',
      'create.KafkaTransmitter-module',
      130,
    ],
    ['HTTP_RECEIVER', 'HttpReceiver:HttpReceiver', 'HttpReceiver', 'httpReceiver', 'create.HttpReceiver-module', 140],
    ['HTTP_TRANSMITTER', 'HttpTransmitter:HttpTransmitter', 'HttpTransmitter', 'httpTransmitter', 'create.HttpTransmitter-module', 150],
    ['FILE_RECEIVER', 'FileReceiver:FileReceiver', 'FileReceiver', 'fileReceiver', 'create.fileReceiver-module', 160],
    ['FILE_TRANSMITTER', 'FileTransmitter:FileTransmitter', 'FileTransmitter', 'fileTransmitter', 'create.FileTransmitter-module', 170],
    ['DB_RECEIVER', 'DbReceiver:DbReceiver', 'DbReceiver', 'dbReceiver', 'create.dbReceiver-module', 180],
    ['DB_TRANSMITTER', 'DbTransmitter:DbTransmitter', 'DbTransmitter', 'dbTransmitter', 'create.dbTransmitter-module', 190],
    ['CUSTOM_ICON_TASK', 'customIcon:CustomTask', 'http://medportal.behsa.com/schema/bpmn/custom-icons', 'customTask', null, 220],
  ];

  return {
    ownerCode: 'MEDIATION',
    ownerDisplayName: 'Mediation Portal',
    groups: [
      { id: 1, code: 'CORE_BPMN', name: 'Core BPMN Elements' },
      { id: 2, code: 'FILE_PROCESSING', name: 'File Processing Elements' },
    ],
    elements: catalog.map(([code, bpmnType, namespaceUri, localName, paletteAction, sortOrder], index) => ({
      id: index + 1,
      code,
      bpmnType,
      namespaceUri,
      localName,
      paletteAction,
      displayName: code,
      sortOrder,
    })),
  };
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

function paginationHeaders(total = 0): Record<string, string> {
  return {
    ...jsonHeaders,
    'x-total-count': String(total),
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
  const failureText = request.failure()?.errorText ?? '';
  return (
    request.resourceType() === 'websocket' ||
    request.url().includes('/sockjs-node/') ||
    // A backend call cancelled because the test navigated away is not a failure. `/v3/api-docs`
    // is the swagger fetch behind /admin/docs; it has no trailing path segment, so it never
    // matched the api|management pattern and surfaced as a flake whenever the abort happened to
    // land after the route changed.
    (failureText.includes('ERR_ABORTED') && (/\/(api|management)\//.test(request.url()) || request.url().includes('/v3/api-docs')))
  );
}

function escapeRegExp(value: string): string {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}
