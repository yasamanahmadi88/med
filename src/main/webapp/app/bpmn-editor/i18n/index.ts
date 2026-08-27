import zh_CN from './zh_CN';
import en_US from './en_US';

/**
 * Message bundles for the BPMN editor.
 *
 * The original Vue editor built a vue-i18n instance here. Nothing outside this module used
 * that instance — only `defaultLang` is imported elsewhere — so the bundles are exported
 * directly and the vue-i18n dependency is gone.
 */
export const defaultLang = 'en_US';

export const messages = {
  zh_CN,
  en_US,
};

export type Lang = keyof typeof messages;

export default messages;
