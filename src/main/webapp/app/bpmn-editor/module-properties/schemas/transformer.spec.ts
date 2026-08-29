import { transformerSchema } from './transformer';

/**
 * The schema is the whole feature for this module: the generic provider renders exactly what is
 * declared here and nothing checks it against the Vue original at runtime. Both of this module's
 * real settings are selects over the same fixed list, so these specs pin it value for value.
 */
describe('transformerSchema', () => {
  const transformTypes = [
    'DTS',
    'MCCI_CHANGECARD',
    'BI_EVENT',
    'IPCC_TICKET',
    'IPCC_AGENT_CALLS',
    'IPCC_IVR_INBOUND',
    'CHARGE_BY_VOUCHER',
    'EVENT_FROM_DB_RECEIVER',
    'PAYMENT_ALL_COUNTRY',
  ];

  it('is keyed to the type the palette creates', () => {
    // rewritePaletteProvider builds this exact moddle type; a mismatch costs the element its whole
    // properties group with no error anywhere.
    expect(transformerSchema.type).toBe('Transformer:Transformer');
  });

  it('declares every field the Vue module had', () => {
    // Four field components exist under `transformerProperties/`, including the First Action the
    // Vue panel never imported. A dropped field is otherwise invisible.
    expect(transformerSchema.fields).toHaveLength(4);
  });

  it('names a distinct, non-empty property for every field', () => {
    // The name is both the moddle attribute and the entry id, so a blank or duplicated one makes
    // two fields fight over one value. It matters more than usual here: the two selects are
    // identical apart from their property names.
    const names = transformerSchema.fields.map(f => f.name);

    expect(names.every(name => name.length > 0)).toBe(true);
    expect(new Set(names).size).toBe(names.length);
    expect(names).toEqual(['agreementMode', 'transformType', 'firstAction', 'commentDesc']);
  });

  it('offers the agreement modes the Vue select offered', () => {
    // These values are read by the backend, so an extra or renamed option produces a diagram the
    // engine rejects rather than a visibly broken form.
    const agreementMode = transformerSchema.fields.find(f => f.name === 'agreementMode');

    expect(agreementMode?.kind).toBe('select');
    expect(agreementMode?.options).toEqual(['RUNNING', 'FETCH_ONLY', 'DRAFT']);
  });

  it('offers every named transformation, underscores intact, on both selects', () => {
    // The Vue options displayed these with spaces ("MCCI CHANGECARD") but stored the underscored
    // value. `transformType` and `firstAction` shared one list verbatim in the Vue source; letting
    // the two drift apart here would be invisible until a diagram failed at runtime.
    const transformType = transformerSchema.fields.find(f => f.name === 'transformType');
    const firstAction = transformerSchema.fields.find(f => f.name === 'firstAction');

    expect(transformType?.kind).toBe('select');
    expect(transformType?.options).toEqual(transformTypes);
    expect(firstAction?.kind).toBe('select');
    expect(firstAction?.options).toEqual(transformTypes);
  });

  it('gives options to selects and to nothing else', () => {
    // The provider only calls getOptions for a select; options left on a text field are dead
    // weight, and a select without them renders as a dropdown with only the blank choice.
    for (const field of transformerSchema.fields) {
      expect(field.options === undefined).toBe(field.kind !== 'select');
    }
  });
});
