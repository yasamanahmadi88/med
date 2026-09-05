import { AGREEMENT_MODE, ModuleSchema } from '../schema';

/**
 * `HttpReceiver` is the reference implementation; the remaining modules follow the same shape.
 * Field order matches the Vue panel so the form reads the same way it always has.
 */
export const httpReceiverSchema: ModuleSchema = {
  type: 'HttpReceiver:HttpReceiver',
  label: 'HttpReceiver',
  fields: [
    { name: 'agreementMode', label: 'Agreement Mode', kind: 'select', options: AGREEMENT_MODE },
    { name: 'validator', label: 'Validator', kind: 'textarea' },
    { name: 'transformer', label: 'Transformer', kind: 'textarea' },
    { name: 'responseTransformer', label: 'Response Transformer', kind: 'textarea' },
    { name: 'transferType', label: 'Transfer Type', kind: 'select', options: ['JSLT', 'XSLT'] },
    { name: 'asyncMessageTypes', label: 'Async Message Types', kind: 'text' },
    { name: 'authUserName', label: 'Auth Username', kind: 'text' },
    { name: 'authPassword', label: 'Auth Password', kind: 'text' },
    { name: 'authTokenVerificationUrl', label: 'Auth Token Verification Url', kind: 'text' },
    {
      name: 'authTokenVerificationResponseValidator',
      label: 'Auth Token Verification Response Validator',
      kind: 'textarea',
    },
    { name: 'commentDesc', label: 'Comment Desc', kind: 'text' },
  ],
};
