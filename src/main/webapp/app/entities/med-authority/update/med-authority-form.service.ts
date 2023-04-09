import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { IMedAuthority, NewMedAuthority } from '../med-authority.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IMedAuthority for edit and NewMedAuthorityFormGroupInput for create.
 */
type MedAuthorityFormGroupInput = IMedAuthority | PartialWithRequiredKeyOf<NewMedAuthority>;

type MedAuthorityFormDefaults = Pick<NewMedAuthority, 'id'>;

type MedAuthorityFormGroupContent = {
  id: FormControl<IMedAuthority['id'] | NewMedAuthority['id']>;
  name: FormControl<IMedAuthority['name']>;
  displayName: FormControl<IMedAuthority['displayName']>;
  parentId: FormControl<IMedAuthority['parentId']>;
};

export type MedAuthorityFormGroup = FormGroup<MedAuthorityFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class MedAuthorityFormService {
  createMedAuthorityFormGroup(medAuthority: MedAuthorityFormGroupInput = { id: null }): MedAuthorityFormGroup {
    const medAuthorityRawValue = {
      ...this.getFormDefaults(),
      ...medAuthority,
    };
    return new FormGroup<MedAuthorityFormGroupContent>({
      id: new FormControl(
        { value: medAuthorityRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        }
      ),
      name: new FormControl(medAuthorityRawValue.name, {
        validators: [Validators.required, Validators.maxLength(50)],
      }),
      displayName: new FormControl(medAuthorityRawValue.displayName, {
        validators: [Validators.maxLength(500)],
      }),
      parentId: new FormControl(medAuthorityRawValue.parentId),
    });
  }

  getMedAuthority(form: MedAuthorityFormGroup): IMedAuthority | NewMedAuthority {
    return form.getRawValue() as IMedAuthority | NewMedAuthority;
  }

  resetForm(form: MedAuthorityFormGroup, medAuthority: MedAuthorityFormGroupInput): void {
    const medAuthorityRawValue = { ...this.getFormDefaults(), ...medAuthority };
    form.reset(
      {
        ...medAuthorityRawValue,
        id: { value: medAuthorityRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */
    );
  }

  private getFormDefaults(): MedAuthorityFormDefaults {
    return {
      id: null,
    };
  }
}
