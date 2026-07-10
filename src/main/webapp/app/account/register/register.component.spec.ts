import { vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import { EMAIL_ALREADY_USED_TYPE, LOGIN_ALREADY_USED_TYPE } from 'app/config/error.constants';

import { RegisterService } from './register.service';
import { RegisterComponent } from './register.component';

describe('RegisterComponent', () => {
  let fixture: ComponentFixture<RegisterComponent>;
  let comp: RegisterComponent;
  let service: RegisterService;
  let mockTranslateService: TranslateService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot(), HttpClientTestingModule],
      declarations: [RegisterComponent],
      providers: [FormBuilder],
    })
      .overrideTemplate(RegisterComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RegisterComponent);
    comp = fixture.componentInstance;
    service = TestBed.inject(RegisterService);
    mockTranslateService = TestBed.inject(TranslateService);
  });

  it('should ensure the two passwords entered match', () => {
    comp.registerForm.patchValue({
      password: 'password',
      confirmPassword: 'non-matching',
    });

    comp.register();

    expect(comp.doNotMatch).toBe(true);
  });

  it('should update success to true after creating an account', () => {
    vi.spyOn(service, 'save').mockReturnValue(of({}));
    Object.defineProperty(mockTranslateService, 'currentLang', { configurable: true, get: () => 'en' });
    comp.registerForm.patchValue({
      password: 'password',
      confirmPassword: 'password',
    });

    comp.register();

    expect(service.save).toHaveBeenCalledWith({
      email: '',
      password: 'password',
      login: '',
      langKey: 'en',
    });
    expect(comp.success).toBe(true);
    expect(comp.error).toBe(false);
  });

  it('should notify of user existence upon 400/login already in use', () => {
    vi.spyOn(service, 'save').mockReturnValue(
      throwError(() => ({
        status: 400,
        error: { type: LOGIN_ALREADY_USED_TYPE },
      }))
    );
    comp.registerForm.patchValue({
      password: 'password',
      confirmPassword: 'password',
    });

    comp.register();

    expect(comp.error).toBe(true);
    expect(comp.success).toBe(false);
  });

  it('should notify of email existence upon 400/email address already in use', () => {
    vi.spyOn(service, 'save').mockReturnValue(
      throwError(() => ({
        status: 400,
        error: { type: EMAIL_ALREADY_USED_TYPE },
      }))
    );
    comp.registerForm.patchValue({
      password: 'password',
      confirmPassword: 'password',
    });

    comp.register();

    expect(comp.error).toBe(true);
    expect(comp.success).toBe(false);
  });

  it('should notify of generic error', () => {
    vi.spyOn(service, 'save').mockReturnValue(
      throwError(() => ({
        status: 503,
      }))
    );
    comp.registerForm.patchValue({
      password: 'password',
      confirmPassword: 'password',
    });

    comp.register();

    expect(comp.error).toBe(true);
  });
});
