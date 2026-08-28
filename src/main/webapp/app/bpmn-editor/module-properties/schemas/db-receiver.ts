import { ModuleSchema } from '../schema';

/**
 * `DbReceiver` — a database-polling entry point. The connection itself lives on the process; all
 * this module declares is what the rows it reads are turned into.
 *
 * Field order follows `case 'DbReceiver:DbReceiver'` in the Vue panel's
 * `components/Panel/index.tsx`. Every component under `dbReceiverProperties/` is composed there, so
 * this is the whole module.
 *
 * Identical field-for-field to `EventaDbReceiver`, which is a separate moddle type with its own
 * (duplicated) getters and setters — the two schemas are deliberately kept apart rather than shared.
 */
export const dbReceiverSchema: ModuleSchema = {
  type: 'DbReceiver:DbReceiver',
  label: 'DbReceiver',
  fields: [
    { name: 'agreementMode', label: 'Agreement Mode', kind: 'select', options: ['RUNNING', 'FETCH_ONLY', 'DRAFT'] },
    { name: 'outputMsgType', label: 'Output Message Type', kind: 'text' },
    { name: 'commentDesc', label: 'Comment Desc', kind: 'text' },
  ],
};
