import zh_CN from '../../i18n/zh_CN'
import en_US from '../../i18n/en_US'

const languages = {
  zh_CN,
  en_US
}

const lang = sessionStorage.getItem('en_Us')

export function customTranslate(template: string, replacements?: Record<string, string>) {
  replacements = replacements || {}

  const translations = languages['en_Us' || lang]

  // Translate
  template = translations?.elements[template] || template

  // Replace
  return template.replace(/{([^}]+)}/g, function (_, key) {
    return replacements![key] || '{' + key + '}'
  })
}

export default {
  translate: ['value', customTranslate]
}
