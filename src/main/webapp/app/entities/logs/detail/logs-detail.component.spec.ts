import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { LogsDetailComponent } from './logs-detail.component';

describe('Product Management Detail Component', () => {
  let comp: LogsDetailComponent;
  let fixture: ComponentFixture<LogsDetailComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [LogsDetailComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { data: of({ product: { id: 123 } }) },
        },
      ],
    })
      .overrideTemplate(LogsDetailComponent, '')
      .compileComponents();
    fixture = TestBed.createComponent(LogsDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load product on init', () => {
      // WHEN
      comp.ngOnInit();

      // THEN
      expect(comp.product).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
