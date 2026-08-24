import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { VersionDetailComponent } from './version-detail.component';

describe('Version Management Detail Component', () => {
  let comp: VersionDetailComponent;
  let fixture: ComponentFixture<VersionDetailComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [VersionDetailComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { data: of({ version: { id: 123 } }) },
        },
      ],
    })
      .overrideTemplate(VersionDetailComponent, '')
      .compileComponents();
    fixture = TestBed.createComponent(VersionDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load version on init', () => {
      // WHEN
      comp.ngOnInit();

      // THEN
      expect(comp.version).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
