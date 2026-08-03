import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export type ThemeMode = 'light' | 'dark';

const STORAGE_KEY = 'medportal-theme';

/**
 * Central theme controller. Persists preference in localStorage and applies
 * `data-theme` on the document element. First visit follows prefers-color-scheme.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly themeSubject: BehaviorSubject<ThemeMode>;

  constructor() {
    const initial = this.resolveInitialTheme();
    this.themeSubject = new BehaviorSubject<ThemeMode>(initial);
    this.applyTheme(initial);
  }

  get theme$(): Observable<ThemeMode> {
    return this.themeSubject.asObservable();
  }

  get currentTheme(): ThemeMode {
    return this.themeSubject.value;
  }

  setTheme(theme: ThemeMode): void {
    this.themeSubject.next(theme);
    this.applyTheme(theme);
    try {
      localStorage.setItem(STORAGE_KEY, theme);
    } catch {
      // Ignore storage failures (private mode / quota).
    }
  }

  toggleTheme(): void {
    this.setTheme(this.currentTheme === 'dark' ? 'light' : 'dark');
  }

  private resolveInitialTheme(): ThemeMode {
    try {
      const stored = localStorage.getItem(STORAGE_KEY);
      if (stored === 'light' || stored === 'dark') {
        return stored;
      }
    } catch {
      // fall through to system preference
    }
    if (typeof window !== 'undefined' && window.matchMedia?.('(prefers-color-scheme: dark)').matches) {
      return 'dark';
    }
    return 'light';
  }

  private applyTheme(theme: ThemeMode): void {
    if (typeof document === 'undefined') {
      return;
    }
    document.documentElement.setAttribute('data-theme', theme);
    document.documentElement.style.colorScheme = theme;
  }
}
