import { InjectionToken } from '@angular/core';

/**
 * What Save and Cancel mean is decided by whoever routed to the editor — the Flow form persists
 * the diagram against a flow, another caller might only keep it in memory. The editor components
 * ask for this instead of importing a feature service, so the editor stays reusable.
 */
export interface BpmnEditorHost {
  /** Persist the diagram the toolbar just exported and leave the editor. */
  save(xml: string): void;

  /** Leave the editor without persisting. */
  cancel(): void;
}

export const BPMN_EDITOR_HOST = new InjectionToken<BpmnEditorHost>('bpmn-editor-host');
