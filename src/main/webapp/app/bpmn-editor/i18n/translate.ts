import { messages, Lang } from './index';

type TranslationTable = Record<string, string>;

/** The signature diagram-js calls: a template, plus the values for its `{placeholder}`s. */
export type BpmnTranslate = (template: string, replacements?: Record<string, unknown>) => string;

/**
 * The languages whose bundle is actually translated, and so the only ones that may reach
 * diagram-js.
 *
 * This list is the whole reason the Vue `Translate` module was left out of the first pass (see
 * "`Translate` — broken in the original, and the bundle is not English" in the module README).
 * Its lookup key was the literal `'en_Us'`, which matches neither bundle, so it always fell
 * through untranslated — and simply fixing the key would have made things worse, because
 * `i18n/en_US` is largely untranslated Chinese carried over from `zh_CN`. Wiring translation up
 * for `en_US` would turn the palette, context pad and popup menu Chinese.
 *
 * So `en_US` is deliberately absent. It falls through to bpmn-js's own English source labels,
 * which is what users have always seen and what they should keep seeing until that bundle is
 * genuinely translated. Adding a language here is safe only once its bundle is.
 */
const TRANSLATED_LANGS: readonly Lang[] = ['zh_CN'];

/**
 * A bundle is split by area (`elements`, `lint`, `configForm`, `panel`, `toolbar`) but
 * diagram-js's `translate` is one flat lookup, so the areas are merged.
 *
 * They are merged in the bundle's own key order, so a key defined in two areas resolves to the
 * later one — the same collapse the bundle's `index.ts` already performs across the four element
 * files.
 */
function flatten(lang: Lang): TranslationTable {
  const bundle = messages[lang] as Record<string, unknown>;

  return Object.values(bundle).reduce<TranslationTable>((table, area) => {
    // A bundle area is always a flat string table; anything else is a bundle bug, not input.
    return typeof area === 'object' && area !== null ? { ...table, ...(area as TranslationTable) } : table;
  }, {});
}

/**
 * Substitute `{key}` placeholders, leaving a placeholder alone when no replacement was supplied.
 *
 * Leaving it alone matters: diagram-js passes replacements for some labels and not others, and a
 * label whose `{type}` silently became `undefined` reads as a rendering bug rather than a missing
 * argument. `hasOwnProperty` rather than a truthiness check, so a legitimately empty or zero
 * value still substitutes.
 */
function interpolate(template: string, replacements: Record<string, unknown>): string {
  return template.replace(/{([^}]+)}/g, (match: string, key: string) =>
    Object.prototype.hasOwnProperty.call(replacements, key) ? String(replacements[key]) : match,
  );
}

/**
 * The `translate` service for one language.
 *
 * An unknown or untranslated language gives the identity translation — template in, template out,
 * placeholders still substituted — which is exactly bpmn-js's own behaviour without the module.
 */
export function createBpmnTranslate(language?: string): BpmnTranslate {
  const translations = TRANSLATED_LANGS.includes(language as Lang) ? flatten(language as Lang) : {};

  return (template: string, replacements: Record<string, unknown> = {}): string =>
    interpolate(translations[template] ?? template, replacements);
}

/**
 * The didi module that overrides diagram-js's own `translate`.
 *
 * Registered as a `'value'` rather than a `'type'` because the language is known when the modeler
 * is built, not injected into it: `designer.component.ts` rebuilds the modeler when the settings
 * change, so each modeler carries the translate function for the language selected at that
 * moment.
 */
export function translationModuleFor(language?: string): { translate: ['value', BpmnTranslate] } {
  return {
    translate: ['value', createBpmnTranslate(language)],
  };
}
