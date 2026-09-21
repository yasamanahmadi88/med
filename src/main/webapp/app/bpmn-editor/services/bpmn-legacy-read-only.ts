import type { BpmnXmlElementIdentity } from './bpmn-element-access.types';

/**
 * Retired modules that remain readable/editable in persisted diagrams but can no longer be
 * created. Their moddle descriptors, renderers and property schemas must stay registered.
 */
export const LEGACY_READ_ONLY_BPMN_TYPES = new Set(['CdrParser:CdrParser', 'CsvTransformer:CsvTransformer']);

export const LEGACY_READ_ONLY_PALETTE_ACTIONS = new Set(['create.cdrParser-module', 'create.csvTransformerCorner-module']);

const LEGACY_READ_ONLY_XML_KEYS = new Set([xmlKey('CdrParser', 'cdrParser'), xmlKey('CsvTransformer', 'csvTransformer')]);

export function isLegacyReadOnlyBpmnType(type: string | null | undefined): boolean {
  return !!type && LEGACY_READ_ONLY_BPMN_TYPES.has(type);
}

export function isLegacyReadOnlyPaletteAction(action: string): boolean {
  return LEGACY_READ_ONLY_PALETTE_ACTIONS.has(action);
}

export function isLegacyReadOnlyXmlElement(identity: BpmnXmlElementIdentity): boolean {
  return LEGACY_READ_ONLY_XML_KEYS.has(xmlKey(identity.namespaceUri, identity.localName));
}

export function xmlKey(namespaceUri: string, localName: string): string {
  return `${namespaceUri}\u0000${localName}`;
}
