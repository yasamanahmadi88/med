import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { IFlow, NewFlow } from '../flow.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IFlow for edit and NewFlowFormGroupInput for create.
 */
type FlowFormGroupInput = IFlow | PartialWithRequiredKeyOf<NewFlow>;

type FlowFormDefaults = Pick<NewFlow, 'id'>;

type FlowFormGroupContent = {
  id: FormControl<IFlow['id'] | NewFlow['id']>;
  flowName: FormControl<IFlow['flowName']>;
  flowDesc: FormControl<IFlow['flowDesc']>;
  flow: FormControl<IFlow['flow']>;
  product: FormControl<IFlow['product']>;
};

export type FlowFormGroup = FormGroup<FlowFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class FlowFormService {
  createFlowFormGroup(flow: FlowFormGroupInput = { id: null }): FlowFormGroup {
    const flowRawValue = {
      ...this.getFormDefaults(),
      ...flow,
    };
    return new FormGroup<FlowFormGroupContent>({
      id: new FormControl(
        { value: flowRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        }
      ),
      flowName: new FormControl(flowRawValue.flowName, {
        validators: [Validators.required, Validators.maxLength(6), Validators.minLength(6)],
      }),
      flowDesc: new FormControl(flowRawValue.flowDesc, {
        validators: [Validators.required, Validators.maxLength(300)],
      }),
      flow: new FormControl(flowRawValue.flow, {
        validators: [Validators.required],
      }),
      product: new FormControl(flowRawValue.product, {
        validators: [Validators.required],
      }),
    });
  }

  getFlow(form: FlowFormGroup): IFlow | NewFlow {
    return form.getRawValue() as IFlow | NewFlow;
  }

  resetForm(form: FlowFormGroup, flow: FlowFormGroupInput): void {
    const flowRawValue = { ...this.getFormDefaults(), ...flow };
    form.reset(
      {
        ...flowRawValue,
        id: { value: flowRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */
    );
  }

  private getFormDefaults(): FlowFormDefaults {
    return {
      id: null,
    };
  }
}
