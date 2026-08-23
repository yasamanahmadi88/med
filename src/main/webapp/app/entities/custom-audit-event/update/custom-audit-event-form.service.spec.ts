import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../custom-audit-event.test-samples';

import { CustomAuditEventFormService } from './custom-audit-event-form.service';

describe('CustomAuditEvent Form Service', () => {
  let service: CustomAuditEventFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CustomAuditEventFormService);
  });

  describe('Service methods', () => {
    describe('createCustomAuditEventFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createCustomAuditEventFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            principal: expect.any(Object),
            eventDate: expect.any(Object),
            eventType: expect.any(Object),
          })
        );
      });

      it('passing ICustomAuditEvent should create a new form with FormGroup', () => {
        const formGroup = service.createCustomAuditEventFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            principal: expect.any(Object),
            eventDate: expect.any(Object),
            eventType: expect.any(Object),
          })
        );
      });
    });

    describe('getCustomAuditEvent', () => {
      it('should return NewCustomAuditEvent for default CustomAuditEvent initial value', () => {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        const formGroup = service.createCustomAuditEventFormGroup(sampleWithNewData);

        const customAuditEvent = service.getCustomAuditEvent(formGroup) as any;

        expect(customAuditEvent).toMatchObject(sampleWithNewData);
      });

      it('should return NewCustomAuditEvent for empty CustomAuditEvent initial value', () => {
        const formGroup = service.createCustomAuditEventFormGroup();

        const customAuditEvent = service.getCustomAuditEvent(formGroup) as any;

        expect(customAuditEvent).toMatchObject({});
      });

      it('should return ICustomAuditEvent', () => {
        const formGroup = service.createCustomAuditEventFormGroup(sampleWithRequiredData);

        const customAuditEvent = service.getCustomAuditEvent(formGroup) as any;

        expect(customAuditEvent).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing ICustomAuditEvent should not enable id FormControl', () => {
        const formGroup = service.createCustomAuditEventFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewCustomAuditEvent should disable id FormControl', () => {
        const formGroup = service.createCustomAuditEventFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
