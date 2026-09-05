import { dbReceiverSchema } from './db-receiver';

/**
 * The schema is the whole feature for this module: the generic provider renders exactly what is
 * declared here and nothing checks it against the Vue original at runtime. A typo in a property
 * name loses a field's stored value silently, so these specs pin the declaration itself.
 */
describe('dbReceiverSchema', () => {
  it('is keyed to the type the palette creates', () => {
    // enhancementPaletteProvider builds this exact moddle type; a mismatch costs the element its
    // whole properties group with no error anywhere.
    expect(dbReceiverSchema.type).toBe('DbReceiver:DbReceiver');
  });

  it('declares the three fields the Vue panel composed, in order', () => {
    // All three components under `dbReceiverProperties/` are composed by the Vue panel, so the
    // module is fully covered by this list — nothing here is a judgement call.
    expect(dbReceiverSchema.fields.map(f => f.name)).toEqual(['agreementMode', 'outputMsgType', 'commentDesc']);
  });

  it('names a distinct, non-empty property for every field', () => {
    // The name is both the moddle attribute and the entry id, so a blank or duplicated one makes
    // two fields fight over one value.
    const names = dbReceiverSchema.fields.map(f => f.name);

    expect(names.every(name => name.length > 0)).toBe(true);
    expect(new Set(names).size).toBe(names.length);
  });

  it('offers the agreement modes the Vue select offered', () => {
    // The only select on this module. Its values are read by the backend, so an extra or renamed
    // option produces a diagram the engine rejects rather than a visibly broken form.
    const agreementMode = dbReceiverSchema.fields.find(f => f.name === 'agreementMode');

    expect(agreementMode?.kind).toBe('select');
    expect(agreementMode?.options).toEqual(['RUNNING', 'FETCH_ONLY', 'DRAFT']);
  });

  it('gives options to selects and to nothing else', () => {
    // The provider only calls getOptions for a select; options left on a text field are dead
    // weight, and a select without them renders as a dropdown with only the blank choice.
    for (const field of dbReceiverSchema.fields) {
      expect(field.options === undefined).toBe(field.kind !== 'select');
    }
  });
});
