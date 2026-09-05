import { httpReceiverEventaSchema } from './http-receiver-eventa';

/**
 * The schema is the whole feature for this module: the generic provider renders exactly what is
 * declared here and nothing checks it against the Vue original at runtime. A typo in a property
 * name loses a field's stored value silently, so these specs pin the declaration itself.
 */
describe('httpReceiverEventaSchema', () => {
  it('is keyed to the type the palette creates', () => {
    // enhancementPaletteProvider builds this exact moddle type; a mismatch costs the element its
    // whole properties group with no error anywhere.
    expect(httpReceiverEventaSchema.type).toBe('HttpReceiverEventa:HttpReceiverEventa');
  });

  it('declares every field the Vue module had', () => {
    // Six field components exist under `httpReceiverEventaProperties/`, including the Agreement
    // Key the Vue panel never imported. A dropped field is otherwise invisible.
    expect(httpReceiverEventaSchema.fields).toHaveLength(6);
  });

  it('names a distinct, non-empty property for every field', () => {
    // The name is both the moddle attribute and the entry id, so a blank or duplicated one makes
    // two fields fight over one value.
    const names = httpReceiverEventaSchema.fields.map(f => f.name);

    expect(names.every(name => name.length > 0)).toBe(true);
    expect(new Set(names).size).toBe(names.length);
  });

  it('leaves Comment Desc where the Vue panel put it, ahead of Transformer Type', () => {
    // Every other module ends on Comment Desc; this one does not. Assuming the house pattern here
    // would quietly reorder the form, so the exception is pinned.
    expect(httpReceiverEventaSchema.fields.map(f => f.name)).toEqual([
      'agreementMode',
      'agreementKey',
      'transformer',
      'validator',
      'commentDesc',
      'transformerType',
    ]);
  });

  it('offers the agreement modes the Vue select offered', () => {
    // These values are read by the backend, so an extra or renamed option produces a diagram the
    // engine rejects rather than a visibly broken form.
    const agreementMode = httpReceiverEventaSchema.fields.find(f => f.name === 'agreementMode');

    expect(agreementMode?.kind).toBe('select');
    expect(agreementMode?.options).toEqual(['RUNNING', 'FETCH_ONLY', 'DRAFT']);
  });

  it('offers the two transformer languages', () => {
    // `transformerType` here, not `transferType` as on the plain HttpReceiver — same two values,
    // different property, and mixing them up would strand the setting.
    const transformerType = httpReceiverEventaSchema.fields.find(f => f.name === 'transformerType');

    expect(transformerType?.kind).toBe('select');
    expect(transformerType?.options).toEqual(['JSLT', 'XSLT']);
  });

  it('gives options to selects and to nothing else', () => {
    // The provider only calls getOptions for a select; options left on a text field are dead
    // weight, and a select without them renders as a dropdown with only the blank choice.
    for (const field of httpReceiverEventaSchema.fields) {
      expect(field.options === undefined).toBe(field.kind !== 'select');
    }
  });
});
