import ModulePropertiesProvider from './ModulePropertiesProvider';
import { RoleModulesService } from '../services/role-modules.service';

/**
 * didi module registering the integration-module properties group. Added to the modeler alongside
 * the stock properties panel modules; see `designer.component.ts`.
 */
export default {
  __init__: ['modulePropertiesProvider'],
  modulePropertiesProvider: ['type', ModulePropertiesProvider],
  roleModulesService: [
    'factory',
    function (config: any) {
      // Get RoleModulesService from config, which is passed from the designer component
      return config.roleModulesService || new RoleModulesService(null as any);
    },
  ],
};

export { ModulePropertiesProvider };
export * from './schema';
export * from './schemas';
export * from './validators';
export * from './validation';
