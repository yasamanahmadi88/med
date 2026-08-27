import { Component, OnInit } from '@angular/core';
import {FlowFormGroup, FlowFormService} from "../update/flow-form.service";
import {FlowService} from "../service/flow.service";
import {Observable} from "rxjs";
import {HttpResponse} from "@angular/common/http";
import {IFlow} from "../flow.model";
import {finalize, map} from "rxjs/operators";
import {IProduct} from "../../product/product.model";
import {ProductService} from "../../product/service/product.service";
import {ActivatedRoute, Router} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'jhi-new',
  templateUrl: './flow-new.component.html',
  styleUrls: ['./flow-new.component.scss']
})
export class FlowNewComponent implements OnInit {

  isSaving = false;
  editForm: FlowFormGroup = this.flowFormService.createFlowFormGroup();
  productsSharedCollection: IProduct[] = [];
  flow: IFlow | any = null;

  bpmnXml: any;
  isGoingToBPMNPage = false;

  productName: any;

  constructor(protected flowService: FlowService,
              protected flowFormService: FlowFormService,
              protected productService: ProductService,
              protected activatedRoute: ActivatedRoute,
              public router: Router,
              public route: ActivatedRoute,
              private toastr: ToastrService,
              private translateService: TranslateService) { }

  compareProduct = (o1: IProduct | null, o2: IProduct | null): boolean => this.productService.compareProduct(o1, o2);

  ngOnInit(): void {
    this.loadRelationshipsOptions();
    this.bpmnXml = this.flowService.xmlTemp;
    this.editForm.patchValue({flow:this.bpmnXml});

    if(this.flowService.xmlTemp == "") this.openBPMNPage();
    else if(this.flowService.xmlTemp == " ") window.history.back();

    if(this.route.snapshot.queryParams["productId"]) {
      this.productName = " ";
      this.productService.find(this.route.snapshot.queryParams["productId"]).subscribe(value => {
        this.productName = value.body?.productName;
        this.editForm.patchValue({product:value.body});
      });
    }
  }

  ngOnDestroy() : void {
    if(!this.isGoingToBPMNPage)
      this.flowService.xmlTemp = "";
  }

  save(): void {
    this.isSaving = true;
    const flow = this.flowFormService.getFlow(this.editForm);

    this.flowService.isFlowNameValid(flow.flowName).subscribe(value => {
      if(value.body.length > 0){
        this.toastr.error(this.translateService.instant("medPortalApp.flow.invalidFlowName"));
        this.isSaving = false;
      } else {
        if(flow.id === null)
          this.subscribeToSaveResponse(this.flowService.create(flow));
      }
    });
  }

  openBPMNPage(){
    this.isGoingToBPMNPage = true;
    this.router.navigate(['/bpmn'], {relativeTo: this.activatedRoute});
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

  previousState(): void {
    window.history.back();
  }

  protected loadRelationshipsOptions(): void {
    this.productService
      .query()
      .pipe(map((res: HttpResponse<IProduct[]>) => res.body ?? []))
      .pipe(map((products: IProduct[]) => this.productService.addProductToCollectionIfMissing<IProduct>(products, this.flow?.product)))
      .subscribe((products: IProduct[]) => (this.productsSharedCollection = products));
  }

}
