import { csvTransformerSchema } from './csv-transformer';

/**
 * The schema is the whole feature for this module: the generic provider renders exactly what is
 * declared here and nothing checks it against the Vue original at runtime. A typo in a property
 * name loses a field's stored value silently — the panel still draws the control, it just reads
 * and writes an attribute nothing else uses — so these specs pin the declaration itself.
 */
describe('csvTransformerSchema', () => {
  it('is keyed to the type the palette creates', () => {
    // enhancementPaletteProvider builds this exact moddle type; a mismatch costs the element its
    // whole properties group with no error anywhere.
    expect(csvTransformerSchema.type).toBe('CsvTransformer:CsvTransformer');
  });

  it('declares every field the Vue module had', () => {
    // Seven field components exist under `csvTransformerProperties/`, all seven composed by the
    // Vue panel. A dropped field is otherwise invisible: the form renders fine, one property just
    // can no longer be set.
    expect(csvTransformerSchema.fields).toHaveLength(7);
  });

  it('names a distinct, non-empty property for every field', () => {
    // The name is both the moddle attribute and the entry id, so a blank or duplicated one makes
    // two fields fight over one value.
    const names = csvTransformerSchema.fields.map(f => f.name);

    expect(names.every(name => name.length > 0)).toBe(true);
    expect(new Set(names).size).toBe(names.length);
  });

  it('keeps the capitalised RefMsgTypeJslt property name', () => {
    // The Vue getter reads `${prefix}:RefMsgTypeJslt` — capital R, unlike every other property on
    // this module. Normalising it would read an attribute no saved diagram has.
    expect(csvTransformerSchema.fields.map(f => f.name)).toContain('RefMsgTypeJslt');
  });

  it('renders the fields in the Vue panel order', () => {
    // Declaration order is render order. The filenames sort differently from the panel's
    // `renderComponents.push` list, so this is the check that catches a re-sorted paste.
    expect(csvTransformerSchema.fields.map(f => f.name)).toEqual([
      'agreementMode',
      'haveHeader',
      'recordSeparator',
      'fieldSeparator',
      'RefMsgTypeJslt',
      'numberOfFields',
      'commentDesc',
    ]);
  });

  it('offers the agreement modes the Vue select offered', () => {
    // These values are read by the backend, so an extra or renamed option produces a diagram the
    // engine rejects rather than a visibly broken form.
    const agreementMode = csvTransformerSchema.fields.find(f => f.name === 'agreementMode');

    expect(agreementMode?.kind).toBe('select');
    expect(agreementMode?.options).toEqual(['RUNNING', 'FETCH_ONLY', 'DRAFT']);
  });

  it('stores the header flag as the strings the Vue select stored', () => {
    // The Vue control was a `<select>` labelled NO/YES over the values '0' and '1' (a stray
    // `type="checkbox"` attribute on it means nothing). Storing booleans instead would write a
    // value the backend does not recognise.
    const haveHeader = csvTransformerSchema.fields.find(f => f.name === 'haveHeader');

    expect(haveHeader?.kind).toBe('select');
    expect(haveHeader?.options).toEqual(['0', '1']);
  });

  it('gives options to selects and to nothing else', () => {
    // The provider only calls getOptions for a select; options left on a text field are dead
    // weight, and a select without them renders as a dropdown with only the blank choice.
    for (const field of csvTransformerSchema.fields) {
      expect(field.options === undefined).toBe(field.kind !== 'select');
    }
  });
});
