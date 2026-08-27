import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { IInstance, NewInstance } from '../instance.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IInstance for edit and NewInstanceFormGroupInput for create.
 */
type InstanceFormGroupInput = IInstance | PartialWithRequiredKeyOf<NewInstance>;

type InstanceFormDefaults = Pick<NewInstance, 'id'>;

type InstanceFormGroupContent = {
  id: FormControl<IInstance['id'] | NewInstance['id']>;
  moduleName: FormControl<IInstance['moduleName']>;
  port: FormControl<IInstance['port']>;
  moduleStatus: FormControl<IInstance['moduleStatus']>;
  lastUpdateDate: FormControl<IInstance['lastUpdateDate']>;
  threadPoolQueueSize: FormControl<IInstance['threadPoolQueueSize']>;
  moduleStartTime: FormControl<IInstance['moduleStartTime']>;
  totalProcessedWork: FormControl<IInstance['totalProcessedWork']>;
  totalProcessedTask: FormControl<IInstance['totalProcessedTask']>;
  processedStatistics: FormControl<IInstance['processedStatistics']>;
  hostName: FormControl<IInstance['hostName']>;
};

export type InstanceFormGroup = FormGroup<InstanceFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class InstanceFormService {
  createInstanceFormGroup(instance: InstanceFormGroupInput = { id: null }): InstanceFormGroup {
    const instanceRawValue = {
      ...this.getFormDefaults(),
      ...instance,
    };
    return new FormGroup<InstanceFormGroupContent>({
      id: new FormControl(
        { value: instanceRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        }
      ),
      moduleName: new FormControl(instanceRawValue.moduleName, {
        validators: [Validators.required, Validators.maxLength(50)],
      }),
      port: new FormControl(instanceRawValue.port, {
        validators: [Validators.required, Validators.maxLength(10)],
      }),
      moduleStatus: new FormControl(instanceRawValue.moduleStatus, {
        validators: [Validators.required, Validators.maxLength(50)],
      }),
      lastUpdateDate: new FormControl(instanceRawValue.lastUpdateDate, {
        validators: [Validators.required],
      }),
      threadPoolQueueSize: new FormControl(instanceRawValue.threadPoolQueueSize),
      moduleStartTime: new FormControl(instanceRawValue.moduleStartTime),
      totalProcessedWork: new FormControl(instanceRawValue.totalProcessedWork),
      totalProcessedTask: new FormControl(instanceRawValue.totalProcessedTask),
      processedStatistics: new FormControl(instanceRawValue.processedStatistics),
      hostName: new FormControl(instanceRawValue.hostName, {
        validators: [Validators.required, Validators.maxLength(50)],
      }),
    });
  }

  getInstance(form: InstanceFormGroup): IInstance | NewInstance {
    return form.getRawValue() as IInstance | NewInstance;
  }

  resetForm(form: InstanceFormGroup, instance: InstanceFormGroupInput): void {
    const instanceRawValue = { ...this.getFormDefaults(), ...instance };
    form.reset(
      {
        ...instanceRawValue,
        id: { value: instanceRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */
    );
  }

  private getFormDefaults(): InstanceFormDefaults {
    return {
      id: null,
    };
  }
}
