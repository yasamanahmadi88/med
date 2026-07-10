import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import { CustomAuditEventFormService, CustomAuditEventFormGroup } from './custom-audit-event-form.service';
import { ICustomAuditEvent } from '../custom-audit-event.model';
import { CustomAuditEventService } from '../service/custom-audit-event.service';

@Component({
  selector: 'jhi-custom-audit-event-update',
  templateUrl: './custom-audit-event-update.component.html',
})
export class CustomAuditEventUpdateComponent implements OnInit {
  isSaving = false;
  customAuditEvent: ICustomAuditEvent | null = null;

  editForm: CustomAuditEventFormGroup = this.customAuditEventFormService.createCustomAuditEventFormGroup();

  constructor(
    protected customAuditEventService: CustomAuditEventService,
    protected customAuditEventFormService: CustomAuditEventFormService,
    protected activatedRoute: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ customAuditEvent }) => {
      this.customAuditEvent = customAuditEvent;
      if (customAuditEvent) {
        this.updateForm(customAuditEvent);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const customAuditEvent = this.customAuditEventFormService.getCustomAuditEvent(this.editForm);
    if (customAuditEvent.id !== null) {
      this.subscribeToSaveResponse(this.customAuditEventService.update(customAuditEvent));
    } else {
      this.subscribeToSaveResponse(this.customAuditEventService.create(customAuditEvent));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ICustomAuditEvent>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(customAuditEvent: ICustomAuditEvent): void {
    this.customAuditEvent = customAuditEvent;
    this.customAuditEventFormService.resetForm(this.editForm, customAuditEvent);
  }
}
