import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { SharedModule } from 'app/shared/shared.module';
import { LOGIN_ROUTE } from './login.route';
import { LoginComponent } from './login.component';
import { BotDetectCaptchaModule } from 'angular-captcha';

@NgModule({
  imports: [SharedModule, RouterModule.forChild([LOGIN_ROUTE]), BotDetectCaptchaModule],
  declarations: [LoginComponent],
})
export class LoginModule {}
