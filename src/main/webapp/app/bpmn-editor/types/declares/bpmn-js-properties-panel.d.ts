// bpmn-js-properties-panel ships no type declarations. Only the three didi modules the editor
// registers are declared here, plus the moddle descriptor that backs the Camunda provider.
declare module 'bpmn-js-properties-panel' {
  import { ModuleDeclaration } from 'didi';

  /** Hosts the panel itself; reads its parent element from the `propertiesPanel` config. */
  export const BpmnPropertiesPanelModule: ModuleDeclaration;

  /** Supplies the plain BPMN 2.0 property groups (id, name, documentation, ...). */
  export const BpmnPropertiesProviderModule: ModuleDeclaration;

  /** Adds the Camunda 7 groups; needs the camunda moddle extension registered alongside it. */
  export const CamundaPlatformPropertiesProviderModule: ModuleDeclaration;
}

declare module 'camunda-bpmn-moddle/resources/camunda.json' {
  const descriptor: Record<string, unknown>;
  export default descriptor;
}
