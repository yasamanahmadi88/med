import { ModuleSchema } from '../schema';

/**
 * `HttpReceiverEventa` — the Eventa-flavoured HTTP entry point: a much smaller form than
 * `HttpReceiver`, with no auth block and a transformer type instead of a transfer type.
 *
 * Field order follows `case 'HttpReceiverEventa:HttpReceiverEventa'` in the Vue panel's
 * `components/Panel/index.tsx`. Note that `Comment Desc` is *not* last there — the panel puts
 * `Transformer Type` after it — which is why the order is transcribed rather than assumed.
 *
 * `agreementKey` is the one deviation: its component (`httpReceiverEventaAgreementKey.vue`) and
 * getter/setter both exist and are complete, but the component is never imported by the panel, so
 * the field was unreachable in the running Vue editor. It is declared here next to the agreement
 * mode it belongs with, so the module is configurable in full; drop this one entry to reproduce
 * the Vue form exactly.
 */
export const httpReceiverEventaSchema: ModuleSchema = {
  type: 'HttpReceiverEventa:HttpReceiverEventa',
  label: 'HttpReceiverEventa',
  fields: [
    { name: 'agreementMode', label: 'Agreement Mode', kind: 'select', options: ['RUNNING', 'FETCH_ONLY', 'DRAFT'] },
    { name: 'agreementKey', label: 'Agreement Key', kind: 'number' },
    { name: 'transformer', label: 'Transformer', kind: 'textarea' },
    { name: 'validator', label: 'Validator', kind: 'textarea' },
    { name: 'commentDesc', label: 'Comment Desc', kind: 'text' },
    { name: 'transformerType', label: 'Transformer Type', kind: 'select', options: ['JSLT', 'XSLT'] },
  ],
};
