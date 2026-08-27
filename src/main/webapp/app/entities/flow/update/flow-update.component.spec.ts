import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, from } from 'rxjs';

import { FlowFormService } from './flow-form.service';
import { FlowService } from '../service/flow.service';
import { IFlow } from '../flow.model';
import { IProduct } from 'app/entities/product/product.model';
import { ProductService } from 'app/entities/product/service/product.service';

import { FlowUpdateComponent } from './flow-update.component';

describe('Flow Management Update Component', () => {
  let comp: FlowUpdateComponent;
  let fixture: ComponentFixture<FlowUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let flowFormService: FlowFormService;
  let flowService: FlowService;
  let productService: ProductService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([])],
      declarations: [FlowUpdateComponent],
      providers: [
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(FlowUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(FlowUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    flowFormService = TestBed.inject(FlowFormService);
    flowService = TestBed.inject(FlowService);
    productService = TestBed.inject(ProductService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call Product query and add missing value', () => {
      const flow: IFlow = { id: 456 };
      const product: IProduct = { id: 8150 };
      flow.product = product;

      const productCollection: IProduct[] = [{ id: 95036 }];
      jest.spyOn(productService, 'query').mockReturnValue(of(new HttpResponse({ body: productCollection })));
      const additionalProducts = [product];
      const expectedCollection: IProduct[] = [...additionalProducts, ...productCollection];
      jest.spyOn(productService, 'addProductToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ flow });
      comp.ngOnInit();

      expect(productService.query).toHaveBeenCalled();
      expect(productService.addProductToCollectionIfMissing).toHaveBeenCalledWith(
        productCollection,
        ...additionalProducts.map(expect.objectContaining)
      );
      expect(comp.productsSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const flow: IFlow = { id: 456 };
      const product: IProduct = { id: 86669 };
      flow.product = product;

      activatedRoute.data = of({ flow });
      comp.ngOnInit();

      expect(comp.productsSharedCollection).toContain(product);
      expect(comp.flow).toEqual(flow);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IFlow>>();
      const flow = { id: 123 };
      jest.spyOn(flowFormService, 'getFlow').mockReturnValue(flow);
      jest.spyOn(flowService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ flow });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: flow }));
      saveSubject.complete();

      // THEN
      expect(flowFormService.getFlow).toHaveBeenCalled();
      expect(comp.previousState).toHaveBeenCalled();
      expect(flowService.update).toHaveBeenCalledWith(expect.objectContaining(flow));
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IFlow>>();
      const flow = { id: 123 };
      jest.spyOn(flowFormService, 'getFlow').mockReturnValue({ id: null });
      jest.spyOn(flowService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ flow: null });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: flow }));
      saveSubject.complete();

      // THEN
      expect(flowFormService.getFlow).toHaveBeenCalled();
      expect(flowService.create).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<IFlow>>();
      const flow = { id: 123 };
      jest.spyOn(flowService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ flow });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(flowService.update).toHaveBeenCalled();
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Compare relationships', () => {
    describe('compareProduct', () => {
      it('Should forward to productService', () => {
        const entity = { id: 123 };
        const entity2 = { id: 456 };
        jest.spyOn(productService, 'compareProduct');
        comp.compareProduct(entity, entity2);
        expect(productService.compareProduct).toHaveBeenCalledWith(entity, entity2);
      });
    });
  });
});
