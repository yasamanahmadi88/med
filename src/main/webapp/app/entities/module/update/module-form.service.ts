import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { IModule, NewModule } from '../module.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IModule for edit and NewModuleFormGroupInput for create.
 */
type ModuleFormGroupInput = IModule | PartialWithRequiredKeyOf<NewModule>;

type ModuleFormDefaults = Pick<NewModule, 'id'>;

type ModuleFormGroupContent = {
  id: FormControl<IModule['id'] | NewModule['id']>;
  moduleName: FormControl<IModule['moduleName']>;
  defaultPort: FormControl<IModule['defaultPort']>;
  redisKeyPrefix: FormControl<IModule['redisKeyPrefix']>;
  status: FormControl<IModule['status']>;
  loggingMode: FormControl<IModule['loggingMode']>;
  loggingFilter: FormControl<IModule['loggingFilter']>;
  dnsName: FormControl<IModule['dnsName']>;
};

export type ModuleFormGroup = FormGroup<ModuleFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class ModuleFormService {
  createModuleFormGroup(module: ModuleFormGroupInput = { id: null }): ModuleFormGroup {
    const moduleRawValue = {
      ...this.getFormDefaults(),
      ...module,
    };
    return new FormGroup<ModuleFormGroupContent>({
      id: new FormControl(
        { value: moduleRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      moduleName: new FormControl(moduleRawValue.moduleName, {
        validators: [Validators.required, Validators.maxLength(50), Validators.pattern('[a-z-]+')],
      }),
      defaultPort: new FormControl(moduleRawValue.defaultPort, {
        validators: [Validators.required, Validators.minLength(4), Validators.maxLength(4), Validators.pattern('[0-9]+')],
      }),
      redisKeyPrefix: new FormControl(moduleRawValue.redisKeyPrefix, {
        validators: [Validators.required, Validators.minLength(5), Validators.maxLength(5), Validators.pattern('[A-Z]+')],
      }),
      status: new FormControl(moduleRawValue.status, {
        validators: [],
      }),
      loggingMode: new FormControl(moduleRawValue.loggingMode, {
        validators: [Validators.required],
      }),
      loggingFilter: new FormControl(moduleRawValue.loggingFilter, {
        validators: [Validators.maxLength(500)],
      }),
      dnsName: new FormControl(moduleRawValue.dnsName, {
        validators: [Validators.required, Validators.maxLength(100)],
      }),
    });
  }

  getModule(form: ModuleFormGroup): IModule | NewModule {
    return form.getRawValue();
  }

  resetForm(form: ModuleFormGroup, module: ModuleFormGroupInput): void {
    const moduleRawValue = { ...this.getFormDefaults(), ...module };
    form.reset(
      {
        ...moduleRawValue,
        id: { value: moduleRawValue.id, disabled: true },
      } /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): ModuleFormDefaults {
    return {
      id: null,
    };
  }
}
