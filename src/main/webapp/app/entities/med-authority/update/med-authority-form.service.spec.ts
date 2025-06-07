import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../med-authority.test-samples';

import { MedAuthorityFormService } from './med-authority-form.service';

describe('MedAuthority Form Service', () => {
  let service: MedAuthorityFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MedAuthorityFormService);
  });

  describe('Service methods', () => {
    describe('createMedAuthorityFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createMedAuthorityFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            displayName: expect.any(Object),
            parentId: expect.any(Object),
          })
        );
      });

      it('passing IMedAuthority should create a new form with FormGroup', () => {
        const formGroup = service.createMedAuthorityFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            displayName: expect.any(Object),
            parentId: expect.any(Object),
          })
        );
      });
    });

    describe('getMedAuthority', () => {
      it('should return NewMedAuthority for default MedAuthority initial value', () => {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        const formGroup = service.createMedAuthorityFormGroup(sampleWithNewData);

        const medAuthority = service.getMedAuthority(formGroup) as any;

        expect(medAuthority).toMatchObject(sampleWithNewData);
      });

      it('should return NewMedAuthority for empty MedAuthority initial value', () => {
        const formGroup = service.createMedAuthorityFormGroup();

        const medAuthority = service.getMedAuthority(formGroup) as any;

        expect(medAuthority).toMatchObject({});
      });

      it('should return IMedAuthority', () => {
        const formGroup = service.createMedAuthorityFormGroup(sampleWithRequiredData);

        const medAuthority = service.getMedAuthority(formGroup) as any;

        expect(medAuthority).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IMedAuthority should not enable id FormControl', () => {
        const formGroup = service.createMedAuthorityFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewMedAuthority should disable id FormControl', () => {
        const formGroup = service.createMedAuthorityFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
