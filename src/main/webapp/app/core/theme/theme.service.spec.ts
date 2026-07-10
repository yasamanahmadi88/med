import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { ThemeService } from './theme.service';

function stubMatchMedia(matches: boolean): void {
  Object.defineProperty(window, 'matchMedia', {
    writable: true,
    configurable: true,
    value: vi.fn().mockImplementation((query: string) => ({
      matches,
      media: query,
      onchange: null,
      addListener: vi.fn(),
      removeListener: vi.fn(),
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
      dispatchEvent: vi.fn(),
    })),
  });
}

describe('ThemeService', () => {
  beforeEach(() => {
    localStorage.removeItem('medportal-theme');
    document.documentElement.removeAttribute('data-theme');
  });

  afterEach(() => {
    localStorage.removeItem('medportal-theme');
    document.documentElement.removeAttribute('data-theme');
    vi.restoreAllMocks();
  });

  it('defaults to light when no preference and system is not dark', () => {
    stubMatchMedia(false);
    const service = new ThemeService();
    expect(service.currentTheme).toBe('light');
    expect(document.documentElement.getAttribute('data-theme')).toBe('light');
  });

  it('uses prefers-color-scheme dark on first visit', () => {
    stubMatchMedia(true);
    const service = new ThemeService();
    expect(service.currentTheme).toBe('dark');
  });

  it('toggles light to dark and persists', () => {
    stubMatchMedia(false);
    const service = new ThemeService();
    service.setTheme('light');
    service.toggleTheme();
    expect(service.currentTheme).toBe('dark');
    expect(localStorage.getItem('medportal-theme')).toBe('dark');
    expect(document.documentElement.getAttribute('data-theme')).toBe('dark');
  });

  it('toggles dark to light and persists', () => {
    stubMatchMedia(false);
    const service = new ThemeService();
    service.setTheme('dark');
    service.toggleTheme();
    expect(service.currentTheme).toBe('light');
    expect(localStorage.getItem('medportal-theme')).toBe('light');
  });

  it('restores stored theme after refresh simulation', () => {
    stubMatchMedia(false);
    localStorage.setItem('medportal-theme', 'dark');
    const service = new ThemeService();
    expect(service.currentTheme).toBe('dark');
  });
});
