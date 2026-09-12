import zh_CN from './zh_CN';

type TranslationTable = Record<string, string>;

type TranslationBundle = {
  elements?: TranslationTable;
  lint?: TranslationTable;
  configForm?: TranslationTable;
  panel?: TranslationTable;
  toolbar?: TranslationTable;
};

export type BpmnTranslate = (template: string, replacements?: Record<string, unknown>) => string;

const chineseBundle = zh_CN as unknown as TranslationBundle;

const chineseTranslations: TranslationTable = {
  ...(chineseBundle.elements ?? {}),
  ...(chineseBundle.lint ?? {}),
  ...(chineseBundle.configForm ?? {}),
  ...(chineseBundle.panel ?? {}),
  ...(chineseBundle.toolbar ?? {}),
};

function interpolate(template: string, replacements: Record<string, unknown>): string {
  return template.replace(/{([^}]+)}/g, (match: string, key: string) => {
    if (!Object.prototype.hasOwnProperty.call(replacements, key)) {
      return match;
    }

    return String(replacements[key]);
  });
}

/**
 * Creates the diagram-js/bpmn-js translate service.
 *
 * The legacy en_US bundle is deliberately not consumed because the
 * repository audit showed widespread Chinese content inside it.
 *
 * English and unsupported languages therefore safely use the
 * original bpmn-js source labels.
 */
export function createBpmnTranslate(language?: string): BpmnTranslate {
  const translations = language === 'zh_CN' ? chineseTranslations : {};

  return (template: string, replacements: Record<string, unknown> = {}): string => {
    const translatedTemplate = translations[template] ?? template;

    return interpolate(translatedTemplate, replacements);
  };
}

export function translationModuleFor(language?: string): { translate: ['value', BpmnTranslate] } {
  return {
    translate: ['value', createBpmnTranslate(language)],
  };
}
