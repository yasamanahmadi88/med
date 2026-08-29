import zh_CN from './zh_CN';
import en_US from './en_US';

/**
 * Message bundles for the BPMN editor.
 *
 * Exported as plain objects rather than through an i18n runtime: only `defaultLang` is imported
 * elsewhere, so nothing here needs one.
 */
export const defaultLang = 'en_US';

export const messages = {
  zh_CN,
  en_US,
};

export type Lang = keyof typeof messages;

export default messages;
