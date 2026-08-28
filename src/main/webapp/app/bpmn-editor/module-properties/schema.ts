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
