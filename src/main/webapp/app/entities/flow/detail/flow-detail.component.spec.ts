import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { FlowDetailComponent } from './flow-detail.component';

describe('Flow Management Detail Component', () => {
  let comp: FlowDetailComponent;
  let fixture: ComponentFixture<FlowDetailComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [FlowDetailComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { data: of({ flow: { id: 123 } }) },
        },
      ],
    })
      .overrideTemplate(FlowDetailComponent, '')
      .compileComponents();
    fixture = TestBed.createComponent(FlowDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load flow on init', () => {
      // WHEN
      comp.ngOnInit();

      // THEN
      expect(comp.flow).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
