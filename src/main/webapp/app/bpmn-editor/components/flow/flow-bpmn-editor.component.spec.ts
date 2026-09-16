import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { NEVER, Subject, of, throwError } from 'rxjs';
import { ToastrService } from 'ngx-toastr';

import { FlowService } from 'app/entities/flow/service/flow.service';
import { BpmnEditorService } from '../../services/bpmn-editor.service';
import { BpmnElementAccessService } from '../../services/bpmn-element-access.service';
import { RoleModulesService } from '../../services/role-modules.service';
import { BPMN_EDITOR_HOST, BpmnEditorHost } from '../../services/bpmn-editor-host';
import { FlowBpmnEditorComponent } from './flow-bpmn-editor.component';

/**
 * These specs pin the contract the Flow form relies on, which the iframe editor this replaced
 * expressed in postMessage handlers: where the diagram is read from on the way in, where it is
 * written on save, and the empty-draft marker cancel leaves behind.
 *
 * bpmn-js renders through the SVG DOM, which jsdom does not implement, so the modeler is stubbed
 * the same way BpmnEditorComponent's own specs stub it.
 */
vi.mock('bpmn-js/lib/Modeler', () => ({
  default: class {
    createDiagram = vi.fn();
    importXML = vi.fn().mockResolvedValue({});
    saveXML = vi.fn().mockResolvedValue({ xml: '<definitions />' });
    on = vi.fn();
    private readonly eventBus = { on: vi.fn() };
    get = vi.fn((service: string) => (service === 'eventBus' ? this.eventBus : undefined));
    destroy = vi.fn();
  },
}));

const diagram = '<?xml version="1.0"?><definitions />';

describe('FlowBpmnEditorComponent', () => {
  let fixture: ComponentFixture<FlowBpmnEditorComponent>;
  let component: FlowBpmnEditorComponent;
  let httpMock: HttpTestingController;
  let flowService: FlowService;
  let editorService: BpmnEditorService;
  let elementAccessService: {
    clear: ReturnType<typeof vi.fn>;
    loadForProduct: ReturnType<typeof vi.fn>;
    findDisallowedXmlElements: ReturnType<typeof vi.fn>;
    currentConfig: ReturnType<typeof vi.fn>;
    isTypeAllowed: ReturnType<typeof vi.fn>;
  };
  let back: ReturnType<typeof vi.spyOn>;
  let queryParams: Record<string, unknown>;

  const create = (): void => {
    fixture = TestBed.createComponent(FlowBpmnEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  };

  beforeEach(() => {
    // A new BPMN editor is opened in the context of a selected product.
    queryParams = { productId: '7' };
    back = vi.spyOn(window.history, 'back').mockImplementation(() => undefined);

    elementAccessService = {
      clear: vi.fn(),
      loadForProduct: vi.fn().mockReturnValue(of({})),
      findDisallowedXmlElements: vi.fn().mockReturnValue([]),
      currentConfig: vi.fn().mockReturnValue({
        allowedTypes: [],
        allowedPaletteActions: [],
        allowedXmlElements: [],
      }),
      isTypeAllowed: vi.fn().mockReturnValue(false),
    };

    TestBed.configureTestingModule({
      imports: [FlowBpmnEditorComponent, HttpClientTestingModule],
      providers: [
        { provide: ActivatedRoute, useValue: { snapshot: { queryParams } } },
        { provide: RoleModulesService, useValue: { getAvailableModuleTypes: () => NEVER } },
        { provide: BpmnElementAccessService, useValue: elementAccessService },
        { provide: ToastrService, useValue: { error: vi.fn() } },
      ],
    });

    httpMock = TestBed.inject(HttpTestingController);
    flowService = TestBed.inject(FlowService);
    editorService = TestBed.inject(BpmnEditorService);
  });

  afterEach(() => {
    httpMock.verify();
    back.mockRestore();
  });

  describe('opened on a saved flow', () => {
    beforeEach(() => {
      queryParams['flowId'] = '7';
      create();
      httpMock.expectOne({ method: 'GET', url: 'api/flows/7' }).flush({
        id: 7,
        flowName: 'f',
        flow: diagram,
        product: { id: 11 },
      });
      fixture.detectChanges();
    });

    it('seeds the editor from the flow it fetched', () => {
      expect(editorService.getProcessXml()).toBe(diagram);
    });

    it('writes the diagram back onto that flow on save', () => {
      component.save('<definitions id="edited" />');

      const request = httpMock.expectOne({ method: 'PUT', url: 'api/flows/7' });
      expect(request.request.body).toMatchObject({ id: 7, flowName: 'f', flow: '<definitions id="edited" />' });
      request.flush({});
      expect(back).toHaveBeenCalled();
    });

    it('leaves the flow untouched on cancel', () => {
      component.cancel();

      expect(back).toHaveBeenCalled();
      // No PUT — httpMock.verify() in afterEach is the assertion.
    });
  });

  describe('opened on an unsaved draft', () => {
    it('seeds the editor from the draft the new-flow form parked on the service', () => {
      flowService.xmlTemp = diagram;
      create();

      expect(editorService.getProcessXml()).toBe(diagram);
    });

    it('starts an empty diagram when there is no draft yet', () => {
      flowService.xmlTemp = '';
      create();

      expect(editorService.getProcessXml()).toBeUndefined();
    });

    it('stashes the diagram back on the service on save, with no request of its own', () => {
      flowService.xmlTemp = '';
      create();

      component.save(diagram);

      expect(flowService.xmlTemp).toBe(diagram);
      expect(back).toHaveBeenCalled();
    });

    it('marks an empty draft as cancelled so the new-flow form does not bounce back here', () => {
      // FlowNewComponent reads '' as "editor never opened" and navigates straight into it, so
      // cancelling out of an empty draft has to leave something else behind.
      flowService.xmlTemp = '';
      create();

      component.cancel();

      expect(flowService.xmlTemp).toBe(' ');
      expect(back).toHaveBeenCalled();
    });

    it('keeps a non-empty draft as it is on cancel', () => {
      flowService.xmlTemp = diagram;
      create();

      component.cancel();

      expect(flowService.xmlTemp).toBe(diagram);
    });
  });

  describe('element access gate', () => {
    it('waits for product access before scanning and seeding a draft', () => {
      const accessResult = new Subject<unknown>();

      elementAccessService.loadForProduct.mockReturnValueOnce(accessResult);
      flowService.xmlTemp = diagram;

      create();

      expect(elementAccessService.loadForProduct).toHaveBeenCalledOnce();
      expect(elementAccessService.loadForProduct).toHaveBeenCalledWith(7);

      expect(elementAccessService.findDisallowedXmlElements).not.toHaveBeenCalled();
      expect(editorService.getProcessXml()).toBeUndefined();
      expect(component.seeded).toBe(false);

      accessResult.next({});
      accessResult.complete();

      expect(elementAccessService.findDisallowedXmlElements).toHaveBeenCalledOnce();
      expect(elementAccessService.findDisallowedXmlElements).toHaveBeenCalledWith(diagram);

      expect(editorService.getProcessXml()).toBe(diagram);
      expect(component.seeded).toBe(true);
      expect(component.accessError).toBeNull();
    });

    it('does not seed a diagram containing BPMN elements that are not allowed for the product', () => {
      elementAccessService.findDisallowedXmlElements.mockReturnValueOnce([
        {
          namespaceUri: 'http://www.omg.org/spec/BPMN/20100524/MODEL',
          localName: 'startEvent',
        },
      ]);

      flowService.xmlTemp = diagram;

      create();

      expect(elementAccessService.loadForProduct).toHaveBeenCalledWith(7);
      expect(elementAccessService.findDisallowedXmlElements).toHaveBeenCalledWith(diagram);

      expect(editorService.getProcessXml()).toBeUndefined();
      expect(component.seeded).toBe(false);

      expect(component.accessError).toBe(
        'This diagram contains 1 BPMN element type(s) that are not allowed for the selected product.',
      );
    });

    it('does not seed malformed or unsafe BPMN XML', () => {
      elementAccessService.findDisallowedXmlElements.mockImplementationOnce(() => {
        throw new Error('Invalid or unsafe BPMN XML');
      });

      flowService.xmlTemp = diagram;

      create();

      expect(elementAccessService.loadForProduct).toHaveBeenCalledWith(7);
      expect(elementAccessService.findDisallowedXmlElements).toHaveBeenCalledWith(diagram);

      expect(editorService.getProcessXml()).toBeUndefined();
      expect(component.seeded).toBe(false);
      expect(component.accessError).toBe('The BPMN XML is invalid.');
    });

    it('rejects an explicitly invalid productId instead of falling back to stale product state', () => {
      queryParams['productId'] = 'not-a-product-id';

      flowService.productTemp = { id: 99 } as any;
      flowService.xmlTemp = diagram;

      create();

      expect(elementAccessService.loadForProduct).not.toHaveBeenCalled();
      expect(elementAccessService.findDisallowedXmlElements).not.toHaveBeenCalled();

      expect(editorService.getProcessXml()).toBeUndefined();
      expect(component.seeded).toBe(false);

      expect(component.accessError).toBe(
        'Select a product before opening the BPMN editor.',
      );
    });
    it('does not scan or seed when product element permissions cannot be loaded', () => {
      elementAccessService.loadForProduct.mockReturnValueOnce(
        throwError(() => new Error('permission lookup failed')),
      );

      flowService.xmlTemp = diagram;

      create();

      expect(elementAccessService.loadForProduct).toHaveBeenCalledWith(7);
      expect(elementAccessService.findDisallowedXmlElements).not.toHaveBeenCalled();

      expect(editorService.getProcessXml()).toBeUndefined();
      expect(component.seeded).toBe(false);

      expect(component.accessError).toBe(
        'BPMN element permissions could not be loaded for the selected product.',
      );
    });
  });
  it('is the host the toolbar saves and cancels through', () => {
    create();

    const host = fixture.debugElement.injector.get<BpmnEditorHost>(BPMN_EDITOR_HOST);
    expect(host).toBe(component);
  });

  it('clears the diagram on destroy so the next flow does not inherit it', () => {
    flowService.xmlTemp = diagram;
    create();
    fixture.destroy();

    expect(editorService.getProcessXml()).toBeUndefined();
  });
});
