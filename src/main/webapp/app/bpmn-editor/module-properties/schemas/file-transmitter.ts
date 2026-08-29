import { ModuleSchema } from '../schema';

/**
 * `FileTransmitter` — writes the message out as a file, locally or over FTP/SFTP/FTPS.
 *
 * Ported from the Vue editor's `components/Panel/components/fileTransmitterProperties/` (labels and
 * controls) paired with `bo-utils/fileTransmitterProperties/` (moddle property names). Field order
 * is the order `components/Panel/index.tsx` pushed the components under
 * `case 'FileTransmitter:FileTransmitter'`, which is what the panel actually rendered — not the
 * alphabetical order of the files on disk, which would have put `Comment Desc` first.
 *
 * All eleven components in the Vue directory are rendered by that case, so nothing here is an
 * orphan and the panel's eleven fields are the module's eleven properties.
 *
 * Two caveats carried over from the Vue source, both flagged in the port report:
 *
 *  - `Auth Password` was a password input with a show/hide eye toggle. `FieldKind` has no masked
 *    kind, so it is `text` here, exactly as the ported `HttpReceiver.authPassword` is.
 *  - `IP Address` and `Port` carried client-side validation (a regex and an inline error message).
 *    The generic provider has no hook for per-field validation, so both are plain `text` and an
 *    invalid value is now accepted; `Port` was a text input in the Vue form too, not a number one,
 *    so typing it as `number` would change what the panel writes.
 */
export const fileTransmitterSchema: ModuleSchema = {
  type: 'FileTransmitter:FileTransmitter',
  label: 'FileTransmitter',
  fields: [
    { name: 'agreementMode', label: 'Agreement Mode', kind: 'select', options: ['RUNNING', 'FETCH_ONLY', 'DRAFT'] },
    // `FILE` — writing to a local path — is offered here but not by `FileReceiver.readProtocol`.
    { name: 'writeProtocol', label: 'Write Protocol', kind: 'select', options: ['FTP', 'SFTP', 'FTPS', 'FILE'] },
    { name: 'contentFormat', label: 'Content Format', kind: 'select', options: ['UTF8', 'BASE64'] },
    { name: 'directoryPath', label: 'Directory Path', kind: 'text' },
    { name: 'pathSeparator', label: 'Path Separator', kind: 'text' },
    // Labelled "IP Address" here where the sibling `FileReceiver` labels the same property "Ip".
    { name: 'ip', label: 'IP Address', kind: 'text' },
    { name: 'port', label: 'Port', kind: 'text' },
    { name: 'username', label: 'Username', kind: 'text' },
    { name: 'password', label: 'Auth Password', kind: 'text' },
    // Capitalised `FileNameJslt` in the moddle, not `fileNameJslt`: `fileTransmitterFileNameJsltUtil`
    // reads `${prefix}:FileNameJslt`. The lower-cased spelling would write an attribute nothing reads.
    { name: 'FileNameJslt', label: 'File Name JSLT', kind: 'textarea' },
    { name: 'commentDesc', label: 'Comment Desc', kind: 'text' },
  ],
};
