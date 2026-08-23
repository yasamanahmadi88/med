import { NgModule } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { TranslateModule, TranslateService, TranslateLoader, MissingTranslationHandler } from '@ngx-translate/core';
import { missingTranslationHandler, translationProviders, translatePartialLoader } from 'app/config/translation.config';
import { SessionStorageService } from 'ngx-webstorage';

@NgModule({
  imports: [
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: translatePartialLoader,
      },
      missingTranslationHandler: {
        provide: MissingTranslationHandler,
        useFactory: missingTranslationHandler,
      },
    }),
  ],
  providers: [translationProviders],
})
export class TranslationModule {
  constructor(
    private translateService: TranslateService,
    sessionStorageService: SessionStorageService,
  ) {
    translateService.setDefaultLang('en');
    const langKey = sessionStorageService.retrieve('locale') ?? 'en';
    translateService.use(langKey);
  }
}
