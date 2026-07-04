import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import { InstanceFormService, InstanceFormGroup } from './instance-form.service';
import { IInstance } from '../instance.model';
import { InstanceService } from '../service/instance.service';

@Component({
  selector: 'jhi-instance-update',
  templateUrl: './instance-update.component.html',
  standalone: false,
})
export class InstanceUpdateComponent implements OnInit {
  isSaving = false;
  instance: IInstance | null = null;

  editForm: InstanceFormGroup = this.instanceFormService.createInstanceFormGroup();

  constructor(
    protected instanceService: InstanceService,
    protected instanceFormService: InstanceFormService,
    protected activatedRoute: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ instance }) => {
      this.instance = instance;
      if (instance) {
        this.updateForm(instance);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const instance = this.instanceFormService.getInstance(this.editForm);
    if (instance.id !== null) {
      this.subscribeToSaveResponse(this.instanceService.update(instance));
    } else {
      this.subscribeToSaveResponse(this.instanceService.create(instance));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IInstance>>): void {
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

  protected updateForm(instance: IInstance): void {
    this.instance = instance;
    this.instanceFormService.resetForm(this.editForm, instance);
  }
}
