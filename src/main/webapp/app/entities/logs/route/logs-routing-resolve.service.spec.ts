import { vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRouteSnapshot, ActivatedRoute, Router, convertToParamMap } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { IReportLogs } from '../logs.model';
import { LogReportService } from '../service/logs.service';
import { LogsRoutingResolveService } from './logs-routing-resolve.service';

describe('Logs routing resolve service', () => {
  let mockRouter: Router;
  let mockActivatedRouteSnapshot: ActivatedRouteSnapshot;
  let routingResolveService: LogsRoutingResolveService;
  let service: LogReportService;
  let resultLog: IReportLogs | null | undefined;

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
    vi.spyOn(mockRouter, 'navigate').mockImplementation(() => Promise.resolve(true));
    mockActivatedRouteSnapshot = TestBed.inject(ActivatedRoute).snapshot;
    routingResolveService = TestBed.inject(LogsRoutingResolveService);
    service = TestBed.inject(LogReportService);
    resultLog = undefined;
  });

  describe('resolve', () => {
    it('should return IReportLogs returned by find', () => {
      service.find = vi.fn(id => of(new HttpResponse({ body: { id } as IReportLogs })));
      mockActivatedRouteSnapshot.params = { id: 123 };

      routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
        resultLog = result;
      });

      expect(service.find).toBeCalledWith(123);
      expect(resultLog).toEqual(expect.objectContaining({ id: 123 }));
    });

    it('should return null if id is not provided', () => {
      service.find = vi.fn();
      mockActivatedRouteSnapshot.params = {};

      routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
        resultLog = result;
      });

      expect(service.find).not.toBeCalled();
      expect(resultLog).toEqual(null);
    });

    it('should route to 404 page if data not found in server', () => {
      vi.spyOn(service, 'find').mockReturnValue(of(new HttpResponse<IReportLogs>({ body: null })));
      mockActivatedRouteSnapshot.params = { id: 123 };

      routingResolveService.resolve(mockActivatedRouteSnapshot).subscribe(result => {
        resultLog = result;
      });

      expect(service.find).toBeCalledWith(123);
      expect(resultLog).toEqual(undefined);
      expect(mockRouter.navigate).toHaveBeenCalledWith(['404']);
    });
  });
});
