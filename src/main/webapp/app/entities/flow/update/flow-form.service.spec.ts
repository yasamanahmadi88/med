import { TestBed } from '@angular/core/testing';

import { sampleWithRequiredData, sampleWithNewData } from '../flow.test-samples';

import { FlowFormService } from './flow-form.service';

describe('Flow Form Service', () => {
  let service: FlowFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FlowFormService);
  });

  describe('Service methods', () => {
    describe('createFlowFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createFlowFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            flowName: expect.any(Object),
            flowDesc: expect.any(Object),
            flow: expect.any(Object),
            product: expect.any(Object),
          }),
        );
      });

      it('passing IFlow should create a new form with FormGroup', () => {
        const formGroup = service.createFlowFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            flowName: expect.any(Object),
            flowDesc: expect.any(Object),
            flow: expect.any(Object),
            product: expect.any(Object),
          }),
        );
      });
    });

    describe('getFlow', () => {
      it('should return NewFlow for default Flow initial value', () => {
        const formGroup = service.createFlowFormGroup(sampleWithNewData);

        const flow = service.getFlow(formGroup) as any;

        expect(flow).toMatchObject(sampleWithNewData);
      });

      it('should return NewFlow for empty Flow initial value', () => {
        const formGroup = service.createFlowFormGroup();

        const flow = service.getFlow(formGroup) as any;

        expect(flow).toMatchObject({});
      });

      it('should return IFlow', () => {
        const formGroup = service.createFlowFormGroup(sampleWithRequiredData);

        const flow = service.getFlow(formGroup) as any;

        expect(flow).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IFlow should not enable id FormControl', () => {
        const formGroup = service.createFlowFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewFlow should disable id FormControl', () => {
        const formGroup = service.createFlowFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
