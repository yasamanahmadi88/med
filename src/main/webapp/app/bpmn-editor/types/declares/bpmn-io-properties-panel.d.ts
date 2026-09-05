// @bpmn-io/properties-panel ships no type declarations. Only the entry components the module
// properties group renders are declared. They are Preact components constructed by the panel
// itself, so the opaque type is deliberate — nothing here calls them directly.
declare module '@bpmn-io/properties-panel' {
  /** Single-line text input. */
  export const TextFieldEntry: unknown;

  /** Multi-line text input, used for the JSLT/XSLT and validator fields. */
  export const TextAreaEntry: unknown;

  /** Dropdown; the entry supplies its choices through a `getOptions` callback. */
  export const SelectEntry: unknown;

  /** Numeric input. */
  export const NumberFieldEntry: unknown;

  /** Boolean input. */
  export const CheckboxEntry: unknown;
}
