import { Component, ViewChild, OnInit, AfterViewInit, ElementRef } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

import { LoginService } from 'app/login/login.service';
import { AccountService } from 'app/core/auth/account.service';
import { LocalStorageService } from 'ngx-webstorage';

@Component({
  selector: 'jhi-login',
  templateUrl: './login.component.html',
})
export class LoginComponent implements OnInit, AfterViewInit {
  @ViewChild('username', { static: false }) username!: ElementRef;

  backUrl = '';
  authenticationError = false;
  captchaError = false;
  captchaId: string = '';
  captchaImageUrl: string = '';
  isLoading = false;

  loginForm = new FormGroup({
    username: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    rememberMe: new FormControl(false, { nonNullable: true, validators: [Validators.required] }),
    userCaptchaInput: new FormControl('', { validators: [Validators.required] }),
    userEnteredCaptchaCode: new FormControl(),
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
        this.router.navigate(['']);
      }
    });
  }

  ngAfterViewInit(): void {
    this.username.nativeElement.focus();
  }

  loadCaptcha(): void {
    this.isLoading = true;
    const captchaEndpoint = this.backUrl + '/api/captcha-endpoint';
    this.http.post<any>(captchaEndpoint, {}).subscribe({
      next: (response) => {
        this.captchaId = response.captchaId;
        this.captchaImageUrl = this.backUrl + response.captchaImageUrl;
        this.captchaError = false;
        this.isLoading = false;
        // Clear the captcha input when loading new captcha
        this.loginForm.patchValue({ userCaptchaInput: '' });
      },
      error: () => {
        this.captchaError = true;
        this.isLoading = false;
      }
    });
  }

  login(): void {
    if (this.loginForm.invalid || this.isLoading) {
      return;
    }

    const formValue = this.loginForm.getRawValue();

    this.authenticationError = false;
    this.captchaError = false;
    this.isLoading = true;

    const credentials = {
      username: formValue.username,
      password: formValue.password,
      rememberMe: formValue.rememberMe,
      captchaToken: formValue.userCaptchaInput || ''
    };

    this.loginService.login(credentials).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['']);
      },
      error: error => {
        this.isLoading = false;

        if (error.status === 400 && error.error?.message?.toLowerCase().includes('captcha')) {
          this.captchaError = true;
          this.loadCaptcha(); // حتماً کپچای جدید
        } else {
          this.authenticationError = true;
        }
      },
    });
  }


  // login(): void {
  //   if (this.loginForm.valid && !this.isLoading) {
  //     const formValue = this.loginForm.getRawValue();
  //
  //     // Clear previous errors
  //     this.authenticationError = false;
  //     this.captchaError = false;
  //     this.isLoading = true;
  //
  //     // First validate the captcha
  //     this.validateCaptcha(formValue.userCaptchaInput || '').then(isValid => {
  //       if (isValid) {
  //         // Create login credentials with captcha token
  //         const credentials = {
  //           username: formValue.username,
  //           password: formValue.password,
  //           rememberMe: formValue.rememberMe,
  //           captchaToken: formValue.userCaptchaInput || ''
  //         };
  //
  //         this.loginService.login(credentials).subscribe({
  //           next: () => {
  //             this.authenticationError = false;
  //             this.captchaError = false;
  //             this.isLoading = false;
  //             if (!this.router.getCurrentNavigation()) {
  //               this.router.navigate(['']);
  //             }
  //           },
  //           error: (error) => {
  //             this.isLoading = false;
  //             this.authenticationError = true;
  //             // Check if it's a captcha error
  //             if (error.status === 400 && error.error?.message?.includes('captcha')) {
  //               this.captchaError = true;
  //               this.showCaptchaError('Captcha verification failed. Please try again.');
  //               this.loadCaptcha(); // Reload captcha on error
  //             } else {
  //               this.showAuthenticationError('Invalid username or password. Please check your credentials.');
  //             }
  //           },
  //         });
  //       } else {
  //         this.isLoading = false;
  //         this.captchaError = true;
  //         this.showCaptchaError('Incorrect captcha code. Please try again.');
  //         this.loadCaptcha(); // Reload captcha on validation failure
  //       }
  //     });
  //   }
  // }

  validateCaptcha(userInput: string): Promise<boolean> {
    return new Promise((resolve) => {
      const validateEndpoint = this.backUrl + '/api/captcha-validate';
      this.http.post<any>(validateEndpoint, {
        captchaId: this.captchaId,
        userInput: userInput || ''
      }).subscribe({
        next: (response) => {
          resolve(response.valid);
        },
        error: () => {
          resolve(false);
        }
      });
    });
  }

  showCaptchaError(message: string): void {
    this.captchaError = true;
    // You could add a toast notification here if you have a notification service
    console.log('Captcha Error:', message);
  }

  showAuthenticationError(message: string): void {
    this.authenticationError = true;
    // You could add a toast notification here if you have a notification service
    console.log('Authentication Error:', message);
  }

  loginWithEnterKey(e: KeyboardEvent): void {
    if (e.key === 'Enter') {
      this.login();
    }
  }

  // Method to reload captcha
  reloadCaptcha(): void {
    if (!this.isLoading) {
      this.captchaError = false;
      this.loadCaptcha();
    }
  }
}
