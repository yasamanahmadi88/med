import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import { MedAuthorityFormService, MedAuthorityFormGroup } from './med-authority-form.service';
import { IMedAuthority } from '../med-authority.model';
import { MedAuthorityService } from '../service/med-authority.service';

@Component({
  selector: 'jhi-med-authority-update',
  templateUrl: './med-authority-update.component.html',
  standalone: false,
})
export class MedAuthorityUpdateComponent implements OnInit {
  isSaving = false;
  medAuthority: IMedAuthority | null = null;
  authorities: IMedAuthority[] | null = null;

  editForm: MedAuthorityFormGroup = this.medAuthorityFormService.createMedAuthorityFormGroup();

  constructor(
    protected medAuthorityService: MedAuthorityService,
    protected medAuthorityFormService: MedAuthorityFormService,
    protected activatedRoute: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ medAuthority }) => {
      this.medAuthority = medAuthority;
      if (medAuthority) {
        this.updateForm(medAuthority);
      }
    });
    this.medAuthorityService.query().subscribe((res: HttpResponse<IMedAuthority[]>) => (this.authorities = res.body ?? []));
  }

  previousState(): void {
    window.history.back();
  }

  trackById(index: number, item: IMedAuthority): any {
    return item.id;
  }

  save(): void {
    this.isSaving = true;
    const medAuthority = this.medAuthorityFormService.getMedAuthority(this.editForm);
    if (medAuthority.id !== null) {
      this.subscribeToSaveResponse(this.medAuthorityService.update(medAuthority));
    } else {
      this.subscribeToSaveResponse(this.medAuthorityService.create(medAuthority));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IMedAuthority>>): void {
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

  protected updateForm(medAuthority: IMedAuthority): void {
    this.medAuthority = medAuthority;
    this.medAuthorityFormService.resetForm(this.editForm, medAuthority);
  }
}
