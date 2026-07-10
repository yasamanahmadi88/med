import { Component, ViewChild, OnInit, AfterViewInit, ElementRef } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

import { LoginService } from 'app/login/login.service';
import { AccountService } from 'app/core/auth/account.service';
import { LocalStorageService } from 'ngx-webstorage';
import { Login } from './login.model';

@Component({
  selector: 'jhi-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  standalone: false,
})
export class LoginComponent implements OnInit, AfterViewInit {
  @ViewChild('username', { static: false }) username!: ElementRef;

  backUrl = '';
  authenticationError = false;
  captchaLoadError = false;
  captchaId = '';
  captchaImageUrl = '';
  isLoading = false;

  loginForm = new FormGroup({
    username: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    rememberMe: new FormControl(false, { nonNullable: true }),
    userCaptchaInput: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  constructor(
    private localStorageService: LocalStorageService,
    private accountService: AccountService,
    private loginService: LoginService,
    private router: Router,
    private http: HttpClient
  ) {
    this.backUrl = this.localStorageService.retrieve('backendUrl') || '';
  }

  ngOnInit(): void {
    this.loadCaptcha();

    this.accountService.identity().subscribe(() => {
      if (this.accountService.isAuthenticated()) {
        void this.router.navigate(['']);
      }
    });
  }

  ngAfterViewInit(): void {
    this.username.nativeElement.focus();
  }

  loadCaptcha(): void {
    this.isLoading = true;
    this.captchaLoadError = false;

    this.http.post<{ captchaId: string; captchaImageUrl: string }>(this.backUrl + '/api/captcha-endpoint', {}).subscribe({
      next: response => {
        this.captchaId = response.captchaId;
        this.captchaImageUrl = this.backUrl + response.captchaImageUrl;
        this.loginForm.patchValue({ userCaptchaInput: '' });
        this.isLoading = false;
      },
      error: () => {
        this.captchaId = '';
        this.captchaImageUrl = '';
        this.captchaLoadError = true;
        this.authenticationError = true;
        this.isLoading = false;
      },
    });
  }

  login(): void {
    if (this.loginForm.invalid || this.isLoading) {
      this.loginForm.markAllAsTouched();
      return;
    }

    if (!this.captchaId) {
      this.authenticationError = true;
      this.loadCaptcha();
      return;
    }

    const formValue = this.loginForm.getRawValue();

    this.authenticationError = false;
    this.captchaLoadError = false;
    this.isLoading = true;

    const credentials: Login = new Login(
      formValue.username,
      formValue.password,
      formValue.rememberMe,
      this.captchaId,
      formValue.userCaptchaInput
    );

    this.loginService.login(credentials).subscribe({
      next: () => {
        this.authenticationError = false;
        this.isLoading = false;
        void this.router.navigate(['']);
      },
      error: () => {
        this.authenticationError = true;
        this.isLoading = false;

        // Captcha is one-time. Reload it after every failed login attempt.
        this.loadCaptcha();
      },
    });
  }

  loginWithEnterKey(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      this.login();
    }
  }

  reloadCaptcha(): void {
    if (!this.isLoading) {
      this.authenticationError = false;
      this.captchaLoadError = false;
      this.loadCaptcha();
    }
  }
}
