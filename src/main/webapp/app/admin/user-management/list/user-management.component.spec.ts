import { vi } from 'vitest';
vi.mock('app/core/auth/account.service');

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { UserManagementService } from '../service/user-management.service';
import { User } from '../user-management.model';
import { AccountService } from 'app/core/auth/account.service';

import { UserManagementComponent } from './user-management.component';
import { ToastrService } from 'ngx-toastr';
import { TranslateService } from '@ngx-translate/core';

describe('User Management Component', () => {
  let comp: UserManagementComponent;
  let fixture: ComponentFixture<UserManagementComponent>;
  let service: UserManagementService;
  let mockAccountService: AccountService;
  const data = of({
    defaultSort: 'id,asc',
  });
  const queryParamMap = of(
    convertToParamMap({
      page: '1',
      size: '1',
      sort: 'id,desc',
    }),
  );

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([])],
      declarations: [UserManagementComponent],
      providers: [
        { provide: ActivatedRoute, useValue: { data, queryParamMap } },
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
        { provide: ToastrService, useValue: { success: vi.fn(), error: vi.fn() } },
        { provide: TranslateService, useValue: { instant: vi.fn((k: string) => k) } },
      ],
    })
      .overrideTemplate(UserManagementComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UserManagementComponent);
    comp = fixture.componentInstance;
    service = TestBed.inject(UserManagementService);
    mockAccountService = TestBed.inject(AccountService);
    mockAccountService.identity = vi.fn(() => of(null));
  });

  describe('OnInit', () => {
    it('Should call load all on init', () => {
      // GIVEN
      const headers = new HttpHeaders().append('link', 'link;link');
      vi.spyOn(service, 'query').mockReturnValue(
        of(
          new HttpResponse({
            body: [new User(123)],
            headers,
          }),
        ),
      );

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(service.query).toHaveBeenCalled();
      expect(comp.users?.[0]).toEqual(expect.objectContaining({ id: 123 }));
    });
  });

  describe('setActive', () => {
    it('Should update user and call load all', () => {
      // GIVEN
      const headers = new HttpHeaders().append('link', 'link;link');
      const user = new User(123);
      vi.spyOn(service, 'query').mockReturnValue(
        of(
          new HttpResponse({
            body: [user],
            headers,
          }),
        ),
      );
      vi.spyOn(service, 'update').mockReturnValue(of(user));

      // WHEN
      comp.setActive(user, true);

      // THEN
      expect(service.update).toHaveBeenCalledWith({ ...user, activated: true });
      expect(service.query).toHaveBeenCalled();
      expect(comp.users?.[0]).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
