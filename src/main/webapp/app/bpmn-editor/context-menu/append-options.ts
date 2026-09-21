import * as replaceOptions from 'bpmn-js/lib/features/replace/ReplaceOptions';

export interface AppendOption {
  readonly label: string;
  readonly actionName: string;
  readonly className: string;
  readonly target: {
    readonly type: string;
    readonly isExpanded?: boolean;
    readonly eventDefinitionType?: string;
  };
}

/*
 * Mediation module catalog.
 *
 * This catalog is NOT an authorization whitelist. BpmnElementAccessService filters this
 * catalog every time the right-click menu opens, so the active portal owner sees only
 * the BPMN types assigned through Owner -> Group -> Element.
 */
const MEDIATION_OPTIONS: readonly AppendOption[] = [
  {
    label: 'Merger Module',
    actionName: 'create.merger-module',
    className: 'merger-module',
    target: { type: 'Merger:Merger' },
  },
  {
    label: 'Fragmenter Module',
    actionName: 'create.fragmenter-module',
    className: 'fragmenter-module',
    target: { type: 'Fragmenter:Fragmenter' },
  },
  {
    label: 'Kafka Receiver Module',
    actionName: 'create.KafkaReceiver-module',
    className: 'KafkaReceiver-module',
    target: { type: 'KafkaReceiver:KafkaReceiver' },
  },
  {
    label: 'Kafka Transmitter Module',
    actionName: 'create.KafkaTransmitter-module',
    className: 'KafkaTransmitter-module',
    target: { type: 'KafkaTransmitter:KafkaTransmitter' },
  },
  {
    label: 'HTTP Receiver Module',
    actionName: 'create.HttpReceiver-module',
    className: 'HttpReceiver-module',
    target: { type: 'HttpReceiver:HttpReceiver' },
  },
  {
    label: 'HTTP Transmitter Module',
    actionName: 'create.HttpTransmitter-module',
    className: 'HttpTransmitter-module',
    target: { type: 'HttpTransmitter:HttpTransmitter' },
  },
  {
    label: 'File Receiver Module',
    actionName: 'create.fileReceiver-module',
    className: 'fileReceiver-module',
    target: { type: 'FileReceiver:FileReceiver' },
  },
  {
    label: 'File Transmitter Module',
    actionName: 'create.FileTransmitter-module',
    className: 'fileTransmitter-module',
    target: { type: 'FileTransmitter:FileTransmitter' },
  },
  {
    label: 'DB Receiver Module',
    actionName: 'create.dbReceiver-module',
    className: 'dbReceiver-module',
    target: { type: 'DbReceiver:DbReceiver' },
  },
  {
    label: 'DB Transmitter Module',
    actionName: 'create.dbTransmitter-module',
    className: 'dbTransmitter-module',
    target: { type: 'DbTransmitter:DbTransmitter' },
  },
  {
    label: 'CDR Parser Module',
    actionName: 'create.cdrParser-module',
    className: 'bpmn-icon-cdrParserModule',
    target: { type: 'CdrParser:CdrParser' },
  },
  {
    label: 'CSV Transformer Module',
    actionName: 'create.csvTransformerCorner-module',
    className: 'csvTransformer-module',
    target: { type: 'CsvTransformer:CsvTransformer' },
  },
];

export function appendOptions(): AppendOption[] {
  const { START_EVENT, TASK, GATEWAY, BOUNDARY_EVENT } = replaceOptions;

  const standardOptions = [...START_EVENT, ...TASK, ...GATEWAY, ...BOUNDARY_EVENT].filter(
    (entry): entry is AppendOption => typeof entry.label === 'string',
  );

  return [...standardOptions, ...MEDIATION_OPTIONS];
}
