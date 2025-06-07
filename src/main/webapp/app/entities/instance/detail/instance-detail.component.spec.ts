import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { InstanceDetailComponent } from './instance-detail.component';

describe('Instance Management Detail Component', () => {
  let comp: InstanceDetailComponent;
  let fixture: ComponentFixture<InstanceDetailComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [InstanceDetailComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { data: of({ instance: { id: 123 } }) },
        },
      ],
    })
      .overrideTemplate(InstanceDetailComponent, '')
      .compileComponents();
    fixture = TestBed.createComponent(InstanceDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load instance on init', () => {
      // WHEN
      comp.ngOnInit();

      // THEN
      expect(comp.instance).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
