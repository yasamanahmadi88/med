import { TestBed, ComponentFixture } from '@angular/core/testing';
import { BpmnPropertiesPanelModule, BpmnPropertiesProviderModule, CamundaPlatformPropertiesProviderModule } from 'bpmn-js-properties-panel';

import { BpmnEditorComponent } from './bpmn-editor.component';
import { PanelComponent } from './panel/panel.component';
import { BpmnEditorService } from '../services/bpmn-editor.service';
import { additionalModulesFor } from '../additional-modules';

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
    get = vi.fn();
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
    }).compileComponents();

    service = TestBed.inject(BpmnEditorService);
    fixture = TestBed.createComponent(BpmnEditorComponent);
    fixture.detectChanges();
    await flushMacrotasks();
  });

  afterEach(() => {
    fixture.destroy();
  });

  it('hands the properties panel element from PanelComponent to the service', () => {
    const panel = fixture.debugElement.query(el => el.componentInstance instanceof PanelComponent);
    expect(panel).toBeTruthy();

    expect(service.getPropertiesPanelParent()).toBe(panel.nativeElement.querySelector('.editor-properties-panel__content'));
  });

  it('uses editor-specific properties-panel classes that do not collide with the legacy Vue styles', () => {
    const panel: HTMLElement = fixture.nativeElement.querySelector('jhi-panel');

    expect(panel.querySelector('.editor-properties-panel')).toBeTruthy();
    expect(panel.querySelector('.editor-properties-panel__title')?.textContent?.trim()).toBe('Properties');
    expect(panel.querySelector('.panel-header')).toBeNull();
  });

  it('keeps the properties panel mounted while toggling its collapsed layout', () => {
    const toggle: HTMLButtonElement = fixture.nativeElement.querySelector('[data-cy="bpmnPropertiesToggle"]');
    const panelContent = service.getPropertiesPanelParent();

    toggle.click();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('#designer-container').classList).toContain('properties-panel-collapsed');
    expect(toggle.getAttribute('aria-expanded')).toBe('false');
    expect(service.getPropertiesPanelParent()).toBe(panelContent);

    toggle.click();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('#designer-container').classList).not.toContain('properties-panel-collapsed');
    expect(toggle.getAttribute('aria-expanded')).toBe('true');
    expect(service.getPropertiesPanelParent()).toBe(panelContent);
  });

  it('creates exactly one modeler and publishes it on the service', () => {
    expect(created).toHaveLength(1);
    expect(service.getBpmnModeler()).toBe(created[0]);
  });

  it('points the properties panel at the element PanelComponent registered', () => {
    expect(created[0].options.propertiesPanel).toEqual({ parent: service.getPropertiesPanelParent() });
  });

  it('registers the properties panel and provider modules', () => {
    // Alongside whatever palette and renderer modules the settings select, which
    // additional-modules/index.spec.ts covers.
    expect(created[0].options.additionalModules).toContain(BpmnPropertiesPanelModule);
    expect(created[0].options.additionalModules).toContain(BpmnPropertiesProviderModule);
    expect(created[0].options.additionalModules).toContain(CamundaPlatformPropertiesProviderModule);
  });

  it('registers the palette and renderer modules the settings select', () => {
    expect(created[0].options.additionalModules).toEqual(expect.arrayContaining(additionalModulesFor(service.getEditorSettings())));
  });

  it('registers the BPMN runtime translation provider', () => {
    const translationModule = created[0].options.additionalModules.find(
      (module: { translate?: unknown[] }) => module.translate?.[0] === 'value',
    );

    expect(translationModule).toBeTruthy();

    const translate = translationModule.translate[1] as (template: string, replacements?: Record<string, unknown>) => string;

    expect(translate('Task')).toBe('Task');
    expect(translate('Create {type}', { type: 'StartEvent' })).toBe('Create StartEvent');
  });

  it('registers the camunda moddle extension the Camunda provider needs', () => {
    expect(created[0].options.moddleExtensions.camunda).toBeTruthy();
    expect(created[0].options.moddleExtensions.cdrParser).toBeUndefined();
  });

  it('starts an empty diagram from generated XML when no xml is supplied', () => {
    expect(created[0].createDiagram).not.toHaveBeenCalled();
    expect(created[0].importXML).toHaveBeenCalledTimes(1);
  });

  it('clears the modeler from the service on destroy', () => {
    fixture.destroy();

    expect(created[0].destroy).toHaveBeenCalled();
    expect(service.getBpmnModeler()).toBeNull();
  });

  it('does not expose the settings control that the Vue portal kept hidden', () => {
    expect(fixture.nativeElement.querySelector('jhi-settings')).toBeNull();
  });

  it('does not suppress the native context menu outside the BPMN canvas', () => {
    const editor: HTMLElement = fixture.nativeElement.querySelector('#designer-container');
    const editorEvent = new MouseEvent('contextmenu', { bubbles: true, cancelable: true });
    const outsideEvent = new MouseEvent('contextmenu', { bubbles: true, cancelable: true });

    editor.dispatchEvent(editorEvent);
    document.body.dispatchEvent(outsideEvent);

    expect(editorEvent.defaultPrevented).toBe(false);
    expect(outsideEvent.defaultPrevented).toBe(false);
  });
});
