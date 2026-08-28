import { ModuleSchema } from '../schema';

/**
 * `Fragmenter` — splits a batch message into its records. Nothing about the split is configurable;
 * the single agreement mode is the whole form, and `fragmenterProperties/` holds exactly one
 * component and one getter/setter to match.
 *
 * A one-field group still earns its place: without it a placed Fragmenter shows no properties at
 * all and cannot be taken out of DRAFT.
 */
export const fragmenterSchema: ModuleSchema = {
  type: 'Fragmenter:Fragmenter',
  label: 'Fragmenter',
  fields: [{ name: 'agreementMode', label: 'Agreement Mode', kind: 'select', options: ['RUNNING', 'FETCH_ONLY', 'DRAFT'] }],
};
