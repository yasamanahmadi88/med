import { createAction } from './utils';

/**
 * One integration-module palette entry, in the shape both palette providers used to declare
 * inline.
 */
export interface IntegrationModuleEntry {
  /** Palette entry key, unchanged from when each provider declared its own. */
  readonly id: string;
  /** The moddle type handed to `elementFactory.createShape`. */
  readonly type: string;
  readonly className: string;
  readonly title: string;
}

/**
 * Every integration module the editor has a palette entry for.
 *
 * Declared here rather than inline in each provider because the two lists were identical and
 * the allowlist below has to hold for both: a module the palette must not offer cannot be
 * dropped from the rewrite palette and left in the enhancement one.
 */
const INTEGRATION_MODULES: readonly IntegrationModuleEntry[] = [
  { id: 'create.merger-module', type: 'Merger:Merger', className: 'merger-module', title: 'Merger Module' },
  { id: 'create.fragmenter-module', type: 'Fragmenter:Fragmenter', className: 'fragmenter-module', title: 'Fragmenter Module' },
  {
    id: 'create.KafkaReceiver-module',
    type: 'KafkaReceiver:KafkaReceiver',
    className: 'KafkaReceiver-module',
    title: 'Kafka Receiver Module',
  },
  {
    id: 'create.KafkaTransmitter-module',
    type: 'KafkaTransmitter:KafkaTransmitter',
    className: 'KafkaTransmitter-module',
    title: 'Kafka Transmitter Module',
  },
  { id: 'create.HttpReceiver-module', type: 'HttpReceiver:HttpReceiver', className: 'HttpReceiver-module', title: 'Http Receiver Module' },
  {
    id: 'create.HttpTransmitter-module',
    type: 'HttpTransmitter:HttpTransmitter',
    className: 'HttpTransmitter-module',
    title: 'Http Transmitter Module',
  },
  { id: 'create.fileReceiver-module', type: 'FileReceiver:FileReceiver', className: 'fileReceiver-module', title: 'File Receiver Module' },
  {
    id: 'create.FileTransmitter-module',
    type: 'FileTransmitter:FileTransmitter',
    className: 'fileTransmitter-module',
    title: 'File Transmitter Module',
  },
  { id: 'create.dbReceiver-module', type: 'DbReceiver:DbReceiver', className: 'dbReceiver-module', title: 'DB Receiver Module' },
  {
    id: 'create.dbTransmitter-module',
    type: 'DbTransmitter:DbTransmitter',
    className: 'dbTransmitter-module',
    title: 'DB Transmitter Module',
  },
  { id: 'create.cdrParser-module', type: 'CdrParser:CdrParser', className: 'bpmn-icon-cdrParserModule', title: 'CDR Parser Module' },
  {
    id: 'create.csvTransformerCorner-module',
    type: 'CsvTransformer:CsvTransformer',
    className: 'csvTransformer-module',
    title: 'CSV Transformer Module',
  },
];

/**
 * The integration modules a user may put on the canvas.
 *
 * Everything left out — Kafka, HTTP, DB, CDR and CSV — stays fully supported everywhere else:
 * its moddle extension (`additional-modules/index.ts`), its renderer and its property schema
 * (`module-properties/schemas`) are all still registered, so a flow that already carries one
 * still imports, draws, selects and edits exactly as before. Only the palette entry, which is
 * the way to add a *new* one, is gone.
 *
 * This is the whole rule; add a type here to offer it again.
 */
export const CREATABLE_MODULE_TYPES: readonly string[] = [
  'Merger:Merger',
  'Fragmenter:Fragmenter',
  'FileReceiver:FileReceiver',
  'FileTransmitter:FileTransmitter',
];

/** The subset of {@link INTEGRATION_MODULES} the palette offers, in declaration order. */
export function creatableIntegrationModules(): readonly IntegrationModuleEntry[] {
  return INTEGRATION_MODULES.filter(module => CREATABLE_MODULE_TYPES.includes(module.type));
}

/** Whether the palette may offer `type` as a creation tool. */
export function isCreatableModuleType(type: string): boolean {
  return CREATABLE_MODULE_TYPES.includes(type);
}

/**
 * The creatable modules as palette entries, keyed the way both providers already keyed them so
 * the CSS in `styles/palette.scss` and the icons keep matching.
 *
 * All of them sit in the `activity` group, as they did when each provider declared them.
 */
export function integrationModulePaletteEntries(elementFactory: any, create: any): Record<string, any> {
  return Object.fromEntries(
    creatableIntegrationModules().map(module => [
      module.id,
      createAction(elementFactory, create, module.type, 'activity', module.className, module.title),
    ]),
  );
}
