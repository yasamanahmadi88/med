import PaletteProvider from 'bpmn-js/lib/features/palette/PaletteProvider';
import ElementFactory from 'bpmn-js/lib/features/modeling/ElementFactory';

import { createAction } from '../utils';
import { BpmnElementAccessConfig, isBpmnTypeAllowed } from '../../../services/bpmn-element-access.types';

/**
 * Palette used by the Mediation BPMN editor.
 *
 * This is intentionally a whitelist rather than an enhancement of the stock
 * bpmn-js palette. It mirrors the visible Vue palette so users do not see new
 * modeling tools merely because the implementation moved to Angular.
 *
 * BPMN element types that are not listed here are NOT removed from the engine.
 * Existing diagrams can still import/render them and other BPMN UI features
 * may still work with them; they are simply not offered as top-level palette
 * creation tools.
 */
class RewritePaletteProvider extends PaletteProvider {
  private readonly _create: any;
  private readonly _elementFactory: ElementFactory;
  private readonly _spaceTool: any;
  private readonly _lassoTool: any;
  private readonly _handTool: any;
  private readonly _globalConnect: any;

  constructor(
    palette: any,
    create: any,
    elementFactory: ElementFactory,
    spaceTool: any,
    lassoTool: any,
    handTool: any,
    globalConnect: any,
    private readonly elementAccess?: BpmnElementAccessConfig,
  ) {
    // Keep the same provider-registration behaviour already used by this
    // rewrite provider. getPaletteEntries() below owns the visible whitelist.
    super(palette, create, elementFactory, spaceTool, lassoTool, handTool, globalConnect, 2000);

    this._create = create;
    this._elementFactory = elementFactory;
    this._spaceTool = spaceTool;
    this._lassoTool = lassoTool;
    this._handTool = handTool;
    this._globalConnect = globalConnect;
  }

  getPaletteEntries(): Record<string, any> {
    const create = this._create;
    const elementFactory = this._elementFactory;
    const spaceTool = this._spaceTool;
    const lassoTool = this._lassoTool;
    const handTool = this._handTool;
    const globalConnect = this._globalConnect;

    const entries: Record<string, any> = {
      // ------------------------------------------------------------
      // Tools - same four tools and same order as Vue
      // ------------------------------------------------------------

      'hand-tool': {
        group: 'tools',
        className: 'bpmn-icon-hand-tool',
        title: 'Activate the hand tool',
        action: {
          click(event: any) {
            handTool.activateHand(event);
          },
        },
      },

      'lasso-tool': {
        group: 'tools',
        className: 'bpmn-icon-lasso-tool',
        title: 'Activate the lasso tool',
        action: {
          click(event: any) {
            lassoTool.activateSelection(event);
          },
        },
      },

      'space-tool': {
        group: 'tools',
        className: 'bpmn-icon-space-tool',
        title: 'Activate the create/remove space tool',
        action: {
          click(event: any) {
            spaceTool.activateSelection(event);
          },
        },
      },

      'global-connect-tool': {
        group: 'tools',
        className: 'bpmn-icon-connection-multi',
        title: 'Activate the global connect tool',
        action: {
          click(event: any) {
            globalConnect.toggle(event);
          },
        },
      },

      'tool-separator': {
        group: 'tools',
        separator: true,
      },

      // ------------------------------------------------------------
      // BPMN events visible in Vue
      // ------------------------------------------------------------

      'create.start-event': createAction(
        elementFactory,
        create,
        'bpmn:StartEvent',
        'events',
        'bpmn-icon-start-event-none',
        'Create Start Event',
      ),

      'create.end-event': createAction(elementFactory, create, 'bpmn:EndEvent', 'events', 'bpmn-icon-end-event-none', 'Create End Event'),

      // ------------------------------------------------------------
      // Mediation / Eventa modules visible in Vue
      // ------------------------------------------------------------

      'create.merger-module': createAction(elementFactory, create, 'Merger:Merger', 'activity', 'merger-module', 'Merger Module'),

      'create.fragmenter-module': createAction(
        elementFactory,
        create,
        'Fragmenter:Fragmenter',
        'activity',
        'fragmenter-module',
        'Fragmenter Module',
      ),

      'create.KafkaReceiver-module': createAction(
        elementFactory,
        create,
        'KafkaReceiver:KafkaReceiver',
        'activity',
        'KafkaReceiver-module',
        'Kafka Receiver Module',
      ),

      'create.KafkaTransmitter-module': createAction(
        elementFactory,
        create,
        'KafkaTransmitter:KafkaTransmitter',
        'activity',
        'KafkaTransmitter-module',
        'Kafka Transmitter Module',
      ),

      'create.HttpReceiver-module': createAction(
        elementFactory,
        create,
        'HttpReceiver:HttpReceiver',
        'activity',
        'HttpReceiver-module',
        'Http Receiver Module',
      ),

      'create.HttpTransmitter-module': createAction(
        elementFactory,
        create,
        'HttpTransmitter:HttpTransmitter',
        'activity',
        'HttpTransmitter-module',
        'Http Transmitter Module',
      ),

      'create.fileReceiver-module': createAction(
        elementFactory,
        create,
        'FileReceiver:FileReceiver',
        'activity',
        'fileReceiver-module',
        'File Receiver Module',
      ),

      'create.FileTransmitter-module': createAction(
        elementFactory,
        create,
        'FileTransmitter:FileTransmitter',
        'activity',
        'fileTransmitter-module',
        'File Transmitter Module',
      ),

      'create.dbReceiver-module': createAction(
        elementFactory,
        create,
        'DbReceiver:DbReceiver',
        'activity',
        'dbReceiver-module',
        'DB Receiver Module',
      ),

      'create.dbTransmitter-module': createAction(
        elementFactory,
        create,
        'DbTransmitter:DbTransmitter',
        'activity',
        'dbTransmitter-module',
        'DB Transmitter Module',
      ),

      'create.cdrParser-module': createAction(
        elementFactory,
        create,
        'CdrParser:CdrParser',
        'activity',
        'bpmn-icon-cdrParserModule',
        'CDR Parser Module',
      ),

      'create.csvTransformerCorner-module': createAction(
        elementFactory,
        create,
        'CsvTransformer:CsvTransformer',
        'activity',
        'csvTransformer-module',
        'CSV Transformer Module',
      ),
    };

    const elementTypesByEntry: Record<string, string> = {
      'create.start-event': 'bpmn:StartEvent',
      'create.end-event': 'bpmn:EndEvent',
      'create.merger-module': 'Merger:Merger',
      'create.fragmenter-module': 'Fragmenter:Fragmenter',
      'create.KafkaReceiver-module': 'KafkaReceiver:KafkaReceiver',
      'create.KafkaTransmitter-module': 'KafkaTransmitter:KafkaTransmitter',
      'create.HttpReceiver-module': 'HttpReceiver:HttpReceiver',
      'create.HttpTransmitter-module': 'HttpTransmitter:HttpTransmitter',
      'create.fileReceiver-module': 'FileReceiver:FileReceiver',
      'create.FileTransmitter-module': 'FileTransmitter:FileTransmitter',
      'create.dbReceiver-module': 'DbReceiver:DbReceiver',
      'create.dbTransmitter-module': 'DbTransmitter:DbTransmitter',
      'create.cdrParser-module': 'CdrParser:CdrParser',
      'create.csvTransformerCorner-module': 'CsvTransformer:CsvTransformer',
    };

    // Tools are not BPMN model elements and remain available.  Every model element is fail-closed
    // unless its type arrived from Owner -> Group -> Element access loaded from the server.
    for (const [entryId, type] of Object.entries(elementTypesByEntry)) {
      if (!isBpmnTypeAllowed(this.elementAccess, type)) {
        Reflect.deleteProperty(entries, entryId);
      }
    }

    return entries;
  }
}

RewritePaletteProvider.$inject = [
  'palette',
  'create',
  'elementFactory',
  'spaceTool',
  'lassoTool',
  'handTool',
  'globalConnect',
  'config.elementAccess',
];

export default RewritePaletteProvider;
