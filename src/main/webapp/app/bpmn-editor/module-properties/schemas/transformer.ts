import { ModuleSchema } from '../schema';

/**
 * `Transformer` — which of the fixed, named transformations a message is put through.
 *
 * Field order follows `case 'Transformer:Transformer'` in the Vue panel's
 * `components/Panel/index.tsx`.
 *
 * `firstAction` is the one deviation: its component (`transformerFirstAction.vue`) and
 * getter/setter both exist and are complete, but the component is never imported by the panel, so
 * the field was unreachable in the running Vue editor. It is declared here beside `transformType`,
 * whose option list it shares exactly; drop this one entry to reproduce the Vue form exactly. Note
 * that this `firstAction` is unrelated to `Merger`'s property of the same name, which offers
 * SAVE_ONLY / SAVE_AND_SEND — they collide only in name.
 */
export const transformerSchema: ModuleSchema = {
  type: 'Transformer:Transformer',
  label: 'Transformer',
  fields: [
    { name: 'agreementMode', label: 'Agreement Mode', kind: 'select', options: ['RUNNING', 'FETCH_ONLY', 'DRAFT'] },
    // Both selects label their options with spaces for underscores (MCCI_CHANGECARD shows as
    // "MCCI CHANGECARD"). The underscored values are what the backend reads and what is stored.
    {
      name: 'transformType',
      label: 'Transform Type',
      kind: 'select',
      options: [
        'DTS',
        'MCCI_CHANGECARD',
        'BI_EVENT',
        'IPCC_TICKET',
        'IPCC_AGENT_CALLS',
        'IPCC_IVR_INBOUND',
        'CHARGE_BY_VOUCHER',
        'EVENT_FROM_DB_RECEIVER',
        'PAYMENT_ALL_COUNTRY',
      ],
    },
    {
      name: 'firstAction',
      label: 'First Action',
      kind: 'select',
      options: [
        'DTS',
        'MCCI_CHANGECARD',
        'BI_EVENT',
        'IPCC_TICKET',
        'IPCC_AGENT_CALLS',
        'IPCC_IVR_INBOUND',
        'CHARGE_BY_VOUCHER',
        'EVENT_FROM_DB_RECEIVER',
        'PAYMENT_ALL_COUNTRY',
      ],
    },
    { name: 'commentDesc', label: 'Comment Desc', kind: 'text' },
  ],
};
