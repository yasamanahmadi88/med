import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import { VersionFormService, VersionFormGroup } from './version-form.service';
import { IVersion } from '../version.model';
import { VersionService } from '../service/version.service';

@Component({
  selector: 'jhi-version-update',
  templateUrl: './version-update.component.html',
  standalone: false,
})
export class VersionUpdateComponent implements OnInit {
  isSaving = false;
  version: IVersion | null = null;

  editForm: VersionFormGroup = this.versionFormService.createVersionFormGroup();

  constructor(
    protected versionService: VersionService,
    protected versionFormService: VersionFormService,
    protected activatedRoute: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ version }) => {
      this.version = version;
      if (version) {
        this.updateForm(version);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const version = this.versionFormService.getVersion(this.editForm);
    if (version.id !== null) {
      this.subscribeToSaveResponse(this.versionService.update(version));
    } else {
      this.subscribeToSaveResponse(this.versionService.create(version));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IVersion>>): void {
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

  protected updateForm(version: IVersion): void {
    this.version = version;
    this.versionFormService.resetForm(this.editForm, version);
  }
}
