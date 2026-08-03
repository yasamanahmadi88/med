import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    setupFiles: ['src/test/javascript/spec/vitest-globals.ts'],
    coverage: {
      reportsDirectory: 'target/test-results',
    },
    globals: true,
    // Angular TestBed is process-global; keep files sequential per worker.
    fileParallelism: false,
    maxWorkers: 1,
    isolate: true,
  },
});
