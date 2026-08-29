import { FieldOption } from '../schema';
import { fileTransmitterSchema } from './file-transmitter';

/**
 * Every field in this schema is a bare string typed out of a Vue component and a getter/setter
 * pair; nothing in the build checks that a property name matches the one the Vue editor wrote, so
 * a typo would silently store the value under a key the runtime never reads. These specs pin the
 * transcription against the Vue source so a later edit cannot quietly drift from it.
 */
describe('fileTransmitterSchema', () => {
  it('is keyed to the moddle type the palette creates', () => {
    // `Panel/index.tsx` switched on this exact string; the provider looks the schema up by it, so
    // a mismatch costs the element its whole properties group with no error anywhere.
    expect(fileTransmitterSchema.type).toBe('FileTransmitter:FileTransmitter');
  });

  it('declares all 11 fields the Vue module had', () => {
    // `bo-utils/fileTransmitterProperties/` holds 11 getter/setter pairs and `index.tsx` rendered
    // all 11 components. The count is the cheapest guard against a field being dropped during the
    // port or lost in a later merge.
    expect(fileTransmitterSchema.fields).toHaveLength(11);
  });

  it('names a distinct, non-empty property for every field', () => {
    // Two fields sharing a name would make the panel show one entry's value in both and have the
    // second write clobber the first; an empty name would write to the bare engine prefix.
    const names = fileTransmitterSchema.fields.map(field => field.name);

    expect(names.every(name => name.length > 0)).toBe(true);
    expect(new Set(names).size).toBe(names.length);
  });

  it('labels every field', () => {
    // The provider passes the label straight to `translate`; a blank one renders an unlabelled
    // control that the user cannot identify.
    expect(fileTransmitterSchema.fields.every(field => field.label.length > 0)).toBe(true);
  });

  it('keeps the order the Vue panel rendered the fields in', () => {
    // Not alphabetical and not the order of the files on disk — alphabetically `commentDesc` comes
    // first, whereas the panel showed it last. This is the sequence `Panel/index.tsx` pushed onto
    // `renderComponents` under `case 'FileTransmitter:FileTransmitter'`: protocol and format
    // first, then the destination path, then the credentials to reach it. Users navigate the form
    // by position, so the order is part of the port.
    expect(fileTransmitterSchema.fields.map(field => field.name)).toEqual([
      'agreementMode',
      'writeProtocol',
      'contentFormat',
      'directoryPath',
      'pathSeparator',
      'ip',
      'port',
      'username',
      'password',
      'FileNameJslt',
      'commentDesc',
    ]);
  });

  it('stores the file name template under the capitalised FileNameJslt', () => {
    // `fileTransmitterFileNameJsltUtil` reads `${prefix}:FileNameJslt`. Every other property in
    // this module is lowerCamelCase, so the odd one out is exactly the one a later cleanup would
    // "fix" — and thereby write to an attribute the runtime never reads.
    expect(fileTransmitterSchema.fields.find(field => field.label === 'File Name JSLT')?.name).toBe('FileNameJslt');
  });

  it('offers exactly the option values the Vue selects offered', () => {
    // These are enum values the backend matches on, so an extra, missing or misspelt one produces
    // a diagram the engine rejects. Note the Vue <option> *text* was prettified for one of them
    // ("FETCH ONLY"); the values below are what was stored. `writeProtocol` also carries a fourth
    // choice, `FILE`, that the mirror-image `FileReceiver.readProtocol` does not.
    const options: Record<string, readonly FieldOption[]> = {};
    for (const field of fileTransmitterSchema.fields) {
      if (field.options) {
        options[field.name] = field.options;
      }
    }

    expect(options).toEqual({
      agreementMode: ['RUNNING', 'FETCH_ONLY', 'DRAFT'],
      writeProtocol: ['FTP', 'SFTP', 'FTPS', 'FILE'],
      contentFormat: ['UTF8', 'BASE64'],
    });
  });

  it('gives options to selects and only to selects', () => {
    // The provider builds `getOptions` off `kind === 'select'` alone: options on a text field are
    // silently ignored, and a select without them renders an empty dropdown.
    for (const field of fileTransmitterSchema.fields) {
      expect(field.options !== undefined).toBe(field.kind === 'select');
    }
  });

  it('keeps port as text, the control the Vue form used', () => {
    // `fileTransmitterPort.vue` is a plain `<input>` with a validation regex, not
    // `<input type="number">` — this module has no numeric field at all. Typing it as `number`
    // here would change what the panel writes into the diagram.
    expect(fileTransmitterSchema.fields.find(field => field.name === 'port')?.kind).toBe('text');
    expect(fileTransmitterSchema.fields.some(field => field.kind === 'number')).toBe(false);
  });
});
