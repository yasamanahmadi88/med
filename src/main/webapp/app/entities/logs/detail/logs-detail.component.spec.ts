import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { IReportLogs } from '../logs.model';
import { LogsDetailComponent } from './logs-detail.component';

describe('Logs Management Detail Component', () => {
  let comp: LogsDetailComponent;
  let fixture: ComponentFixture<LogsDetailComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [LogsDetailComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { data: of({ log: { id: 123 } as IReportLogs }) },
        },
      ],
    })
      .overrideTemplate(LogsDetailComponent, '')
      .compileComponents();
    fixture = TestBed.createComponent(LogsDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load log on init', () => {
      comp.ngOnInit();
      expect(comp.log).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
