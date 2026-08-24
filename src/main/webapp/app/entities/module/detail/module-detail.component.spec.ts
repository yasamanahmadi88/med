import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { ModuleDetailComponent } from './module-detail.component';

describe('Module Management Detail Component', () => {
  let comp: ModuleDetailComponent;
  let fixture: ComponentFixture<ModuleDetailComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ModuleDetailComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { data: of({ module: { id: 123 } }) },
        },
      ],
    })
      .overrideTemplate(ModuleDetailComponent, '')
      .compileComponents();
    fixture = TestBed.createComponent(ModuleDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load module on init', () => {
      // WHEN
      comp.ngOnInit();

      // THEN
      expect(comp.module).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
