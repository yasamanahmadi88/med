import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../resource-authority.test-samples';

import { ResourceAuthorityFormService } from './resource-authority-form.service';

describe('ResourceAuthority Form Service', () => {
  let service: ResourceAuthorityFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ResourceAuthorityFormService);
  });

  describe('Service methods', () => {
    describe('createResourceAuthorityFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createResourceAuthorityFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            verb: expect.any(Object),
            medAuthority: expect.any(Object),
            resource: expect.any(Object),
          })
        );
      });

      it('passing IResourceAuthority should create a new form with FormGroup', () => {
        const formGroup = service.createResourceAuthorityFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            verb: expect.any(Object),
            medAuthority: expect.any(Object),
            resource: expect.any(Object),
          })
        );
      });
    });

    describe('getResourceAuthority', () => {
      it('should return NewResourceAuthority for default ResourceAuthority initial value', () => {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        const formGroup = service.createResourceAuthorityFormGroup(sampleWithNewData);

        const resourceAuthority = service.getResourceAuthority(formGroup) as any;

        expect(resourceAuthority).toMatchObject(sampleWithNewData);
      });

      it('should return NewResourceAuthority for empty ResourceAuthority initial value', () => {
        const formGroup = service.createResourceAuthorityFormGroup();

        const resourceAuthority = service.getResourceAuthority(formGroup) as any;

        expect(resourceAuthority).toMatchObject({});
      });

      it('should return IResourceAuthority', () => {
        const formGroup = service.createResourceAuthorityFormGroup(sampleWithRequiredData);

        const resourceAuthority = service.getResourceAuthority(formGroup) as any;

        expect(resourceAuthority).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IResourceAuthority should not enable id FormControl', () => {
        const formGroup = service.createResourceAuthorityFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewResourceAuthority should disable id FormControl', () => {
        const formGroup = service.createResourceAuthorityFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
