import { vi } from 'vitest';
vi.mock('app/core/auth/account.service');

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, RouterEvent, NavigationEnd, NavigationStart } from '@angular/router';
import { Title } from '@angular/platform-browser';
import { Subject, of } from 'rxjs';
import { TranslateModule, TranslateService, LangChangeEvent } from '@ngx-translate/core';

import { AccountService } from 'app/core/auth/account.service';
import { FindLanguageFromKeyPipe } from 'app/shared/language/find-language-from-key.pipe';

import { MainComponent } from './main.component';

describe('MainComponent', () => {
  let comp: MainComponent;
  let fixture: ComponentFixture<MainComponent>;
  let titleService: Title;
  let translateService: TranslateService;
  let findLanguageFromKeyPipe: FindLanguageFromKeyPipe;
  let mockAccountService: AccountService;
  const routerEventsSubject = new Subject<RouterEvent>();
  const langChangeSubject = new Subject<LangChangeEvent>();
  const routerState: any = { snapshot: { root: { data: {} } } };
  let currentLangValue = 'en';

  class MockRouter {
    events = routerEventsSubject;
    routerState = routerState;
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      declarations: [MainComponent],
      providers: [
        Title,
        FindLanguageFromKeyPipe,
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
          provide: Router,
          useClass: MockRouter,
        },
      ],
    })
      .overrideTemplate(MainComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MainComponent);
    comp = fixture.componentInstance;
    titleService = TestBed.inject(Title);
    translateService = TestBed.inject(TranslateService);
    findLanguageFromKeyPipe = TestBed.inject(FindLanguageFromKeyPipe);
    mockAccountService = TestBed.inject(AccountService);
    mockAccountService.identity = vi.fn(() => of(null));
    mockAccountService.getAuthenticationState = vi.fn(() => of(null));

    Object.defineProperty(translateService, 'onLangChange', {
      configurable: true,
      get: () => langChangeSubject.asObservable(),
    });
    Object.defineProperty(translateService, 'currentLang', {
      configurable: true,
      get: () => currentLangValue,
    });
  });

  describe('page title', () => {
    const defaultPageTitle = 'global.title';
    const parentRoutePageTitle = 'parentTitle';
    const childRoutePageTitle = 'childTitle';
    const navigationEnd = new NavigationEnd(1, '', '');
    const navigationStart = new NavigationStart(1, '');
    const langChangeEvent: LangChangeEvent = { lang: 'en', translations: {} };

    beforeEach(() => {
      routerState.snapshot.root = { data: {} };
      vi.spyOn(translateService, 'get').mockImplementation((key: string | string[]) => of(`${key as string} translated`) as any);
      currentLangValue = 'en';
      vi.spyOn(titleService, 'setTitle');
      comp.ngOnInit();
    });

    describe('navigation end', () => {
      it('should set page title to default title if pageTitle is missing on routes', () => {
        routerEventsSubject.next(navigationEnd);

        expect(translateService.get).toHaveBeenCalledWith(defaultPageTitle);
        expect(titleService.setTitle).toHaveBeenCalledWith(defaultPageTitle + ' translated');
      });

      it('should set page title to root route pageTitle if there is no child routes', () => {
        routerState.snapshot.root.data = { pageTitle: parentRoutePageTitle };

        routerEventsSubject.next(navigationEnd);

        expect(translateService.get).toHaveBeenCalledWith(parentRoutePageTitle);
        expect(titleService.setTitle).toHaveBeenCalledWith(parentRoutePageTitle + ' translated');
      });

      it('should set page title to child route pageTitle if child routes exist and pageTitle is set for child route', () => {
        routerState.snapshot.root.data = { pageTitle: parentRoutePageTitle };
        routerState.snapshot.root.firstChild = { data: { pageTitle: childRoutePageTitle } };

        routerEventsSubject.next(navigationEnd);

        expect(translateService.get).toHaveBeenCalledWith(childRoutePageTitle);
        expect(titleService.setTitle).toHaveBeenCalledWith(childRoutePageTitle + ' translated');
      });

      it('should set page title to parent route pageTitle if child routes exists but pageTitle is not set for child route data', () => {
        routerState.snapshot.root.data = { pageTitle: parentRoutePageTitle };
        routerState.snapshot.root.firstChild = { data: {} };

        routerEventsSubject.next(navigationEnd);

        expect(translateService.get).toHaveBeenCalledWith(parentRoutePageTitle);
        expect(titleService.setTitle).toHaveBeenCalledWith(parentRoutePageTitle + ' translated');
      });
    });

    describe('navigation start', () => {
      it('should not set page title on navigation start', () => {
        routerEventsSubject.next(navigationStart);

        expect(titleService.setTitle).not.toHaveBeenCalled();
      });
    });

    describe('language change', () => {
      it('should set page title to default title if pageTitle is missing on routes', () => {
        langChangeSubject.next(langChangeEvent);

        expect(translateService.get).toHaveBeenCalledWith(defaultPageTitle);
        expect(titleService.setTitle).toHaveBeenCalledWith(defaultPageTitle + ' translated');
      });

      it('should set page title to root route pageTitle if there is no child routes', () => {
        routerState.snapshot.root.data = { pageTitle: parentRoutePageTitle };

        langChangeSubject.next(langChangeEvent);

        expect(translateService.get).toHaveBeenCalledWith(parentRoutePageTitle);
        expect(titleService.setTitle).toHaveBeenCalledWith(parentRoutePageTitle + ' translated');
      });

      it('should set page title to child route pageTitle if child routes exist and pageTitle is set for child route', () => {
        routerState.snapshot.root.data = { pageTitle: parentRoutePageTitle };
        routerState.snapshot.root.firstChild = { data: { pageTitle: childRoutePageTitle } };

        langChangeSubject.next(langChangeEvent);

        expect(translateService.get).toHaveBeenCalledWith(childRoutePageTitle);
        expect(titleService.setTitle).toHaveBeenCalledWith(childRoutePageTitle + ' translated');
      });

      it('should set page title to parent route pageTitle if child routes exists but pageTitle is not set for child route data', () => {
        routerState.snapshot.root.data = { pageTitle: parentRoutePageTitle };
        routerState.snapshot.root.firstChild = { data: {} };

        langChangeSubject.next(langChangeEvent);

        expect(translateService.get).toHaveBeenCalledWith(parentRoutePageTitle);
        expect(titleService.setTitle).toHaveBeenCalledWith(parentRoutePageTitle + ' translated');
      });
    });
  });

  describe('page language attribute', () => {
    it('should change page language attribute on language change', () => {
      comp.ngOnInit();

      findLanguageFromKeyPipe.isRTL = vi.fn(() => false);
      currentLangValue = 'lang1';
      langChangeSubject.next({ lang: 'lang1', translations: {} });

      expect(document.querySelector('html')?.getAttribute('lang')).toEqual('lang1');
      expect(document.querySelector('html')?.getAttribute('dir')).toEqual('ltr');

      findLanguageFromKeyPipe.isRTL = vi.fn(() => true);
      currentLangValue = 'lang2';
      langChangeSubject.next({ lang: 'lang2', translations: {} });

      expect(document.querySelector('html')?.getAttribute('lang')).toEqual('lang2');
      expect(document.querySelector('html')?.getAttribute('dir')).toEqual('rtl');
    });
  });
});
