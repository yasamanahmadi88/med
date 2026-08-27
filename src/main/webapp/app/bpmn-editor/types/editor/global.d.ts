import { Base, Shape, Connection, Label } from 'diagram-js/lib/model';

declare global {
  /**
   * Minimal shape of the message box the editor reports validation warnings through.
   * The original Vue editor bound naive-ui's message API here; the Angular editor installs
   * nothing, so consumers must treat it as optional.
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
