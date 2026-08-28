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

/**
 * A `select` choice. The Vue templates often showed text differing from the stored value —
 * `<option value="FETCH_ONLY">FETCH ONLY</option>`, or `0`/`1` shown as No/Yes — so a choice is
 * either a bare value (when the two agree) or an explicit pair.
 */
export type FieldOption = string | { readonly value: string; readonly label: string };

export interface ModuleField {
  /** Property name on the business object, without the engine prefix. */
  readonly name: string;
  readonly label: string;
  readonly kind: FieldKind;
  /** Allowed values, for `select` only. Taken from the Vue component's <option> list. */
  readonly options?: readonly FieldOption[];
}

export interface ModuleSchema {
  /** The moddle element type the palette creates, e.g. `HttpReceiver:HttpReceiver`. */
  readonly type: string;
  /** Group heading in the properties panel. */
  readonly label: string;
  readonly fields: readonly ModuleField[];
}

/** Every module offers the same agreement modes; the Vue template showed FETCH_ONLY spaced. */
export const AGREEMENT_MODE: readonly FieldOption[] = ['RUNNING', { value: 'FETCH_ONLY', label: 'FETCH ONLY' }, 'DRAFT'];

/**
 * `HttpReceiver` is the reference implementation; the remaining modules follow the same shape.
 * Field order matches the Vue panel so the form reads the same way it always has.
 */
export const httpReceiverSchema: ModuleSchema = {
  type: 'HttpReceiver:HttpReceiver',
  label: 'HttpReceiver',
  fields: [
    { name: 'agreementMode', label: 'Agreement Mode', kind: 'select', options: AGREEMENT_MODE },
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

/**
 * `HttpTransmitter` is the largest module at 34 fields. Order follows the `renderComponents.push`
 * list in the Vue panel's `HttpTransmitter:HttpTransmitter` case. Four of those entries were
 * commented out there — `retryCountNumber`, `authUserName`, `authPassword`, `authType` — but their
 * components and getters/setters exist and the properties are real, so they are kept here at the
 * positions the comments held, the same way `httpReceiverSchema` keeps fields its panel case
 * omitted.
 */
export const httpTransmitterSchema: ModuleSchema = {
  type: 'HttpTransmitter:HttpTransmitter',
  label: 'HttpTransmitter',
  fields: [
    { name: 'agreementMode', label: 'Agreement Mode', kind: 'select', options: AGREEMENT_MODE },
    { name: 'partyUrl', label: 'Party Url', kind: 'text' },
    { name: 'partyUrlJslt', label: 'Party Url JSLT', kind: 'textarea' },
    { name: 'httpMethod', label: 'Http Method', kind: 'select', options: ['GET', 'POST', 'DELETE', 'PUT'] },
    // Capitalised in the Vue getter — `${prefix}:ContentType`, alone among this module's
    // properties. Lower-casing it here would read and write an attribute no diagram has.
    { name: 'ContentType', label: 'Content Type', kind: 'text' },
    { name: 'partyTimeOut', label: 'Party Time Out (ms)', kind: 'number' },
    { name: 'httpHeaders', label: 'Http Headers', kind: 'textarea' },
    { name: 'httpBody', label: 'Http Body', kind: 'textarea' },
    { name: 'queryParams', label: 'Query Params', kind: 'textarea' },
    { name: 'authUrl', label: 'Auth Url', kind: 'text' },
    { name: 'authHeaders', label: 'Auth Headers', kind: 'textarea' },
    { name: 'authBody', label: 'Auth Body', kind: 'textarea' },
    { name: 'authKey', label: 'Auth Key', kind: 'textarea' },
    { name: 'authResponseDetector', label: 'Auth Response Detector', kind: 'textarea' },
    { name: 'successResponseCodes', label: 'Success Response Codes', kind: 'text' },
    { name: 'successResponsePattern', label: 'Success Response Pattern', kind: 'text' },
    { name: 'retryResponseCodes', label: 'Retry Response Codes', kind: 'text' },
    { name: 'retryResponsePattern', label: 'Retry Response Pattern', kind: 'text' },
    { name: 'retryCountNumber', label: 'Retry Count Number', kind: 'number' },
    { name: 'responseMergePolicy', label: 'Response Merge Policy', kind: 'select', options: ['REQ', 'RES', 'BOTH'] },
    { name: 'authUserName', label: 'Auth Username', kind: 'text' },
    { name: 'authPassword', label: 'Auth Password', kind: 'text' },
    { name: 'authType', label: 'Auth Type', kind: 'text' },
    // Stored as the strings '0' and '1'; the Vue select showed them as No and Yes.
    {
      name: 'isCacheAble',
      label: 'Is Cache Able',
      kind: 'select',
      options: [
        { value: '0', label: 'No' },
        { value: '1', label: 'Yes' },
      ],
    },
    { name: 'cacheExpiryTime', label: 'Cache Expiry Time (ms)', kind: 'number' },
    { name: 'cacheKey', label: 'Cache Key', kind: 'textarea' },
    {
      name: 'backoffType',
      label: 'Back Off Type',
      kind: 'select',
      options: [
        { value: 'FIXED', label: 'Fixed' },
        { value: 'EXPONENTIAL', label: 'Exponential' },
      ],
    },
    { name: 'fixedDelay', label: 'Fixed Delay (ms)', kind: 'number' },
    { name: 'baseDelay', label: 'Base Delay (ms)', kind: 'number' },
    { name: 'multiplier', label: 'Multiplier (ms)', kind: 'number' },
    { name: 'maxDelay', label: 'Max Delay (ms)', kind: 'number' },
    { name: 'maxAttempts', label: 'Max Attempts', kind: 'number' },
    { name: 'overAllTimeOut', label: 'OverAll Time Out (ms)', kind: 'number' },
    { name: 'commentDesc', label: 'Comment Desc', kind: 'text' },
  ],
};

export const moduleSchemas: readonly ModuleSchema[] = [httpReceiverSchema, httpTransmitterSchema];

/** The schema for an element's moddle type, or undefined when it is not a custom module. */
export function schemaForType(type: string | undefined): ModuleSchema | undefined {
  return type ? moduleSchemas.find(schema => schema.type === type) : undefined;
}
