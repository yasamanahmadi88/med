import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { shareReplay, catchError, map } from 'rxjs/operators';
import { ModuleInfo, RoleModulesResponse } from './role-modules.types';

/**
 * Fetches role-based module visibility from backend.
 * Nothing is hardcoded - all configuration comes from the backend.
 */
@Injectable({ providedIn: 'root' })
export class RoleModulesService {
  private readonly apiUrl = '/api/user/role-modules';
  private modules$: Observable<ModuleInfo[]> | null = null;

  constructor(private http: HttpClient) {}

  /**
   * Get available modules for current user's role.
   * Cached after first fetch.
   */
  getAvailableModules(): Observable<ModuleInfo[]> {
    if (!this.modules$) {
      this.modules$ = this.http.get<RoleModulesResponse>(this.apiUrl).pipe(
        map(response => response.modules.filter(m => m.enabled)),
        catchError(error => {
          console.error('Failed to fetch role modules:', error);
          // Fallback: no modules visible if API fails
          return of([]);
        }),
        shareReplay(1), // Cache the result
      );
    }
    return this.modules$;
  }

  /**
   * Get available module types as a Set for quick lookups
   */
  getAvailableModuleTypes(): Observable<Set<string>> {
    return this.getAvailableModules().pipe(map(modules => new Set(modules.map(m => m.type))));
  }

  /**
   * Check if a specific module type is available
   */
  isModuleAvailable(moduleType: string): Observable<boolean> {
    return this.getAvailableModuleTypes().pipe(map(types => types.has(moduleType)));
  }
}
