import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, from } from 'rxjs';

import { CustomAuditEventFormService } from './custom-audit-event-form.service';
import { CustomAuditEventService } from '../service/custom-audit-event.service';
import { ICustomAuditEvent } from '../custom-audit-event.model';

import { CustomAuditEventUpdateComponent } from './custom-audit-event-update.component';

describe('CustomAuditEvent Management Update Component', () => {
  let comp: CustomAuditEventUpdateComponent;
  let fixture: ComponentFixture<CustomAuditEventUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let customAuditEventFormService: CustomAuditEventFormService;
  let customAuditEventService: CustomAuditEventService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([])],
      declarations: [CustomAuditEventUpdateComponent],
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
      .overrideTemplate(CustomAuditEventUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(CustomAuditEventUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    customAuditEventFormService = TestBed.inject(CustomAuditEventFormService);
    customAuditEventService = TestBed.inject(CustomAuditEventService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should update editForm', () => {
      const customAuditEvent: ICustomAuditEvent = { id: 456 };

      activatedRoute.data = of({ customAuditEvent });
      comp.ngOnInit();

      expect(comp.customAuditEvent).toEqual(customAuditEvent);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ICustomAuditEvent>>();
      const customAuditEvent = { id: 123 };
      jest.spyOn(customAuditEventFormService, 'getCustomAuditEvent').mockReturnValue(customAuditEvent);
      jest.spyOn(customAuditEventService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ customAuditEvent });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: customAuditEvent }));
      saveSubject.complete();

      // THEN
      expect(customAuditEventFormService.getCustomAuditEvent).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(customAuditEventService.update).toHaveBeenCalledWith(expect.objectContaining(customAuditEvent));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ICustomAuditEvent>>();
      const customAuditEvent = { id: 123 };
      jest.spyOn(customAuditEventFormService, 'getCustomAuditEvent').mockReturnValue({ id: null });
      jest.spyOn(customAuditEventService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ customAuditEvent: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: customAuditEvent }));
      saveSubject.complete();

      // THEN
      expect(customAuditEventFormService.getCustomAuditEvent).toHaveBeenCalled();
      expect(customAuditEventService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<ICustomAuditEvent>>();
      const customAuditEvent = { id: 123 };
      jest.spyOn(customAuditEventService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ customAuditEvent });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(customAuditEventService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });
});
