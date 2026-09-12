import { AGREEMENT_MODE, ModuleSchema } from '../schema';

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
    { name: 'agreementMode', label: 'Agreement Mode', kind: 'select', options: AGREEMENT_MODE },
    // Both selects label their options with spaces for underscores (MCCI_CHANGECARD shows as
    // "MCCI CHANGECARD"); the underscored value is still what is stored and what the backend
    // reads. `firstAction` is not rendered by the Vue panel, but its component offers the same
    // nine choices, so it carries the same labels.
    {
      name: 'transformType',
      label: 'Transform Type',
      kind: 'select',
      options: [
        'DTS',
        { value: 'MCCI_CHANGECARD', label: 'MCCI CHANGECARD' },
        { value: 'BI_EVENT', label: 'BI EVENT' },
        { value: 'IPCC_TICKET', label: 'IPCC TICKET' },
        { value: 'IPCC_AGENT_CALLS', label: 'IPCC AGENT CALLS' },
        { value: 'IPCC_IVR_INBOUND', label: 'IPCC IVR INBOUND' },
        { value: 'CHARGE_BY_VOUCHER', label: 'CHARGE BY VOUCHER' },
        { value: 'EVENT_FROM_DB_RECEIVER', label: 'EVENT FROM DB RECEIVER' },
        { value: 'PAYMENT_ALL_COUNTRY', label: 'PAYMENT ALL COUNTRY' },
      ],
    },
    {
      name: 'firstAction',
      label: 'First Action',
      kind: 'select',
      options: [
        'DTS',
        { value: 'MCCI_CHANGECARD', label: 'MCCI CHANGECARD' },
        { value: 'BI_EVENT', label: 'BI EVENT' },
        { value: 'IPCC_TICKET', label: 'IPCC TICKET' },
        { value: 'IPCC_AGENT_CALLS', label: 'IPCC AGENT CALLS' },
        { value: 'IPCC_IVR_INBOUND', label: 'IPCC IVR INBOUND' },
        { value: 'CHARGE_BY_VOUCHER', label: 'CHARGE BY VOUCHER' },
        { value: 'EVENT_FROM_DB_RECEIVER', label: 'EVENT FROM DB RECEIVER' },
        { value: 'PAYMENT_ALL_COUNTRY', label: 'PAYMENT ALL COUNTRY' },
      ],
    },
    { name: 'commentDesc', label: 'Comment Desc', kind: 'text' },
  ],
};
