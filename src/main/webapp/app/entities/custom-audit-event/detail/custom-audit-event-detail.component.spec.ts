import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { CustomAuditEventDetailComponent } from './custom-audit-event-detail.component';

describe('CustomAuditEvent Management Detail Component', () => {
  let comp: CustomAuditEventDetailComponent;
  let fixture: ComponentFixture<CustomAuditEventDetailComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [CustomAuditEventDetailComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { data: of({ customAuditEvent: { id: 123 } }) },
        },
      ],
    })
      .overrideTemplate(CustomAuditEventDetailComponent, '')
      .compileComponents();
    fixture = TestBed.createComponent(CustomAuditEventDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load customAuditEvent on init', () => {
      // WHEN
      comp.ngOnInit();

      // THEN
      expect(comp.customAuditEvent).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
