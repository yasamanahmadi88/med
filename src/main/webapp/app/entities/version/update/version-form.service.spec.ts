import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../version.test-samples';

import { VersionFormService } from './version-form.service';

describe('Version Form Service', () => {
  let service: VersionFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(VersionFormService);
  });

  describe('Service methods', () => {
    describe('createVersionFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createVersionFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            tableName: expect.any(Object),
            moduleName: expect.any(Object),
            tableVersion: expect.any(Object),
          }),
        );
      });

      it('passing IVersion should create a new form with FormGroup', () => {
        const formGroup = service.createVersionFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            tableName: expect.any(Object),
            moduleName: expect.any(Object),
            tableVersion: expect.any(Object),
          }),
        );
      });
    });

    describe('getVersion', () => {
      it('should return NewVersion for default Version initial value', () => {
        const formGroup = service.createVersionFormGroup(sampleWithNewData);

        const version = service.getVersion(formGroup) as any;

        expect(version).toMatchObject(sampleWithNewData);
      });

      it('should return NewVersion for empty Version initial value', () => {
        const formGroup = service.createVersionFormGroup();

        const version = service.getVersion(formGroup) as any;

        expect(version).toMatchObject({});
      });

      it('should return IVersion', () => {
        const formGroup = service.createVersionFormGroup(sampleWithRequiredData);

        const version = service.getVersion(formGroup) as any;

        expect(version).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IVersion should not enable id FormControl', () => {
        const formGroup = service.createVersionFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewVersion should disable id FormControl', () => {
        const formGroup = service.createVersionFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
