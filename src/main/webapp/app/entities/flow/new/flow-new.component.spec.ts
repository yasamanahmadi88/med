import { vi } from 'vitest';

import { FlowFormService } from '../update/flow-form.service';
import { FlowService } from '../service/flow.service';
import { ProductService } from '../../product/service/product.service';
import { FlowNewComponent } from './flow-new.component';

describe('FlowNewComponent product-first lifecycle', () => {
  let flowService: {
    xmlTemp: string;
    productTemp: any;
  };

  let productService: {
    compareProduct: ReturnType<typeof vi.fn>;
  };

  let activatedRoute: {
    snapshot: {
      queryParams: Record<string, unknown>;
    };
  };

  let router: {
    navigate: ReturnType<typeof vi.fn>;
  };

  let toastr: {
    error: ReturnType<typeof vi.fn>;
  };

  let translateService: {
    instant: ReturnType<typeof vi.fn>;
  };

  let component: FlowNewComponent;

  beforeEach(() => {
    flowService = {
      xmlTemp: '',
      productTemp: null,
    };

    productService = {
      compareProduct: vi.fn(),
    };

    activatedRoute = {
      snapshot: {
        queryParams: {},
      },
    };

    router = {
      navigate: vi.fn().mockResolvedValue(true),
    };

    toastr = {
      error: vi.fn(),
    };

    translateService = {
      instant: vi.fn((key: string) => key),
    };

    component = new FlowNewComponent(
      flowService as unknown as FlowService,
      new FlowFormService(),
      productService as unknown as ProductService,
      activatedRoute as any,
      router as any,
      activatedRoute as any,
      toastr as any,
      translateService as any,
    );
  });

  it('does not open the BPMN editor without a selected product', () => {
    const productControl = component.editForm.get('product');

    expect(productControl?.value).toBeNull();

    component.openBPMNPage();

    expect(productControl?.touched).toBe(true);
    expect(toastr.error).toHaveBeenCalledWith('Please select a product before opening the BPMN editor.');
    expect(router.navigate).not.toHaveBeenCalled();
    expect(flowService.productTemp).toBeNull();
    expect(component.isGoingToBPMNPage).toBe(false);
  });

  it('parks the selected product before navigating to its BPMN editor', () => {
    const product = { id: 7, productName: 'MCI' };

    component.editForm.patchValue({ product });

    router.navigate.mockImplementationOnce(() => {
      expect(flowService.productTemp).toBe(product);
      expect(component.isGoingToBPMNPage).toBe(true);
    });

    component.openBPMNPage();

    expect(router.navigate).toHaveBeenCalledOnce();
    expect(router.navigate).toHaveBeenCalledWith(['/bpmn-editor'], {
      relativeTo: activatedRoute,
      queryParams: { productId: 7 },
    });
  });

  it('clears draft XML and product state when leaving the create flow normally', () => {
    flowService.xmlTemp = '<definitions />';
    flowService.productTemp = { id: 7 };

    component.isGoingToBPMNPage = false;

    component.ngOnDestroy();

    expect(flowService.xmlTemp).toBe('');
    expect(flowService.productTemp).toBeNull();
  });

  it('preserves draft XML and product state while navigating to the BPMN editor', () => {
    const product = { id: 7, productName: 'MCI' };

    flowService.xmlTemp = '<definitions />';
    flowService.productTemp = product;

    component.isGoingToBPMNPage = true;

    component.ngOnDestroy();

    expect(flowService.xmlTemp).toBe('<definitions />');
    expect(flowService.productTemp).toBe(product);
  });
});
