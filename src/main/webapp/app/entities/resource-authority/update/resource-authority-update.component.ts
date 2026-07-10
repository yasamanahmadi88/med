import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import { ResourceAuthorityFormService, ResourceAuthorityFormGroup } from './resource-authority-form.service';
import { IResourceAuthority } from '../resource-authority.model';
import { ResourceAuthorityService } from '../service/resource-authority.service';
import { IMedAuthority } from 'app/entities/med-authority/med-authority.model';
import { MedAuthorityService } from 'app/entities/med-authority/service/med-authority.service';
import { IResource } from 'app/entities/resource/resource.model';
import { ResourceService } from 'app/entities/resource/service/resource.service';

@Component({
  selector: 'jhi-resource-authority-update',
  templateUrl: './resource-authority-update.component.html',
})
export class ResourceAuthorityUpdateComponent implements OnInit {
  isSaving = false;
  resourceAuthority: IResourceAuthority | null = null;

  medAuthoritiesSharedCollection: IMedAuthority[] = [];
  resourcesSharedCollection: IResource[] = [];

  editForm: ResourceAuthorityFormGroup = this.resourceAuthorityFormService.createResourceAuthorityFormGroup();

  constructor(
    protected resourceAuthorityService: ResourceAuthorityService,
    protected resourceAuthorityFormService: ResourceAuthorityFormService,
    protected medAuthorityService: MedAuthorityService,
    protected resourceService: ResourceService,
    protected activatedRoute: ActivatedRoute
  ) {}

  compareMedAuthority = (o1: IMedAuthority | null, o2: IMedAuthority | null): boolean =>
    this.medAuthorityService.compareMedAuthority(o1, o2);

  compareResource = (o1: IResource | null, o2: IResource | null): boolean => this.resourceService.compareResource(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ resourceAuthority }) => {
      this.resourceAuthority = resourceAuthority;
      if (resourceAuthority) {
        this.updateForm(resourceAuthority);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const resourceAuthority = this.resourceAuthorityFormService.getResourceAuthority(this.editForm);
    if (resourceAuthority.id !== null) {
      this.subscribeToSaveResponse(this.resourceAuthorityService.update(resourceAuthority));
    } else {
      this.subscribeToSaveResponse(this.resourceAuthorityService.create(resourceAuthority));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IResourceAuthority>>): void {
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

  protected updateForm(resourceAuthority: IResourceAuthority): void {
    this.resourceAuthority = resourceAuthority;
    this.resourceAuthorityFormService.resetForm(this.editForm, resourceAuthority);

    this.medAuthoritiesSharedCollection = this.medAuthorityService.addMedAuthorityToCollectionIfMissing<IMedAuthority>(
      this.medAuthoritiesSharedCollection,
      resourceAuthority.medAuthority
    );
    this.resourcesSharedCollection = this.resourceService.addResourceToCollectionIfMissing<IResource>(
      this.resourcesSharedCollection,
      resourceAuthority.resource
    );
  }

  protected loadRelationshipsOptions(): void {
    this.medAuthorityService
      .query({size: 1000})
      .pipe(map((res: HttpResponse<IMedAuthority[]>) => res.body ?? []))
      .pipe(
        map((medAuthorities: IMedAuthority[]) =>
          this.medAuthorityService.addMedAuthorityToCollectionIfMissing<IMedAuthority>(medAuthorities, this.resourceAuthority?.medAuthority)
        )
      )
      .subscribe((medAuthorities: IMedAuthority[]) => (this.medAuthoritiesSharedCollection = medAuthorities));

    this.resourceService
      .query({size: 1000})
      .pipe(map((res: HttpResponse<IResource[]>) => res.body ?? []))
      .pipe(
        map((resources: IResource[]) =>
          this.resourceService.addResourceToCollectionIfMissing<IResource>(resources, this.resourceAuthority?.resource)
        )
      )
      .subscribe((resources: IResource[]) => (this.resourcesSharedCollection = resources));
  }
}
