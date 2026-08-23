import {Component, OnInit} from '@angular/core';
import {FormGroup, FormControl, Validators} from '@angular/forms';
import {Observable} from 'rxjs';

import {AccountService} from 'app/core/auth/account.service';
import {Account} from 'app/core/auth/account.model';
import {PasswordService} from './password.service';

@Component({
  selector: 'jhi-password',
  templateUrl: './password.component.html',
})
export class PasswordComponent implements OnInit {
  doNotMatch = false;
  error = false;
  success = false;
  account$?: Observable<Account | null>;
  passwordForm = new FormGroup({
    currentPassword: new FormControl('', {nonNullable: true, validators: Validators.required}),
    newPassword: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.pattern('^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,64}$')
      ],
    }),
    confirmPassword: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.required
      ],
    }),
  });

  constructor(private passwordService: PasswordService, private accountService: AccountService) {
  }

  ngOnInit(): void {
    this.account$ = this.accountService.identity();
    this.passwordForm.get('newPassword')!.valueChanges.subscribe((value) => {
      if (value) {
        this.passwordService.validatePassword(value).subscribe((res) => {
          const control = this.passwordForm.get('newPassword');
          if (!res.valid && res.validationException) {
            control?.setErrors({validationException: res.validationException});
          } else {
            control?.setErrors(null);
          }
        });
      } else {
        this.passwordForm.get('newPassword')?.setErrors({title: 'emptyPassword'});
      }
    });
  }

  changePassword(): void {
    this.error = false;
    this.success = false;
    this.doNotMatch = false;

    const {newPassword, confirmPassword, currentPassword} = this.passwordForm.getRawValue();
    if (newPassword !== confirmPassword) {
      this.doNotMatch = true;
    } else {
      this.passwordService.save(newPassword, currentPassword).subscribe({
        next: () => {
          this.success = true;
          this.passwordForm.reset();
        },
        error: (err) => {
          const backendTitle = err.error?.title;

          if (backendTitle) {
            this.passwordForm.get('newPassword')?.setErrors({
              title: backendTitle
            });
          } else {
            this.error = true;
          }
        }

      });
    }
  }

  get firstNewPasswordError(): string | null {
    const control = this.passwordForm.get('newPassword');
    if (!control || !control.errors) return null;

    const errorOrder = [
      'emptyPassword',
      'passwordSmallerThanMinLength',
      'passwordGreaterThanMaxLength',
      'passwordMustContainAtLeastOneUppercaseLetter',
      'passwordMustContainAtLeastOneLowercaseLetter',
      'passwordMustContainAtLeastOneDigit',
      'passwordMustContainAtLeastOneSpecialCharacter',
      'passwordIsTooCommonAndInsecure'
    ];

    for (const key of errorOrder) {
      if (control.errors['validationException'] === key) return key;
    }

    return null;
  }

}
