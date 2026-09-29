import { defineConfig } from '@playwright/test';

const baseURL = 'http://127.0.0.1:9060';

// Local-run copy of playwright.config.ts: same server and same viewport, but pointed at the
// Chromium this container already has instead of one Playwright would download.
export default defineConfig({
  testDir: '/home/user/med/e2e/playwright',
  fullyParallel: false,
  workers: 1,
  retries: 0,
  reporter: [['list']],
  outputDir: '/home/user/med/target/playwright-results',
  use: {
    baseURL,
    testIdAttribute: 'data-testid,data-cy',
    viewport: { width: 1280, height: 720 },
    launchOptions: { executablePath: '/opt/pw-browsers/chromium-1194/chrome-linux/chrome' },
  },
  webServer: {
    command: 'DISABLE_BROWSER_SYNC=true npm run webapp:dev -- --host 127.0.0.1 --port 9060 --no-hmr',
    cwd: '/home/user/med',
    url: baseURL,
    reuseExistingServer: true,
    timeout: 300_000,
  },
  projects: [{ name: 'chromium' }],
});
