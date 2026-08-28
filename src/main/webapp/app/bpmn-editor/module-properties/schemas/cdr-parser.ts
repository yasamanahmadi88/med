import { ModuleSchema } from '../schema';

/**
 * `CdrParser` — which call-detail-record dialect an incoming file is parsed as.
 *
 * Field order follows `case 'CdrParser:CdrParser'` in the Vue panel's `components/Panel/index.tsx`.
 *
 * `batchMode` is the one deviation: its component (`cdrParserBatchMode.vue`) and getter/setter both
 * exist and are complete, but the component is never imported by the panel, so the field was
 * unreachable in the running Vue editor. It is declared here ahead of `batchCdrType`, the setting
 * it qualifies, so the module is configurable in full; drop this one entry to reproduce the Vue
 * form exactly.
 *
 * Not to be confused with `moddle-extensions/cdrParserProperties.json`, which despite the name is
 * a renamed copy of the Camunda moddle and says nothing about this module.
 */
export const cdrParserSchema: ModuleSchema = {
  type: 'CdrParser:CdrParser',
  label: 'CdrParser',
  fields: [
    { name: 'agreementMode', label: 'Agreement Mode', kind: 'select', options: ['RUNNING', 'FETCH_ONLY', 'DRAFT'] },
    { name: 'batchMode', label: 'Batch Mode', kind: 'select', options: ['SINGLE', 'BATCH', 'BOTH'] },
    // The Vue options label these with spaces where the value has underscores (HUAWEI_PGW_DATA_CDR
    // shows as "HUAWEI PGW DATA CDR", TAP_312 as "TAP 312"). The panel has no separate option
    // label, so the underscored values — the ones the backend reads — are what is shown.
    {
      name: 'batchCdrType',
      label: 'Batch Cdr Type',
      kind: 'select',
      options: [
        'HUAWEI_UNKNOWN_CDR',
        'HUAWEI_PGW_DATA_CDR',
        'HUAWEI_SGW_DATA_CDR',
        'HUAWEI_VOICE_SMS_CDR',
        'HUAWEI_SMSC_CDR',
        'HUAWEI_MMSC_CDR',
        'HUAWEI_SDP_CDR',
        'TAP_312',
        'ATS9900',
      ],
    },
    { name: 'commentDesc', label: 'Comment Desc', kind: 'text' },
  ],
};
