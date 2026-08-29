import { ModuleSchema } from '../schema';

import { anyProcessSchema } from './any-process';
import { cdrParserSchema } from './cdr-parser';
import { csvTransformerSchema } from './csv-transformer';
import { dbReceiverSchema } from './db-receiver';
import { dbTransmitterSchema } from './db-transmitter';
import { eventaDbReceiverSchema } from './eventa-db-receiver';
import { fileReceiverSchema } from './file-receiver';
import { fileTransmitterSchema } from './file-transmitter';
import { fragmenterSchema } from './fragmenter';
import { httpReceiverSchema } from './http-receiver';
import { httpReceiverEventaSchema } from './http-receiver-eventa';
import { httpTransmitterSchema } from './http-transmitter';
import { kafkaReceiverSchema } from './kafka-receiver';
import { kafkaTransmitterSchema } from './kafka-transmitter';
import { mergerSchema } from './merger';
import { transformerSchema } from './transformer';

/**
 * Every module the properties panel knows about, in the order the palette lists them.
 *
 * `anyProcessSchema` is the odd one: it is keyed on `bpmn:SequenceFlow` rather than a module type,
 * because the Vue panel attached those fields to the connection between two modules. Registering
 * it therefore puts the group on every sequence flow in every diagram, plain BPMN included —
 * which is exactly what the Vue editor did.
 */
export const moduleSchemas: readonly ModuleSchema[] = [
  httpReceiverSchema,
  httpTransmitterSchema,
  httpReceiverEventaSchema,
  kafkaReceiverSchema,
  kafkaTransmitterSchema,
  fileReceiverSchema,
  fileTransmitterSchema,
  dbReceiverSchema,
  dbTransmitterSchema,
  eventaDbReceiverSchema,
  cdrParserSchema,
  csvTransformerSchema,
  transformerSchema,
  fragmenterSchema,
  mergerSchema,
  anyProcessSchema,
];

/** The schema for an element's moddle type, or undefined when it carries no module properties. */
export function schemaForType(type: string | undefined): ModuleSchema | undefined {
  return type ? moduleSchemas.find(schema => schema.type === type) : undefined;
}

export {
  anyProcessSchema,
  cdrParserSchema,
  csvTransformerSchema,
  dbReceiverSchema,
  dbTransmitterSchema,
  eventaDbReceiverSchema,
  fileReceiverSchema,
  fileTransmitterSchema,
  fragmenterSchema,
  httpReceiverSchema,
  httpReceiverEventaSchema,
  httpTransmitterSchema,
  kafkaReceiverSchema,
  kafkaTransmitterSchema,
  mergerSchema,
  transformerSchema,
};
