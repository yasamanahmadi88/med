import { vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { of } from 'rxjs';

import { UserService } from 'app/entities/user/user.service';
import { PasswordService } from 'app/account/password/password.service';
import { LoginService } from 'app/login/login.service';
import { ChangePasswordDialogComponent } from './change-password-dialog.component';

describe('ChangePasswordDialogComponent', () => {
  let component: ChangePasswordDialogComponent;
  let fixture: ComponentFixture<ChangePasswordDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, HttpClientTestingModule],
      declarations: [ChangePasswordDialogComponent],
      providers: [
        { provide: NgbActiveModal, useValue: { close: vi.fn(), dismiss: vi.fn() } },
        { provide: UserService, useValue: {} },
        { provide: PasswordService, useValue: { save: vi.fn(() => of({})) } },
        { provide: LoginService, useValue: { logout: vi.fn() } },
      ],
    })
      .overrideTemplate(ChangePasswordDialogComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ChangePasswordDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
