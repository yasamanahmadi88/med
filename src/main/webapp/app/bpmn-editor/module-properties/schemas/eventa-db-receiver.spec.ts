import { dbReceiverSchema } from './db-receiver';
import { eventaDbReceiverSchema } from './eventa-db-receiver';

/**
 * The schema is the whole feature for this module: the generic provider renders exactly what is
 * declared here and nothing checks it against the Vue original at runtime. A typo in a property
 * name loses a field's stored value silently, so these specs pin the declaration itself.
 */
describe('eventaDbReceiverSchema', () => {
  it('is keyed to the type the palette creates', () => {
    // enhancementPaletteProvider builds this exact moddle type. It is the one thing that must NOT
    // match `DbReceiver`, whose fields are otherwise identical — a copy-paste slip here would give
    // two palette entries the same group and leave this one with none.
    expect(eventaDbReceiverSchema.type).toBe('EventaDbReceiver:EventaDbReceiver');
    expect(eventaDbReceiverSchema.type).not.toBe(dbReceiverSchema.type);
  });

  it('declares the three fields the Vue panel composed, in order', () => {
    // All three components under `eventaDbReceiverProperties/` are composed by the Vue panel, so
    // the module is fully covered by this list.
    expect(eventaDbReceiverSchema.fields.map(f => f.name)).toEqual(['agreementMode', 'outputMsgType', 'commentDesc']);
  });

  it('names a distinct, non-empty property for every field', () => {
    // The name is both the moddle attribute and the entry id, so a blank or duplicated one makes
    // two fields fight over one value.
    const names = eventaDbReceiverSchema.fields.map(f => f.name);

    expect(names.every(name => name.length > 0)).toBe(true);
    expect(new Set(names).size).toBe(names.length);
  });

  it('offers the agreement modes the Vue select offered', () => {
    // The only select on this module. Its values are read by the backend, so an extra or renamed
    // option produces a diagram the engine rejects rather than a visibly broken form.
    const agreementMode = eventaDbReceiverSchema.fields.find(f => f.name === 'agreementMode');

    expect(agreementMode?.kind).toBe('select');
    expect(agreementMode?.options).toEqual(['RUNNING', 'FETCH_ONLY', 'DRAFT']);
  });

  it('gives options to selects and to nothing else', () => {
    // The provider only calls getOptions for a select; options left on a text field are dead
    // weight, and a select without them renders as a dropdown with only the blank choice.
    for (const field of eventaDbReceiverSchema.fields) {
      expect(field.options === undefined).toBe(field.kind !== 'select');
    }
  });
});
