import { HttpClient } from '@angular/common/http';
import { MissingTranslationHandler, MissingTranslationHandlerParams, TranslateLoader } from '@ngx-translate/core';
import { provideTranslateHttpLoader, TranslateHttpLoader } from '@ngx-translate/http-loader';

export const translationNotFoundMessage = 'translation-not-found';

export class MissingTranslationHandlerImpl implements MissingTranslationHandler {
  handle(params: MissingTranslationHandlerParams): string {
    const key = params.key;
    return `${translationNotFoundMessage}[${key}]`;
  }
}

export function translatePartialLoader(): TranslateLoader {
  return new TranslateHttpLoader();
}

export function missingTranslationHandler(): MissingTranslationHandler {
  return new MissingTranslationHandlerImpl();
}

export const translationProviders = provideTranslateHttpLoader({
  prefix: 'i18n/',
  suffix: `.json?_=${typeof I18N_HASH !== 'undefined' ? I18N_HASH : ''}`,
});
