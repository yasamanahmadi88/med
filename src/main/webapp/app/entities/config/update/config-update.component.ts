import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import { ConfigFormService, ConfigFormGroup } from './config-form.service';
import { IConfig } from '../config.model';
import { ConfigService } from '../service/config.service';
import { IModule } from 'app/entities/module/module.model';
import { ModuleService } from 'app/entities/module/service/module.service';

@Component({
  selector: 'jhi-config-update',
  templateUrl: './config-update.component.html',
  standalone: false,
})
export class ConfigUpdateComponent implements OnInit {
  isSaving = false;
  config: IConfig | null = null;

  modulesSharedCollection: IModule[] = [];

  editForm: ConfigFormGroup = this.configFormService.createConfigFormGroup();

  constructor(
    protected configService: ConfigService,
    protected configFormService: ConfigFormService,
    protected moduleService: ModuleService,
    protected activatedRoute: ActivatedRoute,
  ) {}

  compareModule = (o1: IModule | null, o2: IModule | null): boolean => this.moduleService.compareModule(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ config }) => {
      this.config = config;
      if (config) {
        this.updateForm(config);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const config = this.configFormService.getConfig(this.editForm);
    if (config.id !== null) {
      this.subscribeToSaveResponse(this.configService.update(config));
    } else {
      this.subscribeToSaveResponse(this.configService.create(config));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IConfig>>): void {
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

  protected updateForm(config: IConfig): void {
    this.config = config;
    this.configFormService.resetForm(this.editForm, config);

    this.modulesSharedCollection = this.moduleService.addModuleToCollectionIfMissing<IModule>(this.modulesSharedCollection, config.module);
  }

  protected loadRelationshipsOptions(): void {
    this.moduleService
      .query({ size: 1000 })
      .pipe(map((res: HttpResponse<IModule[]>) => res.body ?? []))
      .pipe(map((modules: IModule[]) => this.moduleService.addModuleToCollectionIfMissing<IModule>(modules, this.config?.module)))
      .subscribe((modules: IModule[]) => (this.modulesSharedCollection = modules));
  }
}
