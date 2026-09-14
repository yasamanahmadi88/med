import { TestBed, ComponentFixture } from '@angular/core/testing';
import { NEVER } from 'rxjs';
import { BpmnPropertiesPanelModule, BpmnPropertiesProviderModule, CamundaPlatformPropertiesProviderModule } from 'bpmn-js-properties-panel';

import { BpmnEditorComponent } from './bpmn-editor.component';
import { PanelComponent } from './panel/panel.component';
import { ModulePanelComponent } from './module-panel/module-panel.component';
import { BpmnEditorService } from '../services/bpmn-editor.service';
import { additionalModulesFor } from '../additional-modules';
import { DEFAULT_ELEMENT_SIZES } from '../additional-modules/ElementFactory';
import { RoleModulesService } from '../services/role-modules.service';

/**
 * bpmn-js renders through the SVG DOM, which jsdom does not implement, so the modeler is stubbed
 * here and these specs cover the wiring this component owns: which modules get registered, what
 * the properties panel is pointed at, and what happens when there is no diagram to load.
 * Rendering itself is covered by the Playwright suite, which runs in a real browser.
 */
const { created } = vi.hoisted(() => ({ created: [] as any[] }));

vi.mock('bpmn-js/lib/Modeler', () => ({
  default: class {
    options: any;
    createDiagram = vi.fn();
    importXML = vi.fn().mockResolvedValue({});
    saveXML = vi.fn().mockResolvedValue({ xml: '<definitions />' });
    on = vi.fn();
    private readonly eventBus = { on: vi.fn() };
    get = vi.fn((service: string) => (service === 'eventBus' ? this.eventBus : undefined));
    destroy = vi.fn();

    constructor(options: any) {
      this.options = options;
      created.push(this);
    }
  },
}));

/** DesignerComponent defers modeler creation by a macrotask; let that run. */
const flushMacrotasks = (): Promise<void> => new Promise(resolve => setTimeout(resolve, 0));

describe('BpmnEditorComponent', () => {
  let fixture: ComponentFixture<BpmnEditorComponent>;
  let service: BpmnEditorService;

  beforeEach(async () => {
    created.length = 0;

    await TestBed.configureTestingModule({
      imports: [BpmnEditorComponent],
      providers: [
        {
          provide: RoleModulesService,
          useValue: { getAvailableModuleTypes: () => NEVER },
        },
      ],
    }).compileComponents();

    service = TestBed.inject(BpmnEditorService);
    fixture = TestBed.createComponent(BpmnEditorComponent);
    fixture.detectChanges();
    await flushMacrotasks();
  });

  afterEach(() => {
    fixture.destroy();
  });

  it('prevents the native context menu only inside the BPMN designer', () => {
    const designer: HTMLElement = fixture.nativeElement.querySelector('jhi-designer');

    const designerEvent = new MouseEvent('contextmenu', {
      bubbles: true,
      cancelable: true,
    });

    designer.dispatchEvent(designerEvent);

    expect(designerEvent.defaultPrevented).toBe(true);

    const outsideEvent = new MouseEvent('contextmenu', {
      bubbles: true,
      cancelable: true,
    });

    document.body.dispatchEvent(outsideEvent);

    expect(outsideEvent.defaultPrevented).toBe(false);
  });

  it('uses the Vue-compatible module panel in the default custom-panel mode', () => {
    const modulePanel = fixture.debugElement.query(el => el.componentInstance instanceof ModulePanelComponent);
    const genericPanel = fixture.debugElement.query(el => el.componentInstance instanceof PanelComponent);

    expect(modulePanel).toBeTruthy();
    expect(genericPanel).toBeNull();
    expect(service.getPropertiesPanelParent()).toBeNull();
  });

  it('prevents the native context menu only inside the BPMN designer', () => {
    const designer: HTMLElement = fixture.nativeElement.querySelector('jhi-designer');

    const designerEvent = new MouseEvent('contextmenu', {
      bubbles: true,
      cancelable: true,
    });

    designer.dispatchEvent(designerEvent);

    expect(designerEvent.defaultPrevented).toBe(true);

    const outsideEvent = new MouseEvent('contextmenu', {
      bubbles: true,
      cancelable: true,
    });

    document.body.dispatchEvent(outsideEvent);

    expect(outsideEvent.defaultPrevented).toBe(false);
  });

  it('creates exactly one modeler and publishes it on the service', () => {
    expect(created).toHaveLength(1);
    expect(service.getBpmnModeler()).toBe(created[0]);
  });

  it('prevents the native context menu only inside the BPMN designer', () => {
    const designer: HTMLElement = fixture.nativeElement.querySelector('jhi-designer');

    const designerEvent = new MouseEvent('contextmenu', {
      bubbles: true,
      cancelable: true,
    });

    designer.dispatchEvent(designerEvent);

    expect(designerEvent.defaultPrevented).toBe(true);

    const outsideEvent = new MouseEvent('contextmenu', {
      bubbles: true,
      cancelable: true,
    });

    document.body.dispatchEvent(outsideEvent);

    expect(outsideEvent.defaultPrevented).toBe(false);
  });

  it('does not mount the generic bpmn-js properties panel in custom mode', () => {
    expect(created[0].options.propertiesPanel).toBeUndefined();
    expect(created[0].options.additionalModules).not.toContain(BpmnPropertiesPanelModule);
    expect(created[0].options.additionalModules).not.toContain(BpmnPropertiesProviderModule);
    expect(created[0].options.additionalModules).not.toContain(CamundaPlatformPropertiesProviderModule);
  });

  it('prevents the native context menu only inside the BPMN designer', () => {
    const designer: HTMLElement = fixture.nativeElement.querySelector('jhi-designer');

    const designerEvent = new MouseEvent('contextmenu', {
      bubbles: true,
      cancelable: true,
    });

    designer.dispatchEvent(designerEvent);

    expect(designerEvent.defaultPrevented).toBe(true);

    const outsideEvent = new MouseEvent('contextmenu', {
      bubbles: true,
      cancelable: true,
    });

    document.body.dispatchEvent(outsideEvent);

    expect(outsideEvent.defaultPrevented).toBe(false);
  });

  it('keeps the generic properties panel available for non-custom panel modes', async () => {
    fixture.destroy();
    created.length = 0;
    service.updateConfiguration({ penalMode: 'default' });

    fixture = TestBed.createComponent(BpmnEditorComponent);
    fixture.detectChanges();
    await flushMacrotasks();

    const panel = fixture.debugElement.query(el => el.componentInstance instanceof PanelComponent);
    expect(panel).toBeTruthy();
    expect(service.getPropertiesPanelParent()).toBe(panel.nativeElement.querySelector('.panel-content'));
    expect(created[0].options.propertiesPanel).toEqual({ parent: service.getPropertiesPanelParent() });
    expect(created[0].options.additionalModules).toContain(BpmnPropertiesPanelModule);
    expect(created[0].options.additionalModules).toContain(BpmnPropertiesProviderModule);
    expect(created[0].options.additionalModules).toContain(CamundaPlatformPropertiesProviderModule);
  });

  it('prevents the native context menu only inside the BPMN designer', () => {
    const designer: HTMLElement = fixture.nativeElement.querySelector('jhi-designer');

    const designerEvent = new MouseEvent('contextmenu', {
      bubbles: true,
      cancelable: true,
    });

    designer.dispatchEvent(designerEvent);

    expect(designerEvent.defaultPrevented).toBe(true);

    const outsideEvent = new MouseEvent('contextmenu', {
      bubbles: true,
      cancelable: true,
    });

    document.body.dispatchEvent(outsideEvent);

    expect(outsideEvent.defaultPrevented).toBe(false);
  });

  it('registers the palette and renderer modules the settings select', () => {
    expect(created[0].options.additionalModules).toEqual(expect.arrayContaining(additionalModulesFor(service.getEditorSettings())));
  });

  it('prevents the native context menu only inside the BPMN designer', () => {
    const designer: HTMLElement = fixture.nativeElement.querySelector('jhi-designer');

    const designerEvent = new MouseEvent('contextmenu', {
      bubbles: true,
      cancelable: true,
    });

    designer.dispatchEvent(designerEvent);

    expect(designerEvent.defaultPrevented).toBe(true);

    const outsideEvent = new MouseEvent('contextmenu', {
      bubbles: true,
      cancelable: true,
    });

    document.body.dispatchEvent(outsideEvent);

    expect(outsideEvent.defaultPrevented).toBe(false);
  });

  it('registers the camunda moddle extension the Camunda provider needs', () => {
    expect(created[0].options.moddleExtensions.camunda).toBeTruthy();
    expect(created[0].options.moddleExtensions.cdrParser).toBeUndefined();
  });

  it('prevents the native context menu only inside the BPMN designer', () => {
    const designer: HTMLElement = fixture.nativeElement.querySelector('jhi-designer');

    const designerEvent = new MouseEvent('contextmenu', {
      bubbles: true,
      cancelable: true,
    });

    designer.dispatchEvent(designerEvent);

    expect(designerEvent.defaultPrevented).toBe(true);

    const outsideEvent = new MouseEvent('contextmenu', {
      bubbles: true,
      cancelable: true,
    });

    document.body.dispatchEvent(outsideEvent);

    expect(outsideEvent.defaultPrevented).toBe(false);
  });

  it('starts an empty diagram carrying the configured process identity', () => {
    // Not `modeler.createDiagram()`, which always names the process `Process_1`: the configured
    // processId and processName are what a flow is keyed on, so a diagram that dropped them
    // would have to be renamed by hand before it could be saved.
    const settings = service.getEditorSettings();
    expect(created[0].createDiagram).not.toHaveBeenCalled();

    const [xml] = created[0].importXML.mock.calls[0];
    expect(xml).toContain(`<bpmn:process id="${settings.processId}" name="${settings.processName}"`);
  });

  it('prevents the native context menu only inside the BPMN designer', () => {
    const designer: HTMLElement = fixture.nativeElement.querySelector('jhi-designer');

    const designerEvent = new MouseEvent('contextmenu', {
      bubbles: true,
      cancelable: true,
    });

    designer.dispatchEvent(designerEvent);

    expect(designerEvent.defaultPrevented).toBe(true);

    const outsideEvent = new MouseEvent('contextmenu', {
      bubbles: true,
      cancelable: true,
    });

    document.body.dispatchEvent(outsideEvent);

    expect(outsideEvent.defaultPrevented).toBe(false);
  });

  it('gives the custom element factory the sizes it exists to apply', () => {
    // CustomElementFactory reads nothing but `config.elementFactory`; without it the class is a
    // no-op and every integration module — all of them `bpmn:Task` subclasses — is placed at
    // bpmn-js's 100x80 rather than the 120x120 the Vue editor configured.
    expect(created[0].options.elementFactory).toEqual(DEFAULT_ELEMENT_SIZES);
    expect(created[0].options.elementFactory['bpmn:Task']).toEqual({ width: 120, height: 120 });
  });

  it('prevents the native context menu only inside the BPMN designer', () => {
    const designer: HTMLElement = fixture.nativeElement.querySelector('jhi-designer');

    const designerEvent = new MouseEvent('contextmenu', {
      bubbles: true,
      cancelable: true,
    });

    designer.dispatchEvent(designerEvent);

    expect(designerEvent.defaultPrevented).toBe(true);

    const outsideEvent = new MouseEvent('contextmenu', {
      bubbles: true,
      cancelable: true,
    });

    document.body.dispatchEvent(outsideEvent);

    expect(outsideEvent.defaultPrevented).toBe(false);
  });

  it('stops suppressing the browser context menu once the editor is gone', () => {
    // The listener is on `document`, so leaving it behind kills right-click across the whole
    // portal — not just here — until a full page reload.
    const rightClick = (): MouseEvent => {
      const event = new MouseEvent('contextmenu', { bubbles: true, cancelable: true });
      document.dispatchEvent(event);
      return event;
    };

    expect(rightClick().defaultPrevented).toBe(true);

    fixture.destroy();

    expect(rightClick().defaultPrevented).toBe(false);
  });

  it('prevents the native context menu only inside the BPMN designer', () => {
    const designer: HTMLElement = fixture.nativeElement.querySelector('jhi-designer');

    const designerEvent = new MouseEvent('contextmenu', {
      bubbles: true,
      cancelable: true,
    });

    designer.dispatchEvent(designerEvent);

    expect(designerEvent.defaultPrevented).toBe(true);

    const outsideEvent = new MouseEvent('contextmenu', {
      bubbles: true,
      cancelable: true,
    });

    document.body.dispatchEvent(outsideEvent);

    expect(outsideEvent.defaultPrevented).toBe(false);
  });

  it('clears the modeler from the service on destroy', () => {
    fixture.destroy();

    expect(created[0].destroy).toHaveBeenCalled();
    expect(service.getBpmnModeler()).toBeNull();
  });
});
