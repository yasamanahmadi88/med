import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import { ModuleFormService, ModuleFormGroup } from './module-form.service';
import { IModule } from '../module.model';
import { ModuleService } from '../service/module.service';

@Component({
  selector: 'jhi-module-update',
  templateUrl: './module-update.component.html',
  standalone: false,
})
export class ModuleUpdateComponent implements OnInit {
  isSaving = false;
  module: IModule | null = null;

  loggingModes: String[] = ['OFF', 'ERRORS', 'TOTAL', 'WHITE_LIST_MSG_TYPE', 'BLACK_LIST_MSG_TYPE', 'WHITE_LIST_LOG_TYPE', 'BLACK_LIST_LOG_TYPE'];

  editForm: ModuleFormGroup = this.moduleFormService.createModuleFormGroup();

  constructor(
    protected moduleService: ModuleService,
    protected moduleFormService: ModuleFormService,
    protected activatedRoute: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ module }) => {
      this.module = module;
      if(module.status === 0){
        module.status = true;
      }else if(module.status === 1){
        module.status = false;
      }
      if (module) {
        this.updateForm(module);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const module = this.moduleFormService.getModule(this.editForm);
    if(module.status === true){
      module.status = 0;
    }else if(module.status === false || module.status == null){
      module.status = 1;
    }
    debugger;
    if (module.id !== null) {
      this.subscribeToSaveResponse(this.moduleService.update(module));
    } else {
      this.subscribeToSaveResponse(this.moduleService.create(module));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IModule>>): void {
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

  protected updateForm(module: IModule): void {
    this.module = module;
    this.moduleFormService.resetForm(this.editForm, module);
  }

  checkboxChange(event: any) {
    console.log(event?.target?.checked);
  }
}
