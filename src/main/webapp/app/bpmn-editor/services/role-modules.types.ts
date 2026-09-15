export interface ModuleInfo {
  type: string;
  label: string;
  enabled: boolean;
}

export interface RoleModulesResponse {
  role: string;
  modules: ModuleInfo[];
}
