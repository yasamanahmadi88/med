import { ChangeDetectorRef, Component, ViewChild, OnInit, AfterViewInit, ElementRef } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

import { LoginService } from 'app/login/login.service';
import { AccountService } from 'app/core/auth/account.service';
import { LocalStorageService } from 'ngx-webstorage';
import { Login } from './login.model';
import { StateStorageService } from 'app/core/auth/state-storage.service';

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
    private http: HttpClient,
    private stateStorageService: StateStorageService,
    private changeDetector: ChangeDetectorRef,
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
        this.stopLoading();
      },
      error: () => {
        this.captchaId = '';
        this.captchaImageUrl = '';
        this.captchaLoadError = true;
        this.authenticationError = true;
        this.stopLoading();
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
    const redirectUrl = this.stateStorageService.getUrl();

    this.authenticationError = false;
    this.captchaLoadError = false;
    this.isLoading = true;

    const credentials: Login = new Login(
      formValue.username,
      formValue.password,
      formValue.rememberMe,
      this.captchaId,
      formValue.userCaptchaInput,
    );

    this.loginService.login(credentials).subscribe({
      next: () => {
        this.authenticationError = false;
        this.stopLoading();
        if (!redirectUrl) {
          void this.router.navigate(['']);
        }
      },
      error: () => {
        this.authenticationError = true;
        this.stopLoading();

        // Captcha is one-time. Reload it after every failed login attempt.
        setTimeout(() => this.loadCaptcha());
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

  private stopLoading(): void {
    this.isLoading = false;
    this.changeDetector.detectChanges();
  }
}
