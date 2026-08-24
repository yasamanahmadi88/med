import { vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { of } from 'rxjs';

import { IReportLogs } from '../logs.model';
import { LogReportService } from '../service/logs.service';
import { LogsComponent } from './logs.component';

describe('Logs Management Component', () => {
  let comp: LogsComponent;
  let fixture: ComponentFixture<LogsComponent>;
  let service: LogReportService;
  let routerNavigateSpy: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule.withRoutes([{ path: 'reportLogs', component: LogsComponent }]), HttpClientTestingModule],
      declarations: [LogsComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            data: of({
              defaultSort: 'id,asc',
            }),
            queryParamMap: of(
              convertToParamMap({
                page: '1',
                size: '1',
                sort: 'id,desc',
              }),
            ),
            snapshot: { queryParams: {} },
          },
        },
        { provide: NgbModal, useValue: { open: vi.fn() } },
      ],
    })
      .overrideTemplate(LogsComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(LogsComponent);
    comp = fixture.componentInstance;
    service = TestBed.inject(LogReportService);
    routerNavigateSpy = vi.spyOn(comp.router, 'navigate');

    const headers = new HttpHeaders();
    vi.spyOn(service, 'query').mockReturnValue(
      of(
        new HttpResponse({
          body: [{ id: 123 } as IReportLogs],
          headers,
        }),
      ),
    );
  });

  it('Should call load all on init', () => {
    comp.ngOnInit();
    expect(service.query).toHaveBeenCalled();
    expect(comp.logs?.[0]).toEqual(expect.objectContaining({ id: 123 }));
  });

  describe('trackId', () => {
    it('Should forward to logReportService', () => {
      const entity = { id: 123 } as IReportLogs;
      vi.spyOn(service, 'getLogIdentifier');
      const id = comp.trackId(0, entity);
      expect(service.getLogIdentifier).toHaveBeenCalledWith(entity);
      expect(id).toBe(entity.id);
    });
  });

  it('should load a page', () => {
    comp.navigateToPage(1);
    expect(routerNavigateSpy).toHaveBeenCalled();
  });
});
