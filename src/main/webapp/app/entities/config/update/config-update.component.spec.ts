import { vi } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, from } from 'rxjs';

import { ConfigFormService } from './config-form.service';
import { ConfigService } from '../service/config.service';
import { IConfig } from '../config.model';
import { IModule } from 'app/entities/module/module.model';
import { ModuleService } from 'app/entities/module/service/module.service';

import { ConfigUpdateComponent } from './config-update.component';

describe('Config Management Update Component', () => {
  let comp: ConfigUpdateComponent;
  let fixture: ComponentFixture<ConfigUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let configFormService: ConfigFormService;
  let configService: ConfigService;
  let moduleService: ModuleService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([])],
      declarations: [ConfigUpdateComponent],
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
      .overrideTemplate(ConfigUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(ConfigUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    configFormService = TestBed.inject(ConfigFormService);
    configService = TestBed.inject(ConfigService);
    moduleService = TestBed.inject(ModuleService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call Module query and add missing value', () => {
      const config: IConfig = { id: 456 };
      const module: IModule = { id: 46016 };
      config.module = module;

      const moduleCollection: IModule[] = [{ id: 73220 }];
      vi.spyOn(moduleService, 'query').mockReturnValue(of(new HttpResponse({ body: moduleCollection })));
      const additionalModules = [module];
      const expectedCollection: IModule[] = [...additionalModules, ...moduleCollection];
      vi.spyOn(moduleService, 'addModuleToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ config });
      comp.ngOnInit();

      expect(moduleService.query).toHaveBeenCalled();
      expect(moduleService.addModuleToCollectionIfMissing).toHaveBeenCalledWith(
        moduleCollection,
        ...additionalModules.map(item => expect.objectContaining(item)),
      );
      expect(comp.modulesSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const config: IConfig = { id: 456 };
      const module: IModule = { id: 48658 };
      config.module = module;

      activatedRoute.data = of({ config });
      comp.ngOnInit();

      expect(comp.modulesSharedCollection).toContain(module);
      expect(comp.config).toEqual(config);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IConfig>>();
      const config = { id: 123 };
      vi.spyOn(configFormService, 'getConfig').mockReturnValue(config);
      vi.spyOn(configService, 'update').mockReturnValue(saveSubject);
      vi.spyOn(comp, 'previousState');
      activatedRoute.data = of({ config });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: config }));
      saveSubject.complete();

      // THEN
      expect(configFormService.getConfig).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(configService.update).toHaveBeenCalledWith(expect.objectContaining(config));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IConfig>>();
      const config = { id: 123 };
      vi.spyOn(configFormService, 'getConfig').mockReturnValue({ id: null });
      vi.spyOn(configService, 'create').mockReturnValue(saveSubject);
      vi.spyOn(comp, 'previousState');
      activatedRoute.data = of({ config: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: config }));
      saveSubject.complete();

      // THEN
      expect(configFormService.getConfig).toHaveBeenCalled();
      expect(configService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IConfig>>();
      const config = { id: 123 };
      vi.spyOn(configService, 'update').mockReturnValue(saveSubject);
      vi.spyOn(comp, 'previousState');
      activatedRoute.data = of({ config });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(configService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareModule', () => {
      it('Should forward to moduleService', () => {
        const entity = { id: 123 };
        const entity2 = { id: 456 };
        vi.spyOn(moduleService, 'compareModule');
        comp.compareModule(entity, entity2);
        expect(moduleService.compareModule).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
