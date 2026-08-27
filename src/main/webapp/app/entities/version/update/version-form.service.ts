import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { IVersion, NewVersion } from '../version.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IVersion for edit and NewVersionFormGroupInput for create.
 */
type VersionFormGroupInput = IVersion | PartialWithRequiredKeyOf<NewVersion>;

type VersionFormDefaults = Pick<NewVersion, 'id'>;

type VersionFormGroupContent = {
  id: FormControl<IVersion['id'] | NewVersion['id']>;
  tableName: FormControl<IVersion['tableName']>;
  moduleName: FormControl<IVersion['moduleName']>;
  tableVersion: FormControl<IVersion['tableVersion']>;
};

export type VersionFormGroup = FormGroup<VersionFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class VersionFormService {
  createVersionFormGroup(version: VersionFormGroupInput = { id: null }): VersionFormGroup {
    const versionRawValue = {
      ...this.getFormDefaults(),
      ...version,
    };
    return new FormGroup<VersionFormGroupContent>({
      id: new FormControl(
        { value: versionRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        }
      ),
      tableName: new FormControl(versionRawValue.tableName, {
        validators: [Validators.required, Validators.maxLength(100)],
      }),
      moduleName: new FormControl(versionRawValue.moduleName, {
        validators: [Validators.required, Validators.maxLength(50)],
      }),
      tableVersion: new FormControl(versionRawValue.tableVersion, {
        validators: [Validators.required],
      }),
    });
  }

  getVersion(form: VersionFormGroup): IVersion | NewVersion {
    return form.getRawValue() as IVersion | NewVersion;
  }

  resetForm(form: VersionFormGroup, version: VersionFormGroupInput): void {
    const versionRawValue = { ...this.getFormDefaults(), ...version };
    form.reset(
      {
        ...versionRawValue,
        id: { value: versionRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */
    );
  }

  private getFormDefaults(): VersionFormDefaults {
    return {
      id: null,
    };
  }
}
