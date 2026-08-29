import { ModuleSchema } from '../schema';

/**
 * `EventaDbReceiver` — the Eventa-flavoured database entry point.
 *
 * Field order follows `case 'EventaDbReceiver:EventaDbReceiver'` in the Vue panel's
 * `components/Panel/index.tsx`. Every component under `eventaDbReceiverProperties/` is composed
 * there, so this is the whole module.
 *
 * The fields match `DbReceiver` exactly — the Vue project duplicated the components and the
 * getters/setters wholesale rather than sharing them (`eventaDbReceiverOutputMsgTypeUtil.ts` even
 * kept the `getDbReceiverOutputMsgTypeValue` name). The moddle types are distinct, so the schemas
 * stay distinct too.
 */
export const eventaDbReceiverSchema: ModuleSchema = {
  type: 'EventaDbReceiver:EventaDbReceiver',
  label: 'EventaDbReceiver',
  fields: [
    { name: 'agreementMode', label: 'Agreement Mode', kind: 'select', options: ['RUNNING', 'FETCH_ONLY', 'DRAFT'] },
    { name: 'outputMsgType', label: 'Output Message Type', kind: 'text' },
    { name: 'commentDesc', label: 'Comment Desc', kind: 'text' },
  ],
};
