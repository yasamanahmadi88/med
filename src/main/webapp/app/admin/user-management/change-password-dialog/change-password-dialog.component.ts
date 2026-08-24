import { Component, EventEmitter, OnInit, Output, ViewChild } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { UserService } from 'app/entities/user/user.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { PasswordService } from 'app/account/password/password.service';
import { Account } from 'app/core/auth/account.model';
import { LoginService } from '../../../login/login.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'jhi-change-password-dialog',
  templateUrl: './change-password-dialog.component.html',
  styleUrls: ['./change-password-dialog.component.scss'],
  standalone: false,
})
export class ChangePasswordDialogComponent implements OnInit {
  @Output() result: EventEmitter<any> = new EventEmitter();

  user!: Account;
  doNotMatch?: any;
  error?: any;
  success?: any;
  passwordForm = this.fb.group({
    newPassword: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(50)]],
    confirmPassword: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(50)]],
  });

  constructor(
    private userService: UserService,
    public activeModal: NgbActiveModal,
    private fb: FormBuilder,
    protected loginService: LoginService,
    protected passwordService: PasswordService,
  ) {}

  ngOnInit(): void {
    console.warn('Change Pass');
  }

  clear(): void {
    this.activeModal.dismiss('cancel');
  }

  changePassword(): void {
    const newLocalPass = this.passwordForm.get(['newPassword'])?.value;
    if (newLocalPass !== this.passwordForm.get(['confirmPassword'])?.value) {
      this.error = null;
      this.success = null;
      this.doNotMatch = 'ERROR';
    } else {
      this.doNotMatch = null;
      if (!this.user.login) {
        return;
      }
      this.passwordService.resetPasswordByAdmin(this.user.login, newLocalPass).subscribe(
        () => {
          this.error = null;
          this.success = 'OK';
          this.clear();
          this.result.emit('success');
        },
        () => {
          this.success = null;
          this.error = 'ERROR';
          this.clear();
          this.result.emit('error');
        },
      );
    }
  }
}
