import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { ResourceDetailComponent } from './resource-detail.component';

describe('Resource Management Detail Component', () => {
  let comp: ResourceDetailComponent;
  let fixture: ComponentFixture<ResourceDetailComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ResourceDetailComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { data: of({ resource: { id: 123 } }) },
        },
      ],
    })
      .overrideTemplate(ResourceDetailComponent, '')
      .compileComponents();
    fixture = TestBed.createComponent(ResourceDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load resource on init', () => {
      // WHEN
      comp.ngOnInit();

      // THEN
      expect(comp.resource).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
