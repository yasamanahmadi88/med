import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { MedAuthorityDetailComponent } from './med-authority-detail.component';

describe('MedAuthority Management Detail Component', () => {
  let comp: MedAuthorityDetailComponent;
  let fixture: ComponentFixture<MedAuthorityDetailComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [MedAuthorityDetailComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { data: of({ medAuthority: { id: 123 } }) },
        },
      ],
    })
      .overrideTemplate(MedAuthorityDetailComponent, '')
      .compileComponents();
    fixture = TestBed.createComponent(MedAuthorityDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load medAuthority on init', () => {
      // WHEN
      comp.ngOnInit();

      // THEN
      expect(comp.medAuthority).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
