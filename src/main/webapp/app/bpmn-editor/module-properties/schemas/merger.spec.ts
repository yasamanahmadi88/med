import { optionLabel, optionValue } from '../schema';
import { mergerSchema } from './merger';

/**
 * Merger is the most select-heavy module in the editor: seven of its thirteen fields are closed
 * value lists that the merge engine branches on. The panel would happily render a misspelled
 * option, and the resulting diagram fails only later, in the engine — so the option lists get
 * checked here, value by value, against the Vue `<option>` markup they were transcribed from.
 */
describe('mergerSchema', () => {
  it('is keyed to the type the palette creates', () => {
    // enhancementPaletteProvider builds this exact moddle type; a mismatch costs the element its
    // whole properties group with no error anywhere.
    expect(mergerSchema.type).toBe('Merger:Merger');
  });

  it('declares every field the Vue module had', () => {
    // Thirteen field components under `mergerProperties/`, all thirteen composed by the Vue panel.
    expect(mergerSchema.fields).toHaveLength(13);
  });

  it('names a distinct, non-empty property for every field', () => {
    // The name is both the moddle attribute and the entry id, so a blank or duplicated one makes
    // two fields fight over one value.
    const names = mergerSchema.fields.map(f => f.name);

    expect(names.every(name => name.length > 0)).toBe(true);
    expect(new Set(names).size).toBe(names.length);
  });

  it('keeps the two oddly named properties exactly as the Vue getters wrote them', () => {
    // `isIncremental234` and `mergerForceNextDay` both read wrong next to their labels and invite a
    // tidy-up. Existing diagrams hold values under those names; renaming either would orphan them
    // silently, so the ugliness is pinned deliberately.
    const name = (label: string): string | undefined => mergerSchema.fields.find(f => f.label === label)?.name;

    expect(name('Is Incremental')).toBe('isIncremental234');
    expect(name('Force Next Day')).toBe('mergerForceNextDay');
  });

  it('follows the Vue panel order, not the filename order', () => {
    // The Vue components sort alphabetically on disk but were composed in a deliberate sequence:
    // what to do on the first message, then on the last, then on expiry. Declaration order is
    // render order, so re-sorting the array quietly reshuffles the form.
    expect(mergerSchema.fields.map(f => f.name)).toEqual([
      'agreementMode',
      'isIncremental234',
      'firstAction',
      'lastSaveAction',
      'lastSendAction',
      'expireAction',
      'mergerForceNextDay',
      'expirationCount',
      'expirationTime',
      'expireTimeOfDay',
      'commentDesc',
      'expiredMsgType',
      'mergedMsgType',
    ]);
  });

  it('offers exactly the values the Vue selects offered', () => {
    // Transcribed from the `<option value="...">` lists. Only the value is stored, and only the
    // value is what the engine matches on; the text the user picks from is checked below.
    const values = (name: string): string[] | undefined => mergerSchema.fields.find(f => f.name === name)?.options?.map(optionValue);

    expect(values('agreementMode')).toEqual(['RUNNING', 'FETCH_ONLY', 'DRAFT']);
    expect(values('isIncremental234')).toEqual(['0', '1']);
    expect(values('firstAction')).toEqual(['SAVE_ONLY', 'SAVE_AND_SEND']);
    expect(values('lastSaveAction')).toEqual(['SAVE_MERGED', 'SAVE_NEW', 'SAVE_OLD', 'NOT_SAVE']);
    expect(values('lastSendAction')).toEqual(['SEND_MERGED', 'SEND_NEW', 'SEND_OLD', 'NOT_SEND']);
    expect(values('expireAction')).toEqual(['SEND', 'NOT_SEND']);
    expect(values('mergerForceNextDay')).toEqual(['0', '1']);
  });

  it('shows the text the Vue selects showed, not the stored value', () => {
    // Seven selects, and the Vue markup prettified every one of them: none of these lists reads
    // the same as its values. A Merger whose "Force Next Day" offers 0 and 1 rather than Is Not
    // and Is is a different form, however identical the diagram it writes.
    const labels = (name: string): string[] | undefined => mergerSchema.fields.find(f => f.name === name)?.options?.map(optionLabel);

    expect(labels('agreementMode')).toEqual(['RUNNING', 'FETCH ONLY', 'DRAFT']);
    expect(labels('isIncremental234')).toEqual(['No', 'Yes']);
    expect(labels('firstAction')).toEqual(['SAVE ONLY', 'SAVE AND SEND']);
    expect(labels('lastSaveAction')).toEqual(['SAVE MERGED', 'SAVE NEW', 'SAVE OLD', 'NOT SAVE']);
    expect(labels('lastSendAction')).toEqual(['SEND MERGED', 'SEND NEW', 'SEND OLD', 'NOT SEND']);
    expect(labels('expireAction')).toEqual(['SEND', 'NOT SEND']);
    expect(labels('mergerForceNextDay')).toEqual(['Is Not', 'Is']);
  });

  it('gives options to selects and to nothing else', () => {
    // The provider only calls getOptions for a select; options left on a text field are dead
    // weight, and a select without them renders as a dropdown with only the blank choice.
    for (const field of mergerSchema.fields) {
      expect(field.options === undefined).toBe(field.kind !== 'select');
    }
  });

  it('counts and times as numbers, message types as free text', () => {
    // Expiration is a count and a millisecond duration; the message types are arbitrary strings.
    // Getting these backwards makes the panel reject valid input rather than fail loudly.
    const kind = (name: string): string | undefined => mergerSchema.fields.find(f => f.name === name)?.kind;

    expect(kind('expirationCount')).toBe('number');
    expect(kind('expirationTime')).toBe('number');
    expect(kind('expiredMsgType')).toBe('text');
    expect(kind('mergedMsgType')).toBe('text');
  });
});
