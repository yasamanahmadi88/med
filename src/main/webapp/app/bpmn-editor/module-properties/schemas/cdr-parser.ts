import { AGREEMENT_MODE, ModuleSchema } from '../schema';

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
    { name: 'agreementMode', label: 'Agreement Mode', kind: 'select', options: AGREEMENT_MODE },
    { name: 'batchMode', label: 'Batch Mode', kind: 'select', options: ['SINGLE', 'BATCH', 'BOTH'] },
    // The Vue options label these with spaces where the value has underscores (HUAWEI_PGW_DATA_CDR
    // shows as "HUAWEI PGW DATA CDR", TAP_312 as "TAP 312"), so they carry the pair form: the
    // panel's SelectEntry renders `option.label` and stores `option.value`
    // (@bpmn-io/properties-panel/dist/index.esm.js:3931-3935).
    {
      name: 'batchCdrType',
      label: 'Batch Cdr Type',
      kind: 'select',
      options: [
        { value: 'HUAWEI_UNKNOWN_CDR', label: 'HUAWEI UNKNOWN CDR' },
        { value: 'HUAWEI_PGW_DATA_CDR', label: 'HUAWEI PGW DATA CDR' },
        { value: 'HUAWEI_SGW_DATA_CDR', label: 'HUAWEI SGW DATA CDR' },
        { value: 'HUAWEI_VOICE_SMS_CDR', label: 'HUAWEI VOICE SMS CDR' },
        { value: 'HUAWEI_SMSC_CDR', label: 'HUAWEI SMSC CDR' },
        { value: 'HUAWEI_MMSC_CDR', label: 'HUAWEI MMSC CDR' },
        { value: 'HUAWEI_SDP_CDR', label: 'HUAWEI SDP CDR' },
        { value: 'TAP_312', label: 'TAP 312' },
        'ATS9900',
      ],
    },
    { name: 'commentDesc', label: 'Comment Desc', kind: 'text' },
  ],
};
