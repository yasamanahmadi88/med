import { FieldOption } from '../schema';
import { anyProcessSchema } from './any-process';

/**
 * Every field in this schema is a bare string typed out of a Vue component and a getter/setter
 * pair; nothing in the build checks that a property name matches the one the Vue editor wrote, so
 * a typo would silently store the value under a key the runtime never reads. These specs pin the
 * transcription against the Vue source so a later edit cannot quietly drift from it.
 *
 * `anyProcess` is also the one schema here whose element type is still an open question, so the
 * first spec is written to fail loudly if someone changes it without meaning to.
 */
describe('anyProcessSchema', () => {
  it('is keyed to bpmn:SequenceFlow — a placeholder, not a palette module type', () => {
    // `Panel/index.tsx` pushes the anyProcess components under `case 'bpmn:SequenceFlow'` and
    // nowhere else, so this is what the Vue editor did. It is deliberately *not* one of the
    // `<Module>:<Module>` types the palette creates: registering it shows this group on every
    // sequence flow in every diagram, plain BPMN ones included. Pinning the value here means the
    // decision to keep or change it is made on purpose rather than drifting in silently.
    expect(anyProcessSchema.type).toBe('bpmn:SequenceFlow');
  });

  it('declares all 8 fields the Vue module had', () => {
    // `bo-utils/anyProcessProperties/` holds 8 getter/setter pairs, one more than the panel ever
    // rendered. The count is the cheapest guard against a field being dropped during the port or
    // lost in a later merge — and the unrendered one is the likeliest to be dropped.
    expect(anyProcessSchema.fields).toHaveLength(8);
  });

  it('names a distinct, non-empty property for every field', () => {
    // Two fields sharing a name would make the panel show one entry's value in both and have the
    // second write clobber the first; an empty name would write to the bare engine prefix.
    const names = anyProcessSchema.fields.map(field => field.name);

    expect(names.every(name => name.length > 0)).toBe(true);
    expect(new Set(names).size).toBe(names.length);
  });

  it('labels every field', () => {
    // The provider passes the label straight to `translate`; a blank one renders an unlabelled
    // control that the user cannot identify.
    expect(anyProcessSchema.fields.every(field => field.label.length > 0)).toBe(true);
  });

  it('keeps the order the Vue panel rendered the fields in, the orphan last', () => {
    // The first seven are the sequence `Panel/index.tsx` pushed onto `renderComponents` under
    // `case 'bpmn:SequenceFlow'`: what to acknowledge, whether the hop is open, then the payload
    // rules, then routing. `msgType` has a component and a getter/setter but was never imported
    // into `index.tsx`, so it had no position; it is appended deliberately.
    expect(anyProcessSchema.fields.map(field => field.name)).toEqual([
      'ackMode',
      'status',
      'properties',
      'predicates',
      'transform',
      'routeKey',
      'commentDesc',
      'msgType',
    ]);
  });

  it('stores the route key under routeKey, not outputMsgType', () => {
    // `index.tsx` imports the component as `AnyProcessOutputMsgType` while the file is
    // `anyProcessRouteKey.vue` and `anyProcessRouteKeyUtil` reads `${prefix}:routeKey`. The stale
    // import alias is the trap: naming this `outputMsgType` would write an attribute nothing reads.
    expect(anyProcessSchema.fields.find(field => field.label === 'Route Key')?.name).toBe('routeKey');
  });

  it('uses the plural moddle names for properties and predicates', () => {
    // The component files are `anyProcessProperty.vue` and `anyProcessPredicate.vue` — singular —
    // but both utils read plural keys. Deriving a name from the filename would miss both.
    const names = anyProcessSchema.fields.map(field => field.name);

    expect(names).toContain('properties');
    expect(names).toContain('predicates');
  });

  it('offers exactly the option values the Vue selects offered', () => {
    // These are enum values the backend matches on, so an extra, missing or misspelt one produces
    // a diagram the engine rejects. The Vue <option> *text* was prettified for all four ack modes
    // ("NO ACK", "REC ACK", …); the underscored values below are what was stored.
    const options: Record<string, readonly FieldOption[]> = {};
    for (const field of anyProcessSchema.fields) {
      if (field.options) {
        options[field.name] = field.options;
      }
    }

    expect(options).toEqual({
      ackMode: ['NO_ACK', 'REC_ACK', 'VAL_ACK', 'PRC_ACK'],
      status: ['OPEN', 'CLOSE'],
    });
  });

  it('gives options to selects and only to selects', () => {
    // The provider builds `getOptions` off `kind === 'select'` alone: options on a text field are
    // silently ignored, and a select without them renders an empty dropdown.
    for (const field of anyProcessSchema.fields) {
      expect(field.options !== undefined).toBe(field.kind === 'select');
    }
  });

  it('has no numeric field', () => {
    // Every input in the Vue directory is a plain `<input>`, a `<textarea>` or a `<select>` — no
    // `<input type="number">` anywhere — so a `number` kind appearing here means a field was
    // transcribed from the wrong control.
    expect(anyProcessSchema.fields.some(field => field.kind === 'number')).toBe(false);
  });
});
