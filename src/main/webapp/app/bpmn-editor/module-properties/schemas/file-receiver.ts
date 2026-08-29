import { ModuleSchema } from '../schema';

/**
 * `FileReceiver` — polls a local or remote directory, picks up matching files and post-processes
 * them once read.
 *
 * Ported from the Vue editor's `components/Panel/components/fileReceiverProperties/` (labels and
 * controls) paired with `bo-utils/fileReceiverProperties/` (moddle property names). Field order is
 * the order `components/Panel/index.tsx` pushed the components under
 * `case 'FileReceiver:FileReceiver'`, which is what the panel actually rendered — not the
 * alphabetical order of the files on disk.
 *
 * Two caveats carried over from the Vue source, both flagged in the port report:
 *
 *  - `Auth Password` was a password input with a show/hide eye toggle. `FieldKind` has no masked
 *    kind, so it is `text` here, exactly as the ported `HttpReceiver.authPassword` is.
 *  - `Unzip` was `<input type="number" min="0" max="1">` — a boolean wearing a number's clothes.
 *    It stays `number` because that is what the Vue form wrote to the diagram; changing it to a
 *    checkbox would change the stored value.
 *
 * `partyTimeOut` and `successResponsePattern` are the last two entries. Their components and
 * getter/setters exist in the Vue project but `index.tsx` never imported them, so the panel never
 * showed them; they are included to keep the module's 22 declared properties addressable and
 * appended last because they have no established position.
 */
export const fileReceiverSchema: ModuleSchema = {
  type: 'FileReceiver:FileReceiver',
  label: 'FileReceiver',
  fields: [
    { name: 'agreementMode', label: 'Agreement Mode', kind: 'select', options: ['RUNNING', 'FETCH_ONLY', 'DRAFT'] },
    { name: 'readProtocol', label: 'Read Protocol', kind: 'select', options: ['FTP', 'SFTP', 'FTPS'] },
    // Stored as `fileScanPolicy`, not `scanPolicy` — the only field whose property name does not
    // follow its label.
    { name: 'fileScanPolicy', label: 'Scan Policy', kind: 'select', options: ['SUBFOLDERS', 'ROOT'] },
    { name: 'directoryPath', label: 'Directory Path', kind: 'text' },
    { name: 'subFolderPattern', label: 'Sub Folder Pattern', kind: 'text' },
    { name: 'fileFormatPattern', label: 'File Format Pattern', kind: 'text' },
    { name: 'lockExtension', label: 'Lock Extension', kind: 'text' },
    { name: 'ip', label: 'Ip', kind: 'text' },
    // `port` was a plain text input with maxlength=10 in the Vue form, not a number input.
    { name: 'port', label: 'Port', kind: 'text' },
    { name: 'username', label: 'Username', kind: 'text' },
    { name: 'password', label: 'Auth Password', kind: 'text' },
    { name: 'pathSeparator', label: 'Path Separator', kind: 'text' },
    {
      name: 'postProcessingAction',
      label: 'Post Processing Action',
      kind: 'select',
      options: ['RENAME', 'REMOVE', 'MOVE', 'RENAME_AND_MOVE'],
    },
    { name: 'moveDir', label: 'Move Dir', kind: 'text' },
    { name: 'renameExtension', label: 'Rename Extension', kind: 'text' },
    { name: 'maxFileRead', label: 'Max File Read', kind: 'number' },
    { name: 'contentFormat', label: 'Content Format', kind: 'select', options: ['UTF8', 'BASE64'] },
    { name: 'operationPostProcessingOnly', label: 'Operation Post Processing Only', kind: 'number' },
    { name: 'unzip', label: 'Unzip', kind: 'number' },
    { name: 'commentDesc', label: 'Comment Desc', kind: 'text' },
    { name: 'partyTimeOut', label: 'Party Time Out', kind: 'number' },
    { name: 'successResponsePattern', label: 'Success Response Pattern', kind: 'text' },
  ],
};
