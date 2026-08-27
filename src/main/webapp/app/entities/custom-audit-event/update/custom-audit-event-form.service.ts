import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { ICustomAuditEvent, NewCustomAuditEvent } from '../custom-audit-event.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ICustomAuditEvent for edit and NewCustomAuditEventFormGroupInput for create.
 */
type CustomAuditEventFormGroupInput = ICustomAuditEvent | PartialWithRequiredKeyOf<NewCustomAuditEvent>;

type CustomAuditEventFormDefaults = Pick<NewCustomAuditEvent, 'id'>;

type CustomAuditEventFormGroupContent = {
  id: FormControl<ICustomAuditEvent['id'] | NewCustomAuditEvent['id']>;
  principal: FormControl<ICustomAuditEvent['principal']>;
  eventDate: FormControl<ICustomAuditEvent['eventDate']>;
  eventType: FormControl<ICustomAuditEvent['eventType']>;
};

export type CustomAuditEventFormGroup = FormGroup<CustomAuditEventFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class CustomAuditEventFormService {
  createCustomAuditEventFormGroup(customAuditEvent: CustomAuditEventFormGroupInput = { id: null }): CustomAuditEventFormGroup {
    const customAuditEventRawValue = {
      ...this.getFormDefaults(),
      ...customAuditEvent,
    };
    return new FormGroup<CustomAuditEventFormGroupContent>({
      id: new FormControl(
        { value: customAuditEventRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        }
      ),
      principal: new FormControl(customAuditEventRawValue.principal, {
        validators: [Validators.required, Validators.maxLength(50)],
      }),
      eventDate: new FormControl(customAuditEventRawValue.eventDate),
      eventType: new FormControl(customAuditEventRawValue.eventType, {
        validators: [Validators.maxLength(255)],
      }),
    });
  }

  getCustomAuditEvent(form: CustomAuditEventFormGroup): ICustomAuditEvent | NewCustomAuditEvent {
    return form.getRawValue() as ICustomAuditEvent | NewCustomAuditEvent;
  }

  resetForm(form: CustomAuditEventFormGroup, customAuditEvent: CustomAuditEventFormGroupInput): void {
    const customAuditEventRawValue = { ...this.getFormDefaults(), ...customAuditEvent };
    form.reset(
      {
        ...customAuditEventRawValue,
        id: { value: customAuditEventRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */
    );
  }

  private getFormDefaults(): CustomAuditEventFormDefaults {
    return {
      id: null,
    };
  }
}
