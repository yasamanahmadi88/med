import { ModuleSchema } from '../schema';

/**
 * `KafkaTransmitter` — publishes the message to a Kafka topic.
 *
 * Ported from the Vue editor's `components/Panel/components/kafkaTransmitterProperties/` (labels
 * and controls) paired with `bo-utils/kafkaTransmitterProperties/` (moddle property names). Field
 * order is the order `components/Panel/index.tsx` pushed the components under
 * `case 'KafkaTransmitter:KafkaTransmitter'` — the sequence the panel actually rendered.
 *
 * That case pushed only seven of the module's nine components. `Agreement Key` and `Headers` are
 * the other two: both have a component *and* a getter/setter in the Vue project, but `index.tsx`
 * never pushed them (`Headers` is imported and then unused; `Agreement Key` is not even imported),
 * so the panel never showed them. They are included to keep all nine declared properties
 * addressable and appended last because they have no established position — the same treatment
 * `fileReceiverSchema` gives its two orphans.
 *
 * The three JSLT-ish fields are the payload: `kafkaKey`, `kafkaHeader` and `kafkaBody`. Note that
 * `kafkaHeader` (a textarea, the record's headers expression) and `headers` (a one-line text input)
 * are two distinct moddle properties despite the near-identical names; do not collapse them.
 */
export const kafkaTransmitterSchema: ModuleSchema = {
  type: 'KafkaTransmitter:KafkaTransmitter',
  label: 'KafkaTransmitter',
  fields: [
    { name: 'agreementMode', label: 'Agreement Mode', kind: 'select', options: ['RUNNING', 'FETCH_ONLY', 'DRAFT'] },
    { name: 'commentDesc', label: 'Comment Desc', kind: 'text' },
    { name: 'topic', label: 'Topic', kind: 'text' },
    { name: 'bootstrapServer', label: 'Bootstrap Server', kind: 'text' },
    { name: 'kafkaHeader', label: 'Kafka Header', kind: 'textarea' },
    { name: 'kafkaBody', label: 'Kafka Body', kind: 'textarea' },
    { name: 'kafkaKey', label: 'Kafka Key', kind: 'textarea' },
    // Never rendered by the Vue panel. `<input type="number" min="0" max="999999999">`.
    { name: 'agreementKey', label: 'Agreement Key', kind: 'number' },
    // Never rendered by the Vue panel. A one-line `<input type="text" maxlength="500">`, distinct
    // from the `kafkaHeader` textarea above.
    { name: 'headers', label: 'Headers', kind: 'text' },
  ],
};
