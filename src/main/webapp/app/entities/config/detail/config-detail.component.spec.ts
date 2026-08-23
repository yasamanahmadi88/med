import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { ConfigDetailComponent } from './config-detail.component';

describe('Config Management Detail Component', () => {
  let comp: ConfigDetailComponent;
  let fixture: ComponentFixture<ConfigDetailComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ConfigDetailComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { data: of({ config: { id: 123 } }) },
        },
      ],
    })
      .overrideTemplate(ConfigDetailComponent, '')
      .compileComponents();
    fixture = TestBed.createComponent(ConfigDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load config on init', () => {
      // WHEN
      comp.ngOnInit();

      // THEN
      expect(comp.config).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
