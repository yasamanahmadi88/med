import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, from } from 'rxjs';

import { VersionFormService } from './version-form.service';
import { VersionService } from '../service/version.service';
import { IVersion } from '../version.model';

import { VersionUpdateComponent } from './version-update.component';

describe('Version Management Update Component', () => {
  let comp: VersionUpdateComponent;
  let fixture: ComponentFixture<VersionUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let versionFormService: VersionFormService;
  let versionService: VersionService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([])],
      declarations: [VersionUpdateComponent],
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
      .overrideTemplate(VersionUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(VersionUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    versionFormService = TestBed.inject(VersionFormService);
    versionService = TestBed.inject(VersionService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should update editForm', () => {
      const version: IVersion = { id: 456 };

      activatedRoute.data = of({ version });
      comp.ngOnInit();

      expect(comp.version).toEqual(version);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IVersion>>();
      const version = { id: 123 };
      jest.spyOn(versionFormService, 'getVersion').mockReturnValue(version);
      jest.spyOn(versionService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ version });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: version }));
      saveSubject.complete();

      // THEN
      expect(versionFormService.getVersion).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(versionService.update).toHaveBeenCalledWith(expect.objectContaining(version));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IVersion>>();
      const version = { id: 123 };
      jest.spyOn(versionFormService, 'getVersion').mockReturnValue({ id: null });
      jest.spyOn(versionService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ version: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: version }));
      saveSubject.complete();

      // THEN
      expect(versionFormService.getVersion).toHaveBeenCalled();
      expect(versionService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IVersion>>();
      const version = { id: 123 };
      jest.spyOn(versionService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ version });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(versionService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
