import ModulePropertiesProvider from './ModulePropertiesProvider';

/**
 * didi module registering the integration-module properties group. Added to the modeler alongside
 * the stock properties panel modules; see `designer.component.ts`.
 */
export default {
  __init__: ['modulePropertiesProvider'],
  modulePropertiesProvider: ['type', ModulePropertiesProvider],
};

export { ModulePropertiesProvider };
export * from './schema';
