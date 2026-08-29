import { ModuleSchema } from '../schema';

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
    { name: 'agreementMode', label: 'Agreement Mode', kind: 'select', options: ['RUNNING', 'FETCH_ONLY', 'DRAFT'] },
    { name: 'isIncremental234', label: 'Is Incremental', kind: 'select', options: ['0', '1'] },
    { name: 'firstAction', label: 'First Action', kind: 'select', options: ['SAVE_ONLY', 'SAVE_AND_SEND'] },
    {
      name: 'lastSaveAction',
      label: 'Last Save Action',
      kind: 'select',
      options: ['SAVE_MERGED', 'SAVE_NEW', 'SAVE_OLD', 'NOT_SAVE'],
    },
    {
      name: 'lastSendAction',
      label: 'Last Send Action',
      kind: 'select',
      options: ['SEND_MERGED', 'SEND_NEW', 'SEND_OLD', 'NOT_SEND'],
    },
    { name: 'expireAction', label: 'Expire Action', kind: 'select', options: ['SEND', 'NOT_SEND'] },
    { name: 'mergerForceNextDay', label: 'Force Next Day', kind: 'select', options: ['0', '1'] },
    { name: 'expirationCount', label: 'Expiration Count', kind: 'number' },
    { name: 'expirationTime', label: 'Expiration Time (ms)', kind: 'number' },
    // A free-text `hh:mm:ss` field. The Vue component validated the format inline and showed its
    // own error; the panel has no validated entry, so it stores whatever is typed.
    { name: 'expireTimeOfDay', label: 'Expire Time Of Day', kind: 'text' },
    { name: 'commentDesc', label: 'Comment Desc', kind: 'text' },
    { name: 'expiredMsgType', label: 'Expired Message Type', kind: 'text' },
    { name: 'mergedMsgType', label: 'Merged Message Type', kind: 'text' },
  ],
};
