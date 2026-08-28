import { cdrParserSchema } from './cdr-parser';

/**
 * The schema is the whole feature for this module: the generic provider renders exactly what is
 * declared here and nothing checks it against the Vue original at runtime. The CDR dialect list is
 * the substance of the module, so these specs pin it value for value.
 */
describe('cdrParserSchema', () => {
  it('is keyed to the type the palette creates', () => {
    // enhancementPaletteProvider builds this exact moddle type; a mismatch costs the element its
    // whole properties group with no error anywhere.
    expect(cdrParserSchema.type).toBe('CdrParser:CdrParser');
  });

  it('declares every field the Vue module had', () => {
    // Four field components exist under `cdrParserProperties/`, including the Batch Mode the Vue
    // panel never imported. A dropped field is otherwise invisible.
    expect(cdrParserSchema.fields).toHaveLength(4);
  });

  it('names a distinct, non-empty property for every field', () => {
    // The name is both the moddle attribute and the entry id, so a blank or duplicated one makes
    // two fields fight over one value.
    const names = cdrParserSchema.fields.map(f => f.name);

    expect(names.every(name => name.length > 0)).toBe(true);
    expect(new Set(names).size).toBe(names.length);
  });

  it('puts the batch mode ahead of the dialect it qualifies', () => {
    // Declaration order is render order; reading "Batch Cdr Type" before knowing whether the feed
    // is batched at all is the wrong way round.
    expect(cdrParserSchema.fields.map(f => f.name)).toEqual(['agreementMode', 'batchMode', 'batchCdrType', 'commentDesc']);
  });

  it('offers the agreement modes the Vue select offered', () => {
    // These values are read by the backend, so an extra or renamed option produces a diagram the
    // engine rejects rather than a visibly broken form.
    const agreementMode = cdrParserSchema.fields.find(f => f.name === 'agreementMode');

    expect(agreementMode?.kind).toBe('select');
    expect(agreementMode?.options).toEqual(['RUNNING', 'FETCH_ONLY', 'DRAFT']);
  });

  it('offers the three batch modes', () => {
    const batchMode = cdrParserSchema.fields.find(f => f.name === 'batchMode');

    expect(batchMode?.kind).toBe('select');
    expect(batchMode?.options).toEqual(['SINGLE', 'BATCH', 'BOTH']);
  });

  it('offers every CDR dialect the Vue select offered, underscores intact', () => {
    // The Vue options displayed these with spaces ("HUAWEI PGW DATA CDR", "TAP 312") but stored
    // the underscored value. Transcribing the display text instead would store nine values the
    // parser cannot dispatch on — a mistake nothing else in the stack would catch.
    const batchCdrType = cdrParserSchema.fields.find(f => f.name === 'batchCdrType');

    expect(batchCdrType?.kind).toBe('select');
    expect(batchCdrType?.options).toEqual([
      'HUAWEI_UNKNOWN_CDR',
      'HUAWEI_PGW_DATA_CDR',
      'HUAWEI_SGW_DATA_CDR',
      'HUAWEI_VOICE_SMS_CDR',
      'HUAWEI_SMSC_CDR',
      'HUAWEI_MMSC_CDR',
      'HUAWEI_SDP_CDR',
      'TAP_312',
      'ATS9900',
    ]);
  });

  it('gives options to selects and to nothing else', () => {
    // The provider only calls getOptions for a select; options left on a text field are dead
    // weight, and a select without them renders as a dropdown with only the blank choice.
    for (const field of cdrParserSchema.fields) {
      expect(field.options === undefined).toBe(field.kind !== 'select');
    }
  });
});
