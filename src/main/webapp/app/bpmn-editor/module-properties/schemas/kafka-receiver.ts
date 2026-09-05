import { ModuleSchema } from '../schema';

/**
 * `KafkaReceiver` — the topic and consumer settings live on the process, so the module itself
 * carries only the agreement mode and a note.
 *
 * Field order follows `case 'KafkaReceiver:KafkaReceiver'` in the Vue panel's
 * `components/Panel/index.tsx`. Both components under `kafkaReceiverProperties/` are composed
 * there, so this is the whole module.
 */
export const kafkaReceiverSchema: ModuleSchema = {
  type: 'KafkaReceiver:KafkaReceiver',
  label: 'KafkaReceiver',
  fields: [
    { name: 'agreementMode', label: 'Agreement Mode', kind: 'select', options: ['RUNNING', 'FETCH_ONLY', 'DRAFT'] },
    { name: 'commentDesc', label: 'Comment Desc', kind: 'text' },
  ],
};
