import { AGREEMENT_MODE, ModuleSchema } from '../schema';

/**
 * `Merger` — how an incoming message is combined with the one already held, and what happens when
 * the held message expires before its partner arrives.
 *
 * Field order follows `case 'Merger:Merger'` in the Vue panel's `components/Panel/index.tsx`; the
 * filenames sort differently and would scramble the save/send/expire progression the form reads in.
 *
 * Two property names do not follow their label: `Force Next Day` writes `mergerForceNextDay` (the
 * only field here carrying the module name in the property) and `Is Incremental` writes
 * `isIncremental234`. Both are transcribed from the Vue getters verbatim — they are what existing
 * diagrams were saved with, so "fixing" either would silently orphan stored values.
 */
export const mergerSchema: ModuleSchema = {
  type: 'Merger:Merger',
  label: 'Merger',
  fields: [
    { name: 'agreementMode', label: 'Agreement Mode', kind: 'select', options: AGREEMENT_MODE },
    {
      name: 'isIncremental234',
      label: 'Is Incremental',
      kind: 'select',
      options: [
        { value: '0', label: 'No' },
        { value: '1', label: 'Yes' },
      ],
    },
    {
      name: 'firstAction',
      label: 'First Action',
      kind: 'select',
      options: [
        { value: 'SAVE_ONLY', label: 'SAVE ONLY' },
        { value: 'SAVE_AND_SEND', label: 'SAVE AND SEND' },
      ],
    },
    {
      name: 'lastSaveAction',
      label: 'Last Save Action',
      kind: 'select',
      options: [
        { value: 'SAVE_MERGED', label: 'SAVE MERGED' },
        { value: 'SAVE_NEW', label: 'SAVE NEW' },
        { value: 'SAVE_OLD', label: 'SAVE OLD' },
        { value: 'NOT_SAVE', label: 'NOT SAVE' },
      ],
    },
    {
      name: 'lastSendAction',
      label: 'Last Send Action',
      kind: 'select',
      options: [
        { value: 'SEND_MERGED', label: 'SEND MERGED' },
        { value: 'SEND_NEW', label: 'SEND NEW' },
        { value: 'SEND_OLD', label: 'SEND OLD' },
        { value: 'NOT_SEND', label: 'NOT SEND' },
      ],
    },
    { name: 'expireAction', label: 'Expire Action', kind: 'select', options: ['SEND', { value: 'NOT_SEND', label: 'NOT SEND' }] },
    {
      name: 'mergerForceNextDay',
      label: 'Force Next Day',
      kind: 'select',
      options: [
        { value: '0', label: 'Is Not' },
        { value: '1', label: 'Is' },
      ],
    },
    { name: 'expirationCount', label: 'Expiration Count', kind: 'number' },
    { name: 'expirationTime', label: 'Expiration Time (ms)', kind: 'number' },
    // A free-text `hh:mm:ss` field. The Vue component validated the format inline and showed its
    // own error; the panel has no validated entry, so it stores whatever is typed.
    { name: 'expireTimeOfDay', label: 'Expire Time Of Day', kind: 'text', validate: 'timeOfDay' },
    { name: 'commentDesc', label: 'Comment Desc', kind: 'text' },
    { name: 'expiredMsgType', label: 'Expired Message Type', kind: 'text' },
    { name: 'mergedMsgType', label: 'Merged Message Type', kind: 'text' },
  ],
};
