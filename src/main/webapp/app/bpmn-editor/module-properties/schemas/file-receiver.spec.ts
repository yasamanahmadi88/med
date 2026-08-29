import { FieldOption } from '../schema';
import { fileReceiverSchema } from './file-receiver';

/**
 * `FileReceiver` is the largest of the ported modules, and every one of its fields is a bare
 * string typed into a declaration — nothing in the build checks that a property name matches the
 * one the Vue editor wrote, so a typo would silently store the value under a key the runtime never
 * reads. These specs are that check: they pin the transcription against the Vue source so a later
 * edit cannot quietly drift from it.
 */
describe('fileReceiverSchema', () => {
  it('is keyed to the moddle type the palette creates', () => {
    // `Panel/index.tsx` switched on this exact string; the provider looks the schema up by it, so
    // a mismatch costs the element its whole properties group with no error anywhere.
    expect(fileReceiverSchema.type).toBe('FileReceiver:FileReceiver');
  });

  it('declares all 22 fields the Vue module had', () => {
    // `bo-utils/fileReceiverProperties/` holds 22 getter/setter pairs. The count is the cheapest
    // guard against a field being dropped during the port or lost in a later merge.
    expect(fileReceiverSchema.fields).toHaveLength(22);
  });

  it('names a distinct, non-empty property for every field', () => {
    // Two fields sharing a name would make the panel show one entry's value in both and have the
    // second write clobber the first; an empty name would write to the bare engine prefix.
    const names = fileReceiverSchema.fields.map(field => field.name);

    expect(names.every(name => name.length > 0)).toBe(true);
    expect(new Set(names).size).toBe(names.length);
  });

  it('labels every field', () => {
    // The provider passes the label straight to `translate`; a blank one renders an unlabelled
    // control that the user cannot identify.
    expect(fileReceiverSchema.fields.every(field => field.label.length > 0)).toBe(true);
  });

  it('keeps the order the Vue panel rendered the fields in', () => {
    // Not alphabetical, and not the order of the files on disk: this is the sequence
    // `Panel/index.tsx` pushed onto `renderComponents` under `case 'FileReceiver:FileReceiver'`,
    // which groups connection details after the paths they apply to. Users navigate the form by
    // position, so the order is part of the port.
    expect(fileReceiverSchema.fields.map(field => field.name)).toEqual([
      'agreementMode',
      'readProtocol',
      'fileScanPolicy',
      'directoryPath',
      'subFolderPattern',
      'fileFormatPattern',
      'lockExtension',
      'ip',
      'port',
      'username',
      'password',
      'pathSeparator',
      'postProcessingAction',
      'moveDir',
      'renameExtension',
      'maxFileRead',
      'contentFormat',
      'operationPostProcessingOnly',
      'unzip',
      'commentDesc',
      // Both have a component and a getter/setter in the Vue project but were never imported into
      // `index.tsx`, so the panel never rendered them. Appended last, deliberately.
      'partyTimeOut',
      'successResponsePattern',
    ]);
  });

  it('stores the scan policy under fileScanPolicy', () => {
    // The one field whose property name does not follow its label — `fileReceiverScanPolicyUtil`
    // reads `${prefix}:fileScanPolicy`. Deriving it from the label would give `scanPolicy` and
    // write to an attribute nothing reads.
    expect(fileReceiverSchema.fields.find(field => field.label === 'Scan Policy')?.name).toBe('fileScanPolicy');
  });

  it('offers exactly the option values the Vue selects offered', () => {
    // These are enum values the backend matches on, so an extra, missing or misspelt one produces
    // a diagram the engine rejects. Note the Vue <option> *text* was prettified for three of them
    // ("FETCH ONLY", "RENAME AND MOVE", "SUB FOLDERS"); the values below are what was stored.
    const options: Record<string, readonly FieldOption[]> = {};
    for (const field of fileReceiverSchema.fields) {
      if (field.options) {
        options[field.name] = field.options;
      }
    }

    expect(options).toEqual({
      agreementMode: ['RUNNING', 'FETCH_ONLY', 'DRAFT'],
      readProtocol: ['FTP', 'SFTP', 'FTPS'],
      fileScanPolicy: ['SUBFOLDERS', 'ROOT'],
      postProcessingAction: ['RENAME', 'REMOVE', 'MOVE', 'RENAME_AND_MOVE'],
      contentFormat: ['UTF8', 'BASE64'],
    });
  });

  it('gives options to selects and only to selects', () => {
    // The provider builds `getOptions` off `kind === 'select'` alone: options on a text field are
    // silently ignored, and a select without them renders an empty dropdown.
    for (const field of fileReceiverSchema.fields) {
      expect(field.options !== undefined).toBe(field.kind === 'select');
    }
  });

  it('uses number only where the Vue control was <input type="number">', () => {
    // Four numeric inputs; `port` in particular was a *text* input with maxlength=10 despite
    // holding a number, and typing it as `number` here would change what the panel writes.
    const numeric = fileReceiverSchema.fields.filter(field => field.kind === 'number').map(field => field.name);

    expect(numeric).toEqual(['maxFileRead', 'operationPostProcessingOnly', 'unzip', 'partyTimeOut']);
  });
});
