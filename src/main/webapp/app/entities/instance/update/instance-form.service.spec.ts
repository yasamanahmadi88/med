import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../instance.test-samples';

import { InstanceFormService } from './instance-form.service';

describe('Instance Form Service', () => {
  let service: InstanceFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(InstanceFormService);
  });

  describe('Service methods', () => {
    describe('createInstanceFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createInstanceFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            moduleName: expect.any(Object),
            port: expect.any(Object),
            moduleStatus: expect.any(Object),
            lastUpdateDate: expect.any(Object),
            threadPoolQueueSize: expect.any(Object),
            moduleStartTime: expect.any(Object),
            totalProcessedWork: expect.any(Object),
            totalProcessedTask: expect.any(Object),
            processedStatistics: expect.any(Object),
            hostName: expect.any(Object),
          })
        );
      });

      it('passing IInstance should create a new form with FormGroup', () => {
        const formGroup = service.createInstanceFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            moduleName: expect.any(Object),
            port: expect.any(Object),
            moduleStatus: expect.any(Object),
            lastUpdateDate: expect.any(Object),
            threadPoolQueueSize: expect.any(Object),
            moduleStartTime: expect.any(Object),
            totalProcessedWork: expect.any(Object),
            totalProcessedTask: expect.any(Object),
            processedStatistics: expect.any(Object),
            hostName: expect.any(Object),
          })
        );
      });
    });

    describe('getInstance', () => {
      it('should return NewInstance for default Instance initial value', () => {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        const formGroup = service.createInstanceFormGroup(sampleWithNewData);

        const instance = service.getInstance(formGroup) as any;

        expect(instance).toMatchObject(sampleWithNewData);
      });

      it('should return NewInstance for empty Instance initial value', () => {
        const formGroup = service.createInstanceFormGroup();

        const instance = service.getInstance(formGroup) as any;

        expect(instance).toMatchObject({});
      });

      it('should return IInstance', () => {
        const formGroup = service.createInstanceFormGroup(sampleWithRequiredData);

        const instance = service.getInstance(formGroup) as any;

        expect(instance).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IInstance should not enable id FormControl', () => {
        const formGroup = service.createInstanceFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewInstance should disable id FormControl', () => {
        const formGroup = service.createInstanceFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
