import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, from } from 'rxjs';

import { ResourceAuthorityFormService } from './resource-authority-form.service';
import { ResourceAuthorityService } from '../service/resource-authority.service';
import { IResourceAuthority } from '../resource-authority.model';
import { IMedAuthority } from 'app/entities/med-authority/med-authority.model';
import { MedAuthorityService } from 'app/entities/med-authority/service/med-authority.service';
import { IResource } from 'app/entities/resource/resource.model';
import { ResourceService } from 'app/entities/resource/service/resource.service';

import { ResourceAuthorityUpdateComponent } from './resource-authority-update.component';

describe('ResourceAuthority Management Update Component', () => {
  let comp: ResourceAuthorityUpdateComponent;
  let fixture: ComponentFixture<ResourceAuthorityUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let resourceAuthorityFormService: ResourceAuthorityFormService;
  let resourceAuthorityService: ResourceAuthorityService;
  let medAuthorityService: MedAuthorityService;
  let resourceService: ResourceService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([])],
      declarations: [ResourceAuthorityUpdateComponent],
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
      .overrideTemplate(ResourceAuthorityUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(ResourceAuthorityUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    resourceAuthorityFormService = TestBed.inject(ResourceAuthorityFormService);
    resourceAuthorityService = TestBed.inject(ResourceAuthorityService);
    medAuthorityService = TestBed.inject(MedAuthorityService);
    resourceService = TestBed.inject(ResourceService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call MedAuthority query and add missing value', () => {
      const resourceAuthority: IResourceAuthority = { id: 456 };
      const medAuthority: IMedAuthority = { id: 51828 };
      resourceAuthority.medAuthority = medAuthority;

      const medAuthorityCollection: IMedAuthority[] = [{ id: 32359 }];
      jest.spyOn(medAuthorityService, 'query').mockReturnValue(of(new HttpResponse({ body: medAuthorityCollection })));
      const additionalMedAuthorities = [medAuthority];
      const expectedCollection: IMedAuthority[] = [...additionalMedAuthorities, ...medAuthorityCollection];
      jest.spyOn(medAuthorityService, 'addMedAuthorityToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ resourceAuthority });
      comp.ngOnInit();

      expect(medAuthorityService.query).toHaveBeenCalled();
      expect(medAuthorityService.addMedAuthorityToCollectionIfMissing).toHaveBeenCalledWith(
        medAuthorityCollection,
        ...additionalMedAuthorities.map(expect.objectContaining)
      );
      expect(comp.medAuthoritiesSharedCollection).toEqual(expectedCollection);
    });

    it('Should call Resource query and add missing value', () => {
      const resourceAuthority: IResourceAuthority = { id: 456 };
      const resource: IResource = { id: 95110 };
      resourceAuthority.resource = resource;

      const resourceCollection: IResource[] = [{ id: 99786 }];
      jest.spyOn(resourceService, 'query').mockReturnValue(of(new HttpResponse({ body: resourceCollection })));
      const additionalResources = [resource];
      const expectedCollection: IResource[] = [...additionalResources, ...resourceCollection];
      jest.spyOn(resourceService, 'addResourceToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ resourceAuthority });
      comp.ngOnInit();

      expect(resourceService.query).toHaveBeenCalled();
      expect(resourceService.addResourceToCollectionIfMissing).toHaveBeenCalledWith(
        resourceCollection,
        ...additionalResources.map(expect.objectContaining)
      );
      expect(comp.resourcesSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const resourceAuthority: IResourceAuthority = { id: 456 };
      const medAuthority: IMedAuthority = { id: 56113 };
      resourceAuthority.medAuthority = medAuthority;
      const resource: IResource = { id: 99862 };
      resourceAuthority.resource = resource;

      activatedRoute.data = of({ resourceAuthority });
      comp.ngOnInit();

      expect(comp.medAuthoritiesSharedCollection).toContain(medAuthority);
      expect(comp.resourcesSharedCollection).toContain(resource);
      expect(comp.resourceAuthority).toEqual(resourceAuthority);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IResourceAuthority>>();
      const resourceAuthority = { id: 123 };
      jest.spyOn(resourceAuthorityFormService, 'getResourceAuthority').mockReturnValue(resourceAuthority);
      jest.spyOn(resourceAuthorityService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ resourceAuthority });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: resourceAuthority }));
      saveSubject.complete();

      // THEN
      expect(resourceAuthorityFormService.getResourceAuthority).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(resourceAuthorityService.update).toHaveBeenCalledWith(expect.objectContaining(resourceAuthority));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IResourceAuthority>>();
      const resourceAuthority = { id: 123 };
      jest.spyOn(resourceAuthorityFormService, 'getResourceAuthority').mockReturnValue({ id: null });
      jest.spyOn(resourceAuthorityService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ resourceAuthority: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: resourceAuthority }));
      saveSubject.complete();

      // THEN
      expect(resourceAuthorityFormService.getResourceAuthority).toHaveBeenCalled();
      expect(resourceAuthorityService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IResourceAuthority>>();
      const resourceAuthority = { id: 123 };
      jest.spyOn(resourceAuthorityService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ resourceAuthority });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(resourceAuthorityService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareMedAuthority', () => {
      it('Should forward to medAuthorityService', () => {
        const entity = { id: 123 };
        const entity2 = { id: 456 };
        jest.spyOn(medAuthorityService, 'compareMedAuthority');
        comp.compareMedAuthority(entity, entity2);
        expect(medAuthorityService.compareMedAuthority).toHaveBeenCalledWith(entity, entity2);
      });
    });

    describe('compareResource', () => {
      it('Should forward to resourceService', () => {
        const entity = { id: 123 };
        const entity2 = { id: 456 };
        jest.spyOn(resourceService, 'compareResource');
        comp.compareResource(entity, entity2);
        expect(resourceService.compareResource).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
