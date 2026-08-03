import { vi } from 'vitest';
vi.mock('app/core/auth/account.service');

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { throwError, of } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';

import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';

import { SettingsComponent } from './settings.component';

describe('SettingsComponent', () => {
  let comp: SettingsComponent;
  let fixture: ComponentFixture<SettingsComponent>;
  let mockAccountService: AccountService;
  const account: Account = {
    firstName: 'John',
    lastName: 'Doe',
    activated: true,
    email: 'john.doe@mail.com',
    langKey: 'en',
    login: 'john',
    authorities: [],
    imageUrl: '',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot(), HttpClientTestingModule],
      declarations: [SettingsComponent],
      providers: [
        FormBuilder,
        {
          provide: AccountService,
          useValue: {
            identity: vi.fn(() => of(null)),
            getAuthenticationState: vi.fn(() => of(null)),
            isAuthenticated: vi.fn(() => false),
            authenticate: vi.fn(),
            hasAnyAuthority: vi.fn(() => false),
            save: vi.fn(() => of({})),
          },
        },
      ],
    })
      .overrideTemplate(SettingsComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SettingsComponent);
    comp = fixture.componentInstance;
    mockAccountService = TestBed.inject(AccountService);
    mockAccountService.identity = vi.fn(() => of(account));
    mockAccountService.getAuthenticationState = vi.fn(() => of(account));
  });

  it('should send the current identity upon save', () => {
    // GIVEN
    mockAccountService.save = vi.fn(() => of({}));
    const settingsFormValues = {
      firstName: 'John',
      lastName: 'Doe',
      email: 'john.doe@mail.com',
      langKey: 'en',
    };

    // WHEN
    comp.ngOnInit();
    comp.save();

    // THEN
    expect(mockAccountService.identity).toHaveBeenCalled();
    expect(mockAccountService.save).toHaveBeenCalledWith(account);
    expect(mockAccountService.authenticate).toHaveBeenCalledWith(account);
    expect(comp.settingsForm.value).toMatchObject(expect.objectContaining(settingsFormValues));
  });

  it('should notify of success upon successful save', () => {
    // GIVEN
    mockAccountService.save = vi.fn(() => of({}));

    // WHEN
    comp.ngOnInit();
    comp.save();

    // THEN
    expect(comp.success).toBe(true);
  });

  it('should notify of error upon failed save', () => {
    // GIVEN
    mockAccountService.save = vi.fn(() => throwError(() => 'ERROR'));

    // WHEN
    comp.ngOnInit();
    comp.save();

    // THEN
    expect(comp.success).toBe(false);
  });
});
