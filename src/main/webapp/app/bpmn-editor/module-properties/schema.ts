/**
 * Declarative property schema for the custom integration modules.
 *
 * Ported from the Vue editor, where every field was a hand-written component under
 * `components/Panel/components/<module>Properties/` paired with a getter/setter under
 * `bo-utils/<module>Properties/`. Both said the same three things — which moddle property to
 * read, what to label it, and how to render it — so they collapse into one declaration here and
 * one generic provider that builds the panel from it.
 *
 * The property name is namespaced by the *process engine*, not the module: the Vue getters read
 * `${editor().getProcessEngine}:agreementMode`, so an HttpReceiver stores `camunda:agreementMode`
 * under the default engine. No moddle extension declares these — moddle keeps namespaced
 * attributes it does not know about and writes them back out unchanged, which is how the Vue
 * editor persisted them and how they persist here.
 */

/** How a field is rendered. Mirrors the control the Vue component used. */
export type FieldKind = 'text' | 'textarea' | 'select' | 'number';

export interface ModuleField {
  /** Property name on the business object, without the engine prefix. */
  readonly name: string;
  readonly label: string;
  readonly kind: FieldKind;
  /** Allowed values, for `select` only. Taken from the Vue component's <option> list. */
  readonly options?: readonly string[];
}

export interface ModuleSchema {
  /** The moddle element type the palette creates, e.g. `HttpReceiver:HttpReceiver`. */
  readonly type: string;
  /** Group heading in the properties panel. */
  readonly label: string;
  readonly fields: readonly ModuleField[];
}

/**
 * `HttpReceiver` is the reference implementation; the remaining modules follow the same shape.
 * Field order matches the Vue panel so the form reads the same way it always has.
 */
export const httpReceiverSchema: ModuleSchema = {
  type: 'HttpReceiver:HttpReceiver',
  label: 'HttpReceiver',
  fields: [
    { name: 'agreementMode', label: 'Agreement Mode', kind: 'select', options: ['RUNNING', 'FETCH_ONLY', 'DRAFT'] },
    { name: 'validator', label: 'Validator', kind: 'textarea' },
    { name: 'transformer', label: 'Transformer', kind: 'textarea' },
    { name: 'responseTransformer', label: 'Response Transformer', kind: 'textarea' },
    { name: 'transferType', label: 'Transfer Type', kind: 'select', options: ['JSLT', 'XSLT'] },
    { name: 'asyncMessageTypes', label: 'Async Message Types', kind: 'text' },
    { name: 'authUserName', label: 'Auth Username', kind: 'text' },
    { name: 'authPassword', label: 'Auth Password', kind: 'text' },
    { name: 'authTokenVerificationUrl', label: 'Auth Token Verification Url', kind: 'text' },
    {
      name: 'authTokenVerificationResponseValidator',
      label: 'Auth Token Verification Response Validator',
      kind: 'textarea',
    },
    { name: 'commentDesc', label: 'Comment Desc', kind: 'text' },
  ],
};

export const moduleSchemas: readonly ModuleSchema[] = [httpReceiverSchema];

/** The schema for an element's moddle type, or undefined when it is not a custom module. */
export function schemaForType(type: string | undefined): ModuleSchema | undefined {
  return type ? moduleSchemas.find(schema => schema.type === type) : undefined;
}
