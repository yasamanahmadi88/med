import { defineConfig, devices } from '@playwright/test';

const baseURL = process.env.PLAYWRIGHT_BASE_URL ?? 'http://127.0.0.1:9060';

export default defineConfig({
  testDir: './e2e/playwright',
  fullyParallel: true,
  forbidOnly: Boolean(process.env.CI),
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: [['list'], ['html', { open: 'never', outputFolder: 'target/playwright-report' }]],
  outputDir: 'target/playwright-results',
  use: {
    baseURL,
    testIdAttribute: 'data-cy',
    trace: 'on-first-retry',
  },
  webServer: {
    command: 'DISABLE_BROWSER_SYNC=true npm run webapp:dev -- --host 127.0.0.1 --port 9060 --no-hmr',
    url: baseURL,
    reuseExistingServer: !process.env.CI,
    timeout: 180_000,
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
});
