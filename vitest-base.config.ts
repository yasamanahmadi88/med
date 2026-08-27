import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    setupFiles: ['src/test/javascript/spec/vitest-globals.ts'],
    coverage: {
      reportsDirectory: 'target/test-results',
    },
    globals: true,
    server: {
      deps: {
        // bpmn-js-properties-panel bundles preact and imports it by directory ("../.."), which
        // Node's ESM resolver rejects. Inlining routes these through Vite, which resolves them
        // the same way the webpack build does.
        inline: [/@bpmn-io\/properties-panel/, /bpmn-js-properties-panel/],
      },
    },
    // Angular TestBed is process-global; keep files sequential per worker.
    fileParallelism: false,
    maxWorkers: 1,
    isolate: true,
  },
});
