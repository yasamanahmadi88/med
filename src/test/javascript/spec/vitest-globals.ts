import { afterEach } from 'vitest';
import { TestBed } from '@angular/core/testing';

// Webpack DefinePlugin equivalents for unit tests.
(globalThis as any).SERVER_API_URL = '';
(globalThis as any).I18N_HASH = 'test';
(globalThis as any).__VERSION__ = '0.0.1-SNAPSHOT';
(globalThis as any).__DEBUG_INFO_ENABLED__ = false;

// Prevent TestBed state leaking across Vitest files in the same worker.
afterEach(() => {
  TestBed.resetTestingModule();
});
