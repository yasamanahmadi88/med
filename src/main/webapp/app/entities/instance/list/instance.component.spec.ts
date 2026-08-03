import { vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpHeaders, HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { InstanceService } from '../service/instance.service';

import { InstanceComponent } from './instance.component';

describe('Instance Management Component', () => {
  let comp: InstanceComponent;
  let fixture: ComponentFixture<InstanceComponent>;
  let service: InstanceService;
  let routerNavigateSpy: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule.withRoutes([{ path: 'instance', component: InstanceComponent }]), HttpClientTestingModule],
      declarations: [InstanceComponent],
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
                'filter[someId.in]': 'dc4279ea-cfb9-11ec-9d64-0242ac120002',
              }),
            ),
            snapshot: { queryParams: {} },
          },
        },
      ],
    })
      .overrideTemplate(InstanceComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(InstanceComponent);
    comp = fixture.componentInstance;
    service = TestBed.inject(InstanceService);
    routerNavigateSpy = vi.spyOn(comp.router, 'navigate');

    const headers = new HttpHeaders();
    vi.spyOn(service, 'query').mockReturnValue(
      of(
        new HttpResponse({
          body: [{ id: 123 }],
          headers,
        }),
      ),
    );
  });

  it('Should call load all on init', () => {
    // WHEN
    comp.ngOnInit();

    // THEN
    expect(service.query).toHaveBeenCalled();
    expect(comp.instances?.[0]).toEqual(expect.objectContaining({ id: 123 }));
  });

  describe('trackId', () => {
    it('Should forward to instanceService', () => {
      const entity = { id: 123 };
      vi.spyOn(service, 'getInstanceIdentifier');
      const id = comp.trackId(0, entity);
      expect(service.getInstanceIdentifier).toHaveBeenCalledWith(entity);
      expect(id).toBe(entity.id);
    });
  });

  it('should load a page', () => {
    // WHEN
    comp.navigateToPage(1);

    // THEN
    expect(routerNavigateSpy).toHaveBeenCalled();
  });

  it('should calculate the sort attribute for an id', () => {
    // WHEN
    comp.ngOnInit();

    // THEN
    expect(service.query).toHaveBeenLastCalledWith(expect.objectContaining({ sort: ['id,desc'] }));
  });

  it('should calculate the sort attribute for a non-id attribute', () => {
    // GIVEN
    comp.predicate = 'name';

    // WHEN
    comp.navigateToWithComponentValues();

    // THEN
    expect(routerNavigateSpy).toHaveBeenLastCalledWith(
      expect.anything(),
      expect.objectContaining({
        queryParams: expect.objectContaining({
          sort: ['name,asc'],
        }),
      }),
    );
  });

  it('should calculate the filter attribute', () => {
    // WHEN
    comp.ngOnInit();

    // THEN
    expect(service.query).toHaveBeenLastCalledWith(expect.objectContaining({ 'someId.in': ['dc4279ea-cfb9-11ec-9d64-0242ac120002'] }));
  });
});
