import { dbTransmitterSchema } from './db-transmitter';

/**
 * The schema is the whole feature for this module: the generic provider renders exactly what is
 * declared here and nothing validates it against the Vue original at runtime. A typo in a property
 * name loses a field's stored value silently — the panel still draws the input, it just reads and
 * writes an attribute nothing else uses — so these specs pin the declaration itself.
 */
describe('dbTransmitterSchema', () => {
  it('is keyed to the type the palette creates', () => {
    // enhancementPaletteProvider builds this exact moddle type; a mismatch costs the element its
    // whole properties group with no error anywhere.
    expect(dbTransmitterSchema.type).toBe('DbTransmitter:DbTransmitter');
  });

  it('declares every field the Vue module had', () => {
    // Thirteen field components exist under `dbTransmitterProperties/`, one per configurable
    // property, including the Output Msg Type the Vue panel left commented out. A dropped field is
    // otherwise invisible: the panel renders fine, one property just can no longer be set.
    expect(dbTransmitterSchema.fields).toHaveLength(13);
  });

  it('names a distinct, non-empty property for every field', () => {
    // The name is both the moddle attribute and the entry id, so a blank or duplicated one makes
    // two fields fight over one value.
    const names = dbTransmitterSchema.fields.map(f => f.name);

    expect(names.every(name => name.length > 0)).toBe(true);
    expect(new Set(names).size).toBe(names.length);
  });

  it('keeps the connection fields ahead of the pool tuning, as the Vue panel did', () => {
    // Order is declaration order in the rendered panel. Someone configuring a datasource fills in
    // url/credentials/driver first; burying them under the Hikari knobs would be a regression a
    // field-count check cannot catch.
    expect(dbTransmitterSchema.fields.map(f => f.name)).toEqual([
      'agreementMode',
      'url',
      'username',
      'password',
      'driverClassName',
      'minimumIdle',
      'maximumPoolSize',
      'idleTimeout',
      'maxLifeTime',
      'connectionTimeout',
      'poolName',
      'outputMsgType',
      'commentDesc',
    ]);
  });

  it('offers the agreement modes the Vue select offered', () => {
    // The only select on this module. Its values are read by the backend, so an extra or renamed
    // option produces a diagram the engine rejects rather than a visibly broken form.
    const agreementMode = dbTransmitterSchema.fields.find(f => f.name === 'agreementMode');

    expect(agreementMode?.kind).toBe('select');
    expect(agreementMode?.options).toEqual(['RUNNING', 'FETCH_ONLY', 'DRAFT']);
  });

  it('gives options to selects and to nothing else', () => {
    // The provider only calls getOptions for a select; options left on a text field are dead
    // weight, and a select without them renders as a dropdown with only the blank choice.
    for (const field of dbTransmitterSchema.fields) {
      expect(field.options === undefined).toBe(field.kind !== 'select');
    }
  });

  it('renders the Hikari timings as numbers', () => {
    // These were `<input type="number">` in Vue. Kept as numbers so the panel does not accept
    // "30 seconds" for a millisecond property.
    const kinds = ['minimumIdle', 'maximumPoolSize', 'idleTimeout', 'maxLifeTime', 'connectionTimeout'].map(
      name => dbTransmitterSchema.fields.find(f => f.name === name)?.kind,
    );

    expect(kinds).toEqual(['number', 'number', 'number', 'number', 'number']);
  });
});
