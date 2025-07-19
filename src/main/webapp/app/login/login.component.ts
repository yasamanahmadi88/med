import { Component, ViewChild, OnInit, AfterViewInit, ElementRef } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { LoginService } from 'app/login/login.service';
import { AccountService } from 'app/core/auth/account.service';
import { CaptchaComponent } from 'angular-captcha';
import { LocalStorageService } from 'ngx-webstorage';

@Component({
  selector: 'jhi-login',
  templateUrl: './login.component.html',
})
export class LoginComponent implements OnInit, AfterViewInit {
  @ViewChild('username', { static: false })
  @ViewChild(CaptchaComponent, { static: true }) captchaComponent?: CaptchaComponent;
  username!: ElementRef;
  backUrl = '';

  authenticationError = false;

  loginForm = new FormGroup({
    username: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    rememberMe: new FormControl(false, { nonNullable: true, validators: [Validators.required] }),
    userCaptchaInput: new FormControl(''),
    userEnteredCaptchaCode: new FormControl(),
    captchaId: new FormControl(),
  });

  constructor(
    private localStorageService: LocalStorageService,
    private accountService: AccountService, private loginService: LoginService, private router: Router) {
    this.backUrl = this.localStorageService.retrieve('backendUrl');
  }

  ngOnInit(): void {
    // if already authenticated then navigate to home page
    if (this.captchaComponent) {
      this.captchaComponent.captchaEndpoint = this.backUrl + '/captcha-endpoint';
    }
    this.accountService.identity().subscribe(() => {
      if (this.accountService.isAuthenticated()) {
        this.router.navigate(['']);
      }
    });
  }

  ngAfterViewInit(): void {
    this.username.nativeElement.focus();
  }

  login(): void {
    this.loginService.login(this.loginForm.getRawValue()).subscribe({
      next: () => {
        this.authenticationError = false;
        if (!this.router.getCurrentNavigation()) {
          // There were no routing during login (eg from navigationToStoredUrl)
          this.router.navigate(['']);
        }
      },
      error: () => (this.authenticationError = true),
    });
  }
}
