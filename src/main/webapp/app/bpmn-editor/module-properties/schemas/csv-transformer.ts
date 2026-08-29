import { ModuleSchema } from '../schema';

/**
 * `CsvTransformer` — how a delimited record is cut up: the separators and field count first, then
 * the JSLT that names the resulting message type.
 *
 * Field order follows `case 'CsvTransformer:CsvTransformer'` in the Vue panel's
 * `components/Panel/index.tsx`. The filenames sort differently and would put the header flag and
 * the separators on opposite sides of the form.
 *
 * `Ref Msg Type JSLT` writes `RefMsgTypeJslt` — capitalised, alone among this module's properties.
 * That is what the Vue getter reads, so it is what existing diagrams hold; lower-casing it here
 * would read an attribute no saved diagram has.
 */
export const csvTransformerSchema: ModuleSchema = {
  type: 'CsvTransformer:CsvTransformer',
  label: 'CsvTransformer',
  fields: [
    { name: 'agreementMode', label: 'Agreement Mode', kind: 'select', options: ['RUNNING', 'FETCH_ONLY', 'DRAFT'] },
    // Despite a stray `type="checkbox"` attribute, the Vue control is a `<select>` storing the
    // strings '0' and '1', labelled NO and YES. The values are what reaches the diagram.
    { name: 'haveHeader', label: 'Have Header?', kind: 'select', options: ['0', '1'] },
    { name: 'recordSeparator', label: 'Record Separator', kind: 'text' },
    { name: 'fieldSeparator', label: 'Field Separator', kind: 'text' },
    { name: 'RefMsgTypeJslt', label: 'Ref Msg Type JSLT', kind: 'textarea' },
    { name: 'numberOfFields', label: 'Number Of Fields', kind: 'number' },
    { name: 'commentDesc', label: 'Comment Desc', kind: 'text' },
  ],
};
