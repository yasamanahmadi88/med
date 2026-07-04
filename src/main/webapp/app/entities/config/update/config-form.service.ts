import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { IConfig, NewConfig } from '../config.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IConfig for edit and NewConfigFormGroupInput for create.
 */
type ConfigFormGroupInput = IConfig | PartialWithRequiredKeyOf<NewConfig>;

type ConfigFormDefaults = Pick<NewConfig, 'id'>;

type ConfigFormGroupContent = {
  id: FormControl<IConfig['id'] | NewConfig['id']>;
  property: FormControl<IConfig['property']>;
  pValue: FormControl<IConfig['pValue']>;
  commentDesc: FormControl<IConfig['commentDesc']>;
  module: FormControl<IConfig['module']>;
};

export type ConfigFormGroup = FormGroup<ConfigFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class ConfigFormService {
  createConfigFormGroup(config: ConfigFormGroupInput = { id: null }): ConfigFormGroup {
    const configRawValue = {
      ...this.getFormDefaults(),
      ...config,
    };
    return new FormGroup<ConfigFormGroupContent>({
      id: new FormControl(
        { value: configRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        }
      ),
      property: new FormControl(configRawValue.property, {
        validators: [Validators.required, Validators.maxLength(100)],
      }),
      pValue: new FormControl(configRawValue.pValue, {
        validators: [Validators.required, Validators.maxLength(300)],
      }),
      commentDesc: new FormControl(configRawValue.commentDesc, {
        validators: [Validators.maxLength(200)],
      }),
      module: new FormControl(configRawValue.module, {
        validators: [Validators.required],
      }),
    });
  }

  getConfig(form: ConfigFormGroup): IConfig | NewConfig {
    return form.getRawValue() as IConfig | NewConfig;
  }

  resetForm(form: ConfigFormGroup, config: ConfigFormGroupInput): void {
    const configRawValue = { ...this.getFormDefaults(), ...config };
    form.reset(
      {
        ...configRawValue,
        id: { value: configRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */
    );
  }

  private getFormDefaults(): ConfigFormDefaults {
    return {
      id: null,
    };
  }
}
