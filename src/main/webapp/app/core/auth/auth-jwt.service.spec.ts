import { vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import {
  provideNgxWebstorage,
  withNgxWebstorageConfig,
  withLocalStorage,
  withSessionStorage,
  LocalStorageService,
  SessionStorageService,
} from 'ngx-webstorage';
import { AuthServerProvider } from 'app/core/auth/auth-jwt.service';

describe('Auth JWT', () => {
  let service: AuthServerProvider;
  let localStorageService: LocalStorageService;
  let sessionStorageService: SessionStorageService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        provideNgxWebstorage(withNgxWebstorageConfig({ prefix: 'jhi', separator: '-' }), withLocalStorage(), withSessionStorage()),
      ],
    });

    httpMock = TestBed.inject(HttpTestingController);
    service = TestBed.inject(AuthServerProvider);
    localStorageService = TestBed.inject(LocalStorageService);
    sessionStorageService = TestBed.inject(SessionStorageService);
  });

  describe('Get Token', () => {
    it('should return empty token if not found in local storage nor session storage', () => {
      const result = service.getToken();
      expect(result).toEqual('');
    });

    it('should return token from session storage if local storage is empty', () => {
      sessionStorageService.retrieve = vi.fn().mockReturnValue('sessionStorageToken');
      const result = service.getToken();
      expect(result).toEqual('sessionStorageToken');
    });

    it('should return token from localstorage storage', () => {
      localStorageService.retrieve = vi.fn().mockReturnValue('localStorageToken');
      const result = service.getToken();
      expect(result).toEqual('localStorageToken');
    });
  });

  describe('Login', () => {
    it('should clear session storage and save in local storage when rememberMe is true', () => {
      localStorageService.clear = vi.fn();
      sessionStorageService.clear = vi.fn();
      localStorageService.store = vi.fn();
      sessionStorageService.store = vi.fn();

      service.login({ username: 'user', password: 'pass', rememberMe: true, captchaId: 'cid', captchaToken: 'ctok' }).subscribe();
      httpMock.expectOne('api/authenticate').flush({ id_token: 'token' });

      expect(localStorageService.store).toHaveBeenCalledWith('authenticationToken', 'token');
      expect(sessionStorageService.clear).toHaveBeenCalledWith('authenticationToken');
    });

    it('should clear local storage and save in session storage when rememberMe is false', () => {
      localStorageService.clear = vi.fn();
      sessionStorageService.clear = vi.fn();
      localStorageService.store = vi.fn();
      sessionStorageService.store = vi.fn();

      service.login({ username: 'user', password: 'pass', rememberMe: false, captchaId: 'cid', captchaToken: 'ctok' }).subscribe();
      httpMock.expectOne('api/authenticate').flush({ id_token: 'token' });

      expect(sessionStorageService.store).toHaveBeenCalledWith('authenticationToken', 'token');
      expect(localStorageService.clear).toHaveBeenCalledWith('authenticationToken');
    });
  });

  describe('Logout', () => {
    it('should clear storage', () => {
      localStorageService.clear = vi.fn();
      sessionStorageService.clear = vi.fn();
      service.logout().subscribe();
      httpMock.expectOne('api/auth/logout').flush({});
      expect(localStorageService.clear).toHaveBeenCalledWith('authenticationToken');
      expect(sessionStorageService.clear).toHaveBeenCalledWith('authenticationToken');
    });

    it('should complete only after the server-side revoke has answered', () => {
      localStorageService.clear = vi.fn();
      sessionStorageService.clear = vi.fn();
      let completed = false;

      service.logout().subscribe({ complete: () => (completed = true) });

      // The request is still in flight: the observable must not have completed yet.
      expect(completed).toBe(false);
      expect(localStorageService.clear).not.toHaveBeenCalled();

      httpMock.expectOne('api/auth/logout').flush({});

      expect(completed).toBe(true);
    });

    it('should clear storage even when the server-side revoke fails', () => {
      localStorageService.clear = vi.fn();
      sessionStorageService.clear = vi.fn();
      let errored = false;

      service.logout().subscribe({ error: () => (errored = true) });
      httpMock.expectOne('api/auth/logout').flush('nope', { status: 500, statusText: 'Internal Server Error' });

      expect(errored).toBe(true);
      expect(localStorageService.clear).toHaveBeenCalledWith('authenticationToken');
      expect(sessionStorageService.clear).toHaveBeenCalledWith('authenticationToken');
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
