import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BehaviorSubject } from 'rxjs';

import { BPMN_EDITOR_HOST, BpmnEditorHost } from '../../services/bpmn-editor-host';
import { BpmnEditorService } from '../../services/bpmn-editor.service';
import { ToolbarComponent } from './toolbar.component';

describe('ToolbarComponent', () => {
  let fixture: ComponentFixture<ToolbarComponent>;
  let component: ToolbarComponent;

  const canvas = {
    zoom: vi.fn(),
  };
  const commandStack = {
    canRedo: vi.fn(),
    canUndo: vi.fn(),
    clear: vi.fn(),
    redo: vi.fn(),
    undo: vi.fn(),
  };
  const modeler = {
    createDiagram: vi.fn(),
    get: vi.fn(),
    off: vi.fn(),
    on: vi.fn(),
    saveXML: vi.fn(),
  };
  const modelerSubject = new BehaviorSubject<unknown>(modeler);
  const editorService = {
    bpmnModeler$: modelerSubject.asObservable(),
    getBpmnModeler: vi.fn(),
    setProcessXml: vi.fn(),
  };
  const host: BpmnEditorHost = {
    cancel: vi.fn(),
    save: vi.fn(),
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    modelerSubject.next(modeler);
    canvas.zoom.mockImplementation((scale?: number | string) => (scale === undefined ? 0.7 : Number(scale)));
    commandStack.canUndo.mockReturnValue(false);
    commandStack.canRedo.mockReturnValue(false);
    modeler.get.mockImplementation((service: string) => (service === 'canvas' ? canvas : commandStack));
    modeler.createDiagram.mockResolvedValue({});
    modeler.saveXML.mockResolvedValue({ xml: '<definitions id="diagram" />' });
    editorService.getBpmnModeler.mockReturnValue(modeler);

    await TestBed.configureTestingModule({
      imports: [ToolbarComponent],
      providers: [
        { provide: BpmnEditorService, useValue: editorService },
        { provide: BPMN_EDITOR_HOST, useValue: host },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ToolbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    fixture.destroy();
  });

  it('shows the Vue portal primary actions and removes the temporary import/export controls', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent;

    expect(text).toContain('Save');
    expect(text).toContain('Preview as XML');
    expect(text).toContain('Cancel');
    expect(text).not.toContain('Import');
    expect(text).not.toContain('Export');
  });

  it('serializes and saves through the editor host', async () => {
    await component.onSave();

    expect(modeler.saveXML).toHaveBeenCalledWith({ format: true, preamble: false });
    expect(editorService.setProcessXml).toHaveBeenCalledWith('<definitions id="diagram" />');
    expect(host.save).toHaveBeenCalledWith('<definitions id="diagram" />');
  });

  it('opens a formatted XML preview and closes it with Escape', async () => {
    await component.onPreviewXml();
    fixture.detectChanges();

    const dialog: HTMLElement | null = fixture.nativeElement.querySelector('[data-cy="bpmnXmlPreviewDialog"]');
    expect(modeler.saveXML).toHaveBeenCalledWith({ format: true, preamble: true });
    expect(dialog?.textContent).toContain('<definitions id="diagram" />');

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[data-cy="bpmnXmlPreviewDialog"]')).toBeNull();
  });

  it('delegates Cancel to the editor host', () => {
    component.onCancel();

    expect(host.cancel).toHaveBeenCalledOnce();
  });

  it('tracks canvas zoom and exposes zoom out, reset, and zoom in', () => {
    expect(component.zoomPercentage).toBe('70%');

    component.zoomOut();
    expect(canvas.zoom).toHaveBeenLastCalledWith(0.6, { x: 0, y: 0 });

    component.fitViewport();
    expect(canvas.zoom).toHaveBeenLastCalledWith('fit-viewport');

    component.zoomIn();
    expect(canvas.zoom).toHaveBeenLastCalledWith(0.7, { x: 0, y: 0 });

    const viewboxHandler = modeler.on.mock.calls.find(call => call[0] === 'canvas.viewbox.changed')?.[1];
    viewboxHandler?.({ viewbox: { scale: 1.25 } });
    expect(component.zoomPercentage).toBe('120%');
  });

  it('executes undo and redo only when the command stack permits it', () => {
    component.onUndo();
    component.onRedo();
    expect(commandStack.undo).not.toHaveBeenCalled();
    expect(commandStack.redo).not.toHaveBeenCalled();

    commandStack.canUndo.mockReturnValue(true);
    commandStack.canRedo.mockReturnValue(true);
    component.onUndo();
    component.onRedo();

    expect(commandStack.undo).toHaveBeenCalledOnce();
    expect(commandStack.redo).toHaveBeenCalledOnce();
  });

  it('clears command history, creates a blank diagram, and publishes its XML on restart', async () => {
    await component.onRestart();

    expect(commandStack.clear).toHaveBeenCalledOnce();
    expect(modeler.createDiagram).toHaveBeenCalledOnce();
    expect(editorService.setProcessXml).toHaveBeenCalledWith('<definitions id="diagram" />');
  });
});
