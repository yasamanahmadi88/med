import { fragmenterSchema } from './fragmenter';

/**
 * The schema is the whole feature for this module: the generic provider renders exactly what is
 * declared here and nothing checks it against the Vue original at runtime. With a single field
 * there is no room for the group to survive a mistake in a degraded form — it either works or the
 * element has no properties at all.
 */
describe('fragmenterSchema', () => {
  it('is keyed to the type the palette creates', () => {
    // enhancementPaletteProvider builds this exact moddle type; a mismatch costs the element its
    // whole properties group with no error anywhere.
    expect(fragmenterSchema.type).toBe('Fragmenter:Fragmenter');
  });

  it('declares the one field the Vue module had', () => {
    // `fragmenterProperties/` holds exactly one component and one getter/setter — the agreement
    // mode. A second entry here would be invented, not ported.
    expect(fragmenterSchema.fields.map(f => f.name)).toEqual(['agreementMode']);
  });

  it('names a non-empty property for its field', () => {
    // The name is both the moddle attribute and the entry id; blank means the module's only
    // setting reads and writes nothing.
    expect(fragmenterSchema.fields[0].name.length).toBeGreaterThan(0);
  });

  it('offers the agreement modes the Vue select offered', () => {
    // These values are read by the backend, so an extra or renamed option produces a diagram the
    // engine rejects rather than a visibly broken form.
    expect(fragmenterSchema.fields[0].kind).toBe('select');
    expect(fragmenterSchema.fields[0].options).toEqual(['RUNNING', 'FETCH_ONLY', 'DRAFT']);
  });
});
