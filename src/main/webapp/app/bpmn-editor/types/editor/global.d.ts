import { Base, Shape, Connection, Label } from 'diagram-js/lib/model';

declare global {
  /**
   * Minimal shape of the message box the editor reports validation warnings through.
   * Nothing installs one today, so every consumer must treat it as optional.
   */
  interface BpmnMessageBox {
    warning(content: string): unknown;
  }

  interface Window {
    bpmnInstances: any;
    __messageBox?: BpmnMessageBox;
  }

  type BpmnElement = Base | Shape | Connection | Label;
}
