import { enableProdMode } from '@angular/core';
import { platformBrowser } from '@angular/platform-browser';

import { DEBUG_INFO_ENABLED } from './app/app.constants';
import { AppModule } from './app/app.module';

// disable debug data on prod profile to improve performance
if (!DEBUG_INFO_ENABLED) {
  enableProdMode();
}

platformBrowser()
  .bootstrapModule(AppModule, { preserveWhitespaces: true })
  // eslint-disable-next-line no-console
  .then(() => console.log('Application started'))
  .catch((err: unknown) => console.error(err));
