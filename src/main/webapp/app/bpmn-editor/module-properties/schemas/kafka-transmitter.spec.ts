import { kafkaTransmitterSchema } from './kafka-transmitter';

/**
 * Every field in this schema is a bare string typed out of a Vue component and a getter/setter
 * pair; nothing in the build checks that a property name matches the one the Vue editor wrote, so
 * a typo would silently store the value under a key the runtime never reads. These specs pin the
 * transcription against the Vue source so a later edit cannot quietly drift from it.
 */
describe('kafkaTransmitterSchema', () => {
  it('is keyed to the moddle type the palette creates', () => {
    // `Panel/index.tsx` switched on this exact string; the provider looks the schema up by it, so
    // a mismatch costs the element its whole properties group with no error anywhere.
    expect(kafkaTransmitterSchema.type).toBe('KafkaTransmitter:KafkaTransmitter');
  });

  it('declares all 9 fields the Vue module had', () => {
    // `bo-utils/kafkaTransmitterProperties/` holds 9 getter/setter pairs, two more than the panel
    // ever rendered. The count is the cheapest guard against one being dropped during the port or
    // lost in a later merge — and the two unrendered ones are the likeliest to be dropped.
    expect(kafkaTransmitterSchema.fields).toHaveLength(9);
  });

  it('names a distinct, non-empty property for every field', () => {
    // Two fields sharing a name would make the panel show one entry's value in both and have the
    // second write clobber the first; an empty name would write to the bare engine prefix.
    const names = kafkaTransmitterSchema.fields.map(field => field.name);

    expect(names.every(name => name.length > 0)).toBe(true);
    expect(new Set(names).size).toBe(names.length);
  });

  it('labels every field', () => {
    // The provider passes the label straight to `translate`; a blank one renders an unlabelled
    // control that the user cannot identify.
    expect(kafkaTransmitterSchema.fields.every(field => field.label.length > 0)).toBe(true);
  });

  it('keeps the order the Vue panel rendered the fields in, orphans last', () => {
    // The first seven are the sequence `Panel/index.tsx` pushed onto `renderComponents` under
    // `case 'KafkaTransmitter:KafkaTransmitter'` — note `commentDesc` sits second here, not last
    // as in every other module, which is exactly the kind of detail an alphabetical sort would
    // destroy. `agreementKey` and `headers` have components and getter/setters but were never
    // pushed, so they had no position; they are appended deliberately.
    expect(kafkaTransmitterSchema.fields.map(field => field.name)).toEqual([
      'agreementMode',
      'commentDesc',
      'topic',
      'bootstrapServer',
      'kafkaHeader',
      'kafkaBody',
      'kafkaKey',
      'agreementKey',
      'headers',
    ]);
  });

  it('keeps kafkaHeader and headers as two separate properties', () => {
    // `kafkaTransmitterKafkaHeaderUtil` reads `${prefix}:kafkaHeader` (the record's header
    // expression, a textarea) and `kafkaTransmitterHeadersUtil` reads `${prefix}:headers` (a
    // one-line input). The names are close enough that collapsing them looks like a tidy-up, but
    // it would silently merge two independent values.
    const byName = new Map(kafkaTransmitterSchema.fields.map(field => [field.name, field]));

    expect(byName.get('kafkaHeader')?.kind).toBe('textarea');
    expect(byName.get('headers')?.kind).toBe('text');
  });

  it('offers exactly the option values the Vue select offered', () => {
    // `agreementMode` is the module's only select, and these are enum values the backend matches
    // on, so an extra, missing or misspelt one produces a diagram the engine rejects. The Vue
    // <option> *text* read "FETCH ONLY"; the value below is what was stored.
    const options: Record<string, readonly string[]> = {};
    for (const field of kafkaTransmitterSchema.fields) {
      if (field.options) {
        options[field.name] = field.options;
      }
    }

    expect(options).toEqual({ agreementMode: ['RUNNING', 'FETCH_ONLY', 'DRAFT'] });
  });

  it('gives options to selects and only to selects', () => {
    // The provider builds `getOptions` off `kind === 'select'` alone: options on a text field are
    // silently ignored, and a select without them renders an empty dropdown.
    for (const field of kafkaTransmitterSchema.fields) {
      expect(field.options !== undefined).toBe(field.kind === 'select');
    }
  });

  it('uses number only where the Vue control was <input type="number">', () => {
    // `agreementKey` is the module's one numeric input (`min="0" max="999999999"`). `topic` and
    // `bootstrapServer` are text despite often holding numeric-looking values.
    const numeric = kafkaTransmitterSchema.fields.filter(field => field.kind === 'number').map(field => field.name);

    expect(numeric).toEqual(['agreementKey']);
  });
});
