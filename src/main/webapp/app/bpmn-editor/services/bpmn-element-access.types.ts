export interface BpmnElementGroupAccess {
  id: number;
  code: string;
  name: string;
}

export interface BpmnElementAccess {
  id: number;
  code: string;
  bpmnType: string;
  namespaceUri: string;
  localName: string;
  paletteAction?: string | null;
  displayName: string;
  sortOrder: number;
}

export interface BpmnProductElementAccess {
  productId: number;
  productName: string;
  groups: BpmnElementGroupAccess[];
  elements: BpmnElementAccess[];
}

/** Plain configuration handed to bpmn-js/didi.  It deliberately contains no Angular service. */
export interface BpmnElementAccessConfig {
  readonly allowedTypes: readonly string[];
  readonly allowedPaletteActions: readonly string[];
  readonly allowedXmlElements: readonly BpmnXmlElementIdentity[];
}

export interface BpmnXmlElementIdentity {
  readonly namespaceUri: string;
  readonly localName: string;
}

export const EMPTY_BPMN_ELEMENT_ACCESS_CONFIG: BpmnElementAccessConfig = {
  allowedTypes: [],
  allowedPaletteActions: [],
  allowedXmlElements: [],
};

export function toBpmnElementAccessConfig(access: BpmnProductElementAccess | null | undefined): BpmnElementAccessConfig {
  if (!access) {
    return EMPTY_BPMN_ELEMENT_ACCESS_CONFIG;
  }

  return {
    allowedTypes: access.elements.map(element => element.bpmnType),
    allowedPaletteActions: access.elements.flatMap(element => (element.paletteAction ? [element.paletteAction] : [])),
    allowedXmlElements: access.elements.map(element => ({ namespaceUri: element.namespaceUri, localName: element.localName })),
  };
}

export function isBpmnTypeAllowed(config: BpmnElementAccessConfig | null | undefined, type: string | null | undefined): boolean {
  return !!type && !!config?.allowedTypes.includes(type);
}
