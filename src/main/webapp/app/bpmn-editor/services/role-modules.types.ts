/**
 * Role-based module visibility configuration
 * Modules are fetched from backend, nothing is hardcoded
 */

export interface ModuleInfo {
  type: string;
  label: string;
  enabled: boolean;
}

export interface RoleModulesResponse {
  role: string;
  modules: ModuleInfo[];
}
