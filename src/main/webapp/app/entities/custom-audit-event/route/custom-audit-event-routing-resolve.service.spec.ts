import { vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRouteSnapshot, ActivatedRoute, Router, convertToParamMap } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { ICustomAuditEvent } from '../custom-audit-event.model';
import { CustomAuditEventService } from '../service/custom-audit-event.service';

import { CustomAuditEventRoutingResolveService } from './custom-audit-event-routing-resolve.service';

describe('CustomAuditEvent routing resolve service', () => {
  let mockRouter: Router;
  let mockActivatedRouteSnapshot: ActivatedRouteSnapshot;
  let routingResolveService: CustomAuditEventRoutingResolveService;
  let service: CustomAuditEventService;
  let resultCustomAuditEvent: ICustomAuditEvent | null | undefined;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([])],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({}),
            },
          },
        },
      ],
    });
    mockRouter = TestBed.inject(Router);
    jest.spyOn(mockRouter, 'navigate').mockImplementation(() => Promise.resolve(true));
    mockActivatedRouteSnapshot = TestBed.inject(ActivatedRoute).snapshot;
    routingResolveService = TestBed.inject(CustomAuditEventRoutingResolveService);
    service = TestBed.inject(CustomAuditEventService);
    resultCustomAuditEvent = undefined;
  });

  describe('resolve', () => {
    it('should return ICustomAuditEvent returned by find', () => {
      // GIVEN
      service.find = vi.fn(id => of(new HttpResponse({ body: { id } })));
      mockActivatedRouteSnapshot.params = { id: 123 };

      // WHEN
      routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
        resultCustomAuditEvent = result;
      });

      // THEN
      expect(service.find).toBeCalledWith(123);
      expect(resultCustomAuditEvent).toEqual({ id: 123 });
    });

    it('should return null if id is not provided', () => {
      // GIVEN
      service.find = vi.fn();
      mockActivatedRouteSnapshot.params = {};

      // WHEN
      routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
        resultCustomAuditEvent = result;
      });

      // THEN
      expect(service.find).not.toBeCalled();
      expect(resultCustomAuditEvent).toEqual(null);
    });

    it('should route to 404 page if data not found in server', () => {
      // GIVEN
      jest.spyOn(service, 'find').mockReturnValue(of(new HttpResponse<ICustomAuditEvent>({ body: null })));
      mockActivatedRouteSnapshot.params = { id: 123 };

      // WHEN
      routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
        resultCustomAuditEvent = result;
      });

      // THEN
      expect(service.find).toBeCalledWith(123);
      expect(resultCustomAuditEvent).toEqual(undefined);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['404']);
    });
  });
});
