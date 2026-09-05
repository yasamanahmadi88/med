import { EditorSettings } from '../types/editor/settings';

/**
 * Seeds a brand-new diagram, the way the Vue editor's `createNewDiagram` did.
 *
 * bpmn-js ships `modeler.createDiagram()`, which always produces a process called `Process_1`
 * with no name. The Vue editor never used it: it built its own XML so the configured
 * `processId` and `processName` reached the diagram, falling back to a timestamp when the
 * settings left them blank. Those two settings are what the backend keys a flow on, so a
 * diagram created without them is one the user has to rename by hand before it can be saved.
 */

/** A moddle id must be an NCName: no leading digit, and no spaces or punctuation beyond `_-.`. */
const ID_PATTERN = /^[A-Za-z_][\w.-]*$/;

/**
 * Escapes text for an XML attribute. The Vue original interpolated `processName` raw, so a
 * name holding `&` or `"` — both accepted by the settings form — produced a document that
 * failed to import, leaving the canvas blank with only a console warning.
 */
function escapeXml(value: string): string {
  return value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

/**
 * The empty BPMN 2.0 document, carrying the given process id and name.
 *
 * The engine namespace is deliberately absent: the Vue `EmptyXML` took an engine argument and
 * never referenced it, so no diagram it created ever carried one. Adding it here would make new
 * diagrams differ from every existing one, and moddle supplies the prefix on export anyway once
 * a property under that namespace is set.
 */
export function emptyDiagramXml(processId: string, processName: string): string {
  const id = escapeXml(processId);
  return `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
  targetNamespace="http://bpmn.io/schema/bpmn"
  id="Definitions_${id}">
  <bpmn:process id="${id}" name="${escapeXml(processName)}" isExecutable="true"></bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="${id}"></bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`;
}

/**
 * The id and name a new diagram gets, from the settings where they are usable and a timestamp
 * where they are not. A configured id that is not a valid NCName is dropped rather than
 * written, because moddle rejects the document outright and the editor would open empty.
 */
export function newDiagramIdentity(settings?: Pick<EditorSettings, 'processId' | 'processName'>): {
  processId: string;
  processName: string;
} {
  const timestamp = Date.now();
  const configuredId = settings?.processId?.trim();
  return {
    processId: configuredId && ID_PATTERN.test(configuredId) ? configuredId : `Process_${timestamp}`,
    processName: settings?.processName?.trim() || `processName${timestamp}`,
  };
}

interface DiagramImporter {
  importXML(xml: string): Promise<{ warnings?: unknown[] }>;
}

/**
 * Imports `xml` when given one, otherwise an empty diagram carrying the configured identity.
 *
 * Import warnings are logged rather than thrown, matching the Vue behaviour: bpmn-js reports
 * unknown extension attributes as warnings, and a diagram saved by an older editor is still
 * worth opening.
 */
export async function createNewDiagram(modeler: DiagramImporter, settings?: EditorSettings, xml?: string): Promise<void> {
  const { processId, processName } = newDiagramIdentity(settings);
  const { warnings } = await modeler.importXML(xml ?? emptyDiagramXml(processId, processName));
  warnings?.forEach(warning => console.warn(warning));
}
