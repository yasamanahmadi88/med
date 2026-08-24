import { vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, from } from 'rxjs';

import { MedAuthorityFormService } from './med-authority-form.service';
import { MedAuthorityService } from '../service/med-authority.service';
import { IMedAuthority } from '../med-authority.model';

import { MedAuthorityUpdateComponent } from './med-authority-update.component';

describe('MedAuthority Management Update Component', () => {
  let comp: MedAuthorityUpdateComponent;
  let fixture: ComponentFixture<MedAuthorityUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let medAuthorityFormService: MedAuthorityFormService;
  let medAuthorityService: MedAuthorityService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([])],
      declarations: [MedAuthorityUpdateComponent],
      providers: [
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(MedAuthorityUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(MedAuthorityUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    medAuthorityFormService = TestBed.inject(MedAuthorityFormService);
    medAuthorityService = TestBed.inject(MedAuthorityService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should update editForm', () => {
      const medAuthority: IMedAuthority = { id: 456 };

      activatedRoute.data = of({ medAuthority });
      comp.ngOnInit();

      expect(comp.medAuthority).toEqual(medAuthority);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IMedAuthority>>();
      const medAuthority = { id: 123 };
      vi.spyOn(medAuthorityFormService, 'getMedAuthority').mockReturnValue(medAuthority);
      vi.spyOn(medAuthorityService, 'update').mockReturnValue(saveSubject);
      vi.spyOn(comp, 'previousState');
      activatedRoute.data = of({ medAuthority });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: medAuthority }));
      saveSubject.complete();

      // THEN
      expect(medAuthorityFormService.getMedAuthority).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(medAuthorityService.update).toHaveBeenCalledWith(expect.objectContaining(medAuthority));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IMedAuthority>>();
      const medAuthority = { id: 123 };
      vi.spyOn(medAuthorityFormService, 'getMedAuthority').mockReturnValue({ id: null });
      vi.spyOn(medAuthorityService, 'create').mockReturnValue(saveSubject);
      vi.spyOn(comp, 'previousState');
      activatedRoute.data = of({ medAuthority: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: medAuthority }));
      saveSubject.complete();

      // THEN
      expect(medAuthorityFormService.getMedAuthority).toHaveBeenCalled();
      expect(medAuthorityService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IMedAuthority>>();
      const medAuthority = { id: 123 };
      vi.spyOn(medAuthorityService, 'update').mockReturnValue(saveSubject);
      vi.spyOn(comp, 'previousState');
      activatedRoute.data = of({ medAuthority });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(medAuthorityService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
