import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import { FlowFormService, FlowFormGroup } from './flow-form.service';
import { IFlow } from '../flow.model';
import { FlowService } from '../service/flow.service';
import { IProduct } from 'app/entities/product/product.model';
import { ProductService } from 'app/entities/product/service/product.service';
import {ToastrService} from "ngx-toastr";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'jhi-flow-update',
  templateUrl: './flow-update.component.html',
  standalone: false,
})
export class FlowUpdateComponent implements OnInit {
  isSaving = false;
  flow: IFlow | null = null;

  productsSharedCollection: IProduct[] = [];

  editForm: FlowFormGroup = this.flowFormService.createFlowFormGroup();

  constructor(
    protected flowService: FlowService,
    protected flowFormService: FlowFormService,
    protected productService: ProductService,
    protected activatedRoute: ActivatedRoute,
    private toastr: ToastrService,
    private translateService: TranslateService
  ) {}

  compareProduct = (o1: IProduct | null, o2: IProduct | null): boolean => this.productService.compareProduct(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ flow }) => {
      this.flow = flow;
      if (flow) {
        this.updateForm(flow);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const flow = this.flowFormService.getFlow(this.editForm);

    this.flowService.isFlowNameValid(flow).subscribe(value => {
      let flows = value.body;
      if(flows.length > 0 && flows[0].flowName != flow.flowName){
        this.toastr.error(this.translateService.instant("medPortalApp.flow.invalidFlowName"));
        this.isSaving = false;
      } else {
        if (flow.id !== null) {
          this.subscribeToSaveResponse(this.flowService.update(flow));
        } else {
          this.subscribeToSaveResponse(this.flowService.create(flow));
        }
      }
    });
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IFlow>>): void {
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

  protected updateForm(flow: IFlow): void {
    this.flow = flow;
    this.flowFormService.resetForm(this.editForm, flow);

    this.productsSharedCollection = this.productService.addProductToCollectionIfMissing<IProduct>(
      this.productsSharedCollection,
      flow.product
    );
  }

  protected loadRelationshipsOptions(): void {
    this.productService
      .query()
      .pipe(map((res: HttpResponse<IProduct[]>) => res.body ?? []))
      .pipe(map((products: IProduct[]) => this.productService.addProductToCollectionIfMissing<IProduct>(products, this.flow?.product)))
      .subscribe((products: IProduct[]) => (this.productsSharedCollection = products));
  }
}
