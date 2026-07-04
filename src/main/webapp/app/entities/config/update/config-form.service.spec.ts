import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../config.test-samples';

import { ConfigFormService } from './config-form.service';

describe('Config Form Service', () => {
  let service: ConfigFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ConfigFormService);
  });

  describe('Service methods', () => {
    describe('createConfigFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createConfigFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            property: expect.any(Object),
            pValue: expect.any(Object),
            commentDesc: expect.any(Object),
            module: expect.any(Object),
          })
        );
      });

      it('passing IConfig should create a new form with FormGroup', () => {
        const formGroup = service.createConfigFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            property: expect.any(Object),
            pValue: expect.any(Object),
            commentDesc: expect.any(Object),
            module: expect.any(Object),
          })
        );
      });
    });

    describe('getConfig', () => {
      it('should return NewConfig for default Config initial value', () => {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        const formGroup = service.createConfigFormGroup(sampleWithNewData);

        const config = service.getConfig(formGroup) as any;

        expect(config).toMatchObject(sampleWithNewData);
      });

      it('should return NewConfig for empty Config initial value', () => {
        const formGroup = service.createConfigFormGroup();

        const config = service.getConfig(formGroup) as any;

        expect(config).toMatchObject({});
      });

      it('should return IConfig', () => {
        const formGroup = service.createConfigFormGroup(sampleWithRequiredData);

        const config = service.getConfig(formGroup) as any;

        expect(config).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IConfig should not enable id FormControl', () => {
        const formGroup = service.createConfigFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewConfig should disable id FormControl', () => {
        const formGroup = service.createConfigFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
