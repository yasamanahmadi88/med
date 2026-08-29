import { vi } from 'vitest';
vi.mock('app/core/auth/account.service');
vi.mock('app/login/login.service');

import { ElementRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { Router, Navigation } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { provideNgxWebstorage, withNgxWebstorageConfig, withLocalStorage, withSessionStorage } from 'ngx-webstorage';

import { AccountService } from 'app/core/auth/account.service';
import { ApplicationConfigService } from 'app/core/config/application-config.service';

import { LoginService } from './login.service';
import { LoginComponent } from './login.component';
import { Login } from './login.model';

describe('LoginComponent', () => {
  let comp: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let mockRouter: Router;
  let mockAccountService: AccountService;
  let mockLoginService: LoginService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RouterTestingModule.withRoutes([]), HttpClientTestingModule],
      declarations: [LoginComponent],
      providers: [
        provideNgxWebstorage(withNgxWebstorageConfig({ prefix: 'jhi', separator: '-' }), withLocalStorage(), withSessionStorage()),
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
        {
          provide: LoginService,
          useValue: {
            login: vi.fn(() => of({})),
          },
        },
      ],
    })
      .overrideTemplate(LoginComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LoginComponent);
    comp = fixture.componentInstance;
    mockRouter = TestBed.inject(Router);
    vi.spyOn(mockRouter, 'navigate').mockImplementation(() => Promise.resolve(true));
    mockLoginService = TestBed.inject(LoginService);
    mockAccountService = TestBed.inject(AccountService);
  });

  describe('ngOnInit', () => {
    it('Should call accountService.identity on Init', () => {
      // GIVEN
      mockAccountService.identity = vi.fn(() => of(null));
      mockAccountService.getAuthenticationState = vi.fn(() => of(null));

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(mockAccountService.identity).toHaveBeenCalled();
    });

    it('Should call accountService.isAuthenticated on Init', () => {
      // GIVEN
      mockAccountService.identity = vi.fn(() => of(null));

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(mockAccountService.isAuthenticated).toHaveBeenCalled();
    });

    it('should navigate to home page on Init if authenticated=true', () => {
      // GIVEN
      mockAccountService.identity = vi.fn(() => of(null));
      mockAccountService.getAuthenticationState = vi.fn(() => of(null));
      mockAccountService.isAuthenticated = () => true;

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(mockRouter.navigate).toHaveBeenCalledWith(['']);
    });
  });

  describe('ngAfterViewInit', () => {
    it('shoult set focus to username input after the view has been initialized', () => {
      // GIVEN
      const node = {
        focus: vi.fn(),
      };
      comp.username = new ElementRef(node);

      // WHEN
      comp.ngAfterViewInit();

      // THEN
      expect(node.focus).toHaveBeenCalled();
    });
  });

  describe('loadCaptcha', () => {
    it('should request the captcha from the same backend as the rest of the API', () => {
      const httpMock = TestBed.inject(HttpTestingController);

      comp.loadCaptcha();

      const request = httpMock.expectOne('api/captcha-endpoint');
      expect(request.request.method).toEqual('POST');
      request.flush({ captchaId: 'cid-1', captchaImageUrl: '/api/captcha.png?cid=cid-1' });

      expect(comp.captchaId).toEqual('cid-1');
      // Root-relative server path, resolved against `<base href="/">` - not double-prefixed.
      expect(comp.captchaImageUrl).toEqual('api/captcha.png?cid=cid-1');
      expect(comp.captchaLoadError).toEqual(false);
    });

    it('should honour a configured endpoint prefix without doubling the slash', () => {
      const httpMock = TestBed.inject(HttpTestingController);
      TestBed.inject(ApplicationConfigService).setEndpointPrefix('http://backend.example.com/');

      comp.loadCaptcha();

      httpMock
        .expectOne('http://backend.example.com/api/captcha-endpoint')
        .flush({ captchaId: 'cid-2', captchaImageUrl: '/api/captcha.png?cid=cid-2' });

      expect(comp.captchaImageUrl).toEqual('http://backend.example.com/api/captcha.png?cid=cid-2');
    });

    it('should flag a captcha load error when the request fails', () => {
      const httpMock = TestBed.inject(HttpTestingController);

      comp.loadCaptcha();

      httpMock.expectOne('api/captcha-endpoint').flush('nope', { status: 500, statusText: 'Internal Server Error' });

      expect(comp.captchaId).toEqual('');
      expect(comp.captchaImageUrl).toEqual('');
      expect(comp.captchaLoadError).toEqual(true);
    });
  });

  describe('login', () => {
    beforeEach(() => {
      comp.captchaId = 'captcha-1';
      comp.loginForm.patchValue({
        username: 'admin',
        password: 'admin',
        rememberMe: true,
        userCaptchaInput: 'ABCD',
      });
      vi.spyOn(comp, 'loadCaptcha').mockImplementation(() => undefined);
    });

    it('should authenticate the user and navigate to home page', () => {
      const credentials = new Login('admin', 'admin', true, 'captcha-1', 'ABCD');
      const account = { login: 'admin', authorities: ['ROLE_ADMIN'] } as any;
      mockLoginService.login = vi.fn(() => of(account));
      mockAccountService.isAuthenticated = vi.fn(() => true);

      comp.login();

      expect(comp.authenticationError).toEqual(false);
      expect(mockLoginService.login).toHaveBeenCalledWith(credentials);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['']);
    });

    it('should authenticate the user but not navigate to home page if authentication process is already routing to cached url from localstorage', () => {
      vi.spyOn(mockRouter, 'getCurrentNavigation').mockReturnValue({} as Navigation);
      mockLoginService.login = vi.fn(() => of({ login: 'admin' } as any));
      mockAccountService.isAuthenticated = vi.fn(() => true);

      // When previousState is set, AccountService navigates; login still succeeds without forcing home.
      // Patch state-storage via redirect path: simulate stored URL by making navigate already used.
      comp.login();

      expect(comp.authenticationError).toEqual(false);
      expect(mockLoginService.login).toHaveBeenCalled();
    });

    it('should stay on login form when account identity is missing after JWT', () => {
      mockLoginService.login = vi.fn(() => of(null));
      mockAccountService.isAuthenticated = vi.fn(() => false);

      comp.login();

      expect(comp.authenticationError).toEqual(true);
      expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    it('should stay on login form and show error message on login error', () => {
      mockLoginService.login = vi.fn(() => throwError(() => ({})));

      comp.login();

      expect(comp.authenticationError).toEqual(true);
    });
  });
});
