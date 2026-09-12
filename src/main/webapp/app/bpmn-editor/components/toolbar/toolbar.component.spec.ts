import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

import { ToolbarComponent } from './toolbar.component';
import { XmlPreviewDialogComponent } from './xml-preview-dialog.component';
import { ShortcutKeysDialogComponent } from './shortcut-keys-dialog.component';
import { CustomIconsDialogComponent } from './custom-icons-dialog.component';
import { BpmnEditorService } from '../../services/bpmn-editor.service';
import { BPMN_EDITOR_HOST, BpmnEditorHost } from '../../services/bpmn-editor-host';

/**
 * bpmn-js renders through the SVG DOM, which jsdom does not implement, so the modeler is a stub
 * and these specs cover what the toolbar itself decides: which service it reaches for, what it
 * does when there is no modeler yet, and the zoom arithmetic. The buttons doing something
 * visible on a real canvas is covered by the Playwright suite.
 */
describe('ToolbarComponent', () => {
  let fixture: ComponentFixture<ToolbarComponent>;
  let component: ToolbarComponent;
  let service: BpmnEditorService;
  let modal: NgbModal;
  let host: BpmnEditorHost;

  let canvas: { zoom: ReturnType<typeof vi.fn> };
  let commandStack: {
    undo: ReturnType<typeof vi.fn>;
    redo: ReturnType<typeof vi.fn>;
    clear: ReturnType<typeof vi.fn>;
    canUndo: ReturnType<typeof vi.fn>;
    canRedo: ReturnType<typeof vi.fn>;
  };
  let minimap: { toggle: ReturnType<typeof vi.fn> };
  let customIcons: { getIcons: ReturnType<typeof vi.fn> };
  let modeler: any;
  let elements: any[];
  let viewboxListener: ((event: { viewbox: { scale: number } }) => void) | undefined;
  let commandStackListener: (() => void) | undefined;

  beforeEach(async () => {
    canvas = { zoom: vi.fn().mockReturnValue(1) };
    commandStack = {
      undo: vi.fn(),
      redo: vi.fn(),
      clear: vi.fn(),
      canUndo: vi.fn().mockReturnValue(true),
      canRedo: vi.fn().mockReturnValue(true),
    };
    minimap = { toggle: vi.fn() };
    customIcons = { getIcons: vi.fn(() => []) };
    viewboxListener = undefined;
    commandStackListener = undefined;

    elements = [];

    modeler = {
      get: vi.fn((name: string) => {
        if (name === 'canvas') return canvas;
        if (name === 'commandStack') return commandStack;
        if (name === 'minimap') return minimap;
        if (name === 'customIcons') return customIcons;
        if (name === 'elementRegistry') return { getAll: () => elements };
        return undefined;
      }),
      on: vi.fn((event: string, listener: any) => {
        if (event === 'canvas.viewbox.changed') {
          viewboxListener = listener;
        }
        if (event === 'commandStack.changed') {
          commandStackListener = listener;
        }
      }),
      saveXML: vi.fn().mockResolvedValue({ xml: '<definitions />' }),
      importXML: vi.fn().mockResolvedValue({}),
    };

    host = { save: vi.fn(), cancel: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [ToolbarComponent],
      providers: [{ provide: BPMN_EDITOR_HOST, useValue: host }],
    }).compileComponents();

    service = TestBed.inject(BpmnEditorService);
    modal = TestBed.inject(NgbModal);
    fixture = TestBed.createComponent(ToolbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    fixture.destroy();
  });

  /** Publishing the modeler is what DesignerComponent does once it has built one. */
  const attachModeler = (): void => {
    service.setBpmnModeler(modeler);
    fixture.detectChanges();
  };

  describe('before a modeler exists', () => {
    it('does nothing rather than throwing', () => {
      // The toolbar renders in the same change-detection pass that creates the designer, so
      // every one of these is reachable with no modeler behind it.
      expect(() => {
        component.zoomIn();
        component.zoomOut();
        component.zoomToFit();
        component.onUndo();
        component.onRedo();
        component.onRestart();
        component.onToggleMinimap();
      }).not.toThrow();
    });

    it('offers no diagram to save', async () => {
      await component.onSave();

      expect(host.save).not.toHaveBeenCalled();
    });
  });

  describe('zoom', () => {
    beforeEach(attachModeler);

    it('reports the canvas scale as a whole percentage', () => {
      viewboxListener!({ viewbox: { scale: 0.834 } });

      // The Vue label truncated to 10% steps, so a fit-to-viewport at 83% read "80%".
      expect(component.zoomPercent).toBe(83);
    });

    it('steps by 10% about the canvas origin', () => {
      component.zoomIn();

      expect(canvas.zoom).toHaveBeenCalledWith(1.1, { x: 0, y: 0 });
    });

    it('steps down by 10%', () => {
      component.zoomOut();

      expect(canvas.zoom).toHaveBeenCalledWith(0.9, { x: 0, y: 0 });
    });

    it('stops at the bounds diagram-js itself caps scroll zoom to', () => {
      // Without the clamp the Vue arithmetic walked to 0 and then negative, and a canvas zoomed
      // to zero cannot be zoomed back out.
      viewboxListener!({ viewbox: { scale: 0.2 } });
      component.zoomOut();
      expect(canvas.zoom).toHaveBeenLastCalledWith(0.2, { x: 0, y: 0 });

      viewboxListener!({ viewbox: { scale: 4 } });
      component.zoomIn();
      expect(canvas.zoom).toHaveBeenLastCalledWith(4, { x: 0, y: 0 });
    });

    it('fits the diagram to the viewport', () => {
      component.zoomToFit();

      expect(canvas.zoom).toHaveBeenCalledWith('fit-viewport');
    });

    it('follows a zoom it did not cause', () => {
      // The wheel and fit-to-viewport both move the viewbox without going through the buttons.
      viewboxListener!({ viewbox: { scale: 2 } });

      expect(component.zoomPercent).toBe(200);
    });
  });

  describe('command stack', () => {
    beforeEach(attachModeler);

    it('undoes and redoes', () => {
      component.onUndo();
      component.onRedo();

      expect(commandStack.undo).toHaveBeenCalled();
      expect(commandStack.redo).toHaveBeenCalled();
    });

    it('leaves an empty stack alone', () => {
      // `undo()` on an empty stack is harmless in diagram-js, but asking first keeps the button
      // honest about what it is doing.
      commandStack.canUndo.mockReturnValue(false);
      commandStack.canRedo.mockReturnValue(false);

      component.onUndo();
      component.onRedo();

      expect(commandStack.undo).not.toHaveBeenCalled();
      expect(commandStack.redo).not.toHaveBeenCalled();
    });
  });

  describe('restart', () => {
    beforeEach(attachModeler);

    it('clears the history before importing the new diagram', () => {
      // Undoing past the import would otherwise try to restore a diagram the new document has no
      // elements for, and bpmn-js throws.
      void component.onRestart();

      expect(commandStack.clear).toHaveBeenCalled();
      expect(modeler.importXML).toHaveBeenCalledWith(expect.stringContaining('<bpmn:startEvent'));
    });

    it('builds the new diagram from the configured process identity', () => {
      service.updateConfiguration({ processId: 'Order_9', processName: 'Orders' });

      void component.onRestart();

      expect(modeler.importXML).toHaveBeenCalledWith(expect.stringContaining('<bpmn:process id="Order_9" name="Orders"'));
    });
  });

  describe('dialogs', () => {
    beforeEach(attachModeler);

    it('shows the current XML in the preview dialog', async () => {
      const componentInstance: Record<string, unknown> = {};
      const open = vi.spyOn(modal, 'open').mockReturnValue({ componentInstance } as any);

      await component.onPreviewXml();

      expect(open).toHaveBeenCalledWith(XmlPreviewDialogComponent, expect.anything());
      expect(componentInstance['xml']).toBe('<definitions />');
    });

    it('opens no preview when the diagram cannot be serialised', async () => {
      // A malformed diagram is reported to the console by `currentXml`; an empty dialog would
      // read as "your process is empty", which is a different and wrong message.
      modeler.saveXML.mockRejectedValue(new Error('broken'));
      const error = vi.spyOn(console, 'error').mockImplementation(() => undefined);
      const open = vi.spyOn(modal, 'open');

      await component.onPreviewXml();

      expect(open).not.toHaveBeenCalled();
      error.mockRestore();
    });

    it('opens the shortcut reference', () => {
      const open = vi.spyOn(modal, 'open').mockReturnValue({ componentInstance: {} } as any);

      component.onShowShortcuts();

      expect(open).toHaveBeenCalledWith(ShortcutKeysDialogComponent, expect.anything());
    });

    it('hands the icon dialog the library belonging to the open diagram', () => {
      // The icons live in the diagram, so the dialog edits a modeler service rather than anything
      // of the toolbar's. Handing it the wrong object — or none — is a dialog that lists nothing
      // and saves nowhere.
      const componentInstance: Record<string, unknown> = {};
      const open = vi.spyOn(modal, 'open').mockReturnValue({ componentInstance } as any);

      component.onCustomIcons();

      expect(open).toHaveBeenCalledWith(CustomIconsDialogComponent, expect.anything());
      expect(componentInstance['library']).toBe(customIcons);
    });

    it('opens no icon dialog before there is a diagram', () => {
      service.setBpmnModeler(null);
      const open = vi.spyOn(modal, 'open');

      component.onCustomIcons();

      expect(open).not.toHaveBeenCalled();
    });
  });

  describe('settings', () => {
    it('hides the minimap button when the minimap is off', () => {
      service.updateConfiguration({ miniMap: false });
      fixture.detectChanges();

      expect(component.showMinimap).toBe(false);
      expect(fixture.nativeElement.querySelector('[data-cy="bpmnToggleMinimap"]')).toBeNull();
    });

    it('hides the shortcut button when otherModule is off', () => {
      service.updateConfiguration({ otherModule: false });
      fixture.detectChanges();

      expect(fixture.nativeElement.querySelector('[data-cy="bpmnShortcuts"]')).toBeNull();
    });

    it('toggles the minimap through the module, when one is registered', () => {
      attachModeler();

      component.onToggleMinimap();

      expect(minimap.toggle).toHaveBeenCalled();
    });

    it('does not throw when the minimap module is absent', () => {
      // `miniMap: false` means the module was never registered, so a strict injector lookup
      // would throw rather than doing nothing.
      modeler.get = vi.fn().mockReturnValue(undefined);
      attachModeler();

      expect(() => component.onToggleMinimap()).not.toThrow();
    });
  });

  describe('the save gate', () => {
    /** An element carrying one module property, shaped the way the registry hands them back. */
    const moduleElement = (id: string, type: string, properties: Record<string, unknown>): any => ({
      id,
      businessObject: { $type: type, get: (property: string) => properties[property] },
    });

    it('allows saving a diagram with no module properties at all', () => {
      attachModeler();

      expect(component.canSave).toBe(true);
      expect(fixture.nativeElement.querySelector('[data-cy="bpmnSave"]').disabled).toBe(false);
      expect(fixture.nativeElement.querySelector('[data-cy="bpmnProblems"]')).toBeNull();
    });

    it('blocks saving while a module property is invalid', () => {
      elements = [moduleElement('F', 'FileTransmitter:FileTransmitter', { 'camunda:ip': '999.1.1.1' })];
      attachModeler();

      expect(component.canSave).toBe(false);
      expect(fixture.nativeElement.querySelector('[data-cy="bpmnSave"]').disabled).toBe(true);
    });

    it('says what is wrong and where, rather than only greying the button out', () => {
      // The Vue Save button disabled itself with no explanation anywhere on the page.
      elements = [moduleElement('F', 'FileTransmitter:FileTransmitter', { 'camunda:port': '70000' })];
      attachModeler();

      const banner = fixture.nativeElement.querySelector('[data-cy="bpmnProblems"]');
      expect(banner.textContent).toContain('Port');
      expect(banner.textContent).toContain('Port must be an integer between 0 and 65535');
      expect(banner.textContent).toContain('F');
    });

    it('refuses to save even when the method is called directly', async () => {
      elements = [moduleElement('F', 'FileTransmitter:FileTransmitter', { 'camunda:ip': 'nonsense' })];
      attachModeler();

      await component.onSave();

      expect(host.save).not.toHaveBeenCalled();
    });

    it('re-checks the diagram on every edit', () => {
      // The properties panel writes through the command stack, so this is how a fix to the value
      // re-enables the button.
      elements = [moduleElement('F', 'FileTransmitter:FileTransmitter', { 'camunda:ip': 'nonsense' })];
      attachModeler();
      expect(component.canSave).toBe(false);

      elements = [moduleElement('F', 'FileTransmitter:FileTransmitter', { 'camunda:ip': '10.0.0.1' })];
      commandStackListener!();

      expect(component.canSave).toBe(true);
      expect(fixture.nativeElement.querySelector('[data-cy="bpmnProblems"]')).toBeNull();
    });

    it('catches an element the user has never selected', () => {
      // The Vue store held only the selected element's errors and cleared them when the selection
      // moved on, so this diagram saved with the bad value in it.
      elements = [
        moduleElement('Ok', 'FileTransmitter:FileTransmitter', { 'camunda:ip': '10.0.0.1' }),
        moduleElement('Bad', 'Merger:Merger', { 'camunda:expireTimeOfDay': 'noon' }),
      ];
      attachModeler();

      expect(component.problems.map(problem => problem.elementId)).toEqual(['Bad']);
    });

    it('explains itself in the tooltip of the disabled button', () => {
      elements = [moduleElement('M', 'Merger:Merger', { 'camunda:expireTimeOfDay': '25:00:00' })];
      attachModeler();

      expect(fixture.nativeElement.querySelector('[data-cy="bpmnSave"]').title).toContain('HH:mm:ss');
    });
  });

  describe('save and cancel', () => {
    beforeEach(attachModeler);

    it('hands the diagram to the host and keeps it in the service', async () => {
      await component.onSave();

      expect(host.save).toHaveBeenCalledWith('<definitions />');
      expect(service.getProcessXml()).toBe('<definitions />');
    });

    it('reports the cancel to the host', () => {
      component.onCancel();

      expect(host.cancel).toHaveBeenCalled();
    });
  });
});
