import { ChangeDetectorRef, Component, Inject, OnDestroy, OnInit, Optional } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import {
  faCode,
  faDownload,
  faEraser,
  faKeyboard,
  faMap,
  faPodcast,
  faRobot,
  faTriangleExclamation,
  faRedo,
  faSave,
  faSearchMinus,
  faSearchPlus,
  faShapes,
  faTimes,
  faUndo,
  faUpload,
} from '@fortawesome/free-solid-svg-icons';
import { Subject, takeUntil } from 'rxjs';
import { ToastrService } from 'ngx-toastr';

import { BpmnEditorService } from '../../services/bpmn-editor.service';
import { BpmnElementAccessService } from '../../services/bpmn-element-access.service';
import { BPMN_EDITOR_HOST, BpmnEditorHost } from '../../services/bpmn-editor-host';
import { blankDiagramXml, newDiagramIdentity } from '../../utils/empty-diagram';
import { ModulePropertyProblem, describeProblem, moduleValidationProblems } from '../../module-properties';
import { CustomIconsDialogComponent } from './custom-icons-dialog.component';
import { XmlPreviewDialogComponent } from './xml-preview-dialog.component';
import { ShortcutKeysDialogComponent } from './shortcut-keys-dialog.component';
import { BpmnEventsDialogComponent } from './bpmn-events-dialog.component';

/** The zoom bounds diagram-js's own scroll zoom caps to; the buttons must not exceed them. */
const ZOOM_MIN = 0.2;
const ZOOM_MAX = 4;
const ZOOM_STEP = 0.1;

@Component({
  selector: 'jhi-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.scss'],
  standalone: true,
  imports: [CommonModule, FontAwesomeModule],
})
export class ToolbarComponent implements OnInit, OnDestroy {
  /**
   * The icons this toolbar draws.
   *
   * This project ships FontAwesome as SVG components and never loads the webfont stylesheet, so
   * the `<i class="fas fa-save">` markup the toolbar used before rendered nothing at all — every
   * button was its text label and an empty box. Passing the definitions straight to `<fa-icon>`
   * keeps the change here rather than in the app-wide icon registry.
   */
  readonly icons = {
    save: faSave,
    export: faDownload,
    import: faUpload,
    cancel: faTimes,
    undo: faUndo,
    redo: faRedo,
    restart: faEraser,
    zoomOut: faSearchMinus,
    zoomIn: faSearchPlus,
    previewXml: faCode,
    processMock: faRobot,
    bpmnEvents: faPodcast,
    minimap: faMap,
    shortcuts: faKeyboard,
    customIcons: faShapes,
    problem: faTriangleExclamation,
  };

  /** Live canvas zoom, so the label keeps up with the wheel and with fit-to-viewport. */
  zoom = 1;

  /** `miniMap` and `otherModule` gate two buttons, exactly as they did in the Vue toolbar. */
  showMinimap = false;
  showShortcuts = true;

  /**
   * Invalid module properties anywhere in the diagram. Save is blocked while this is non-empty,
   * which is what the Vue Save button did — except it only ever saw the selected element.
   */
  problems: readonly ModulePropertyProblem[] = [];

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly bpmnEditorService: BpmnEditorService,
    private readonly modalService: NgbModal,
    private readonly changeDetector: ChangeDetectorRef,
    private readonly elementAccessService: BpmnElementAccessService,
    private readonly toastr: ToastrService,
    @Optional() @Inject(BPMN_EDITOR_HOST) private readonly host: BpmnEditorHost | null,
  ) {}

  ngOnInit(): void {
    this.bpmnEditorService.editorSettings$.pipe(takeUntil(this.destroy$)).subscribe(settings => {
      this.showMinimap = settings.miniMap;
      this.showShortcuts = settings.otherModule;
    });

    this.bpmnEditorService.bpmnModeler$.pipe(takeUntil(this.destroy$)).subscribe(modeler => {
      if (!modeler) {
        return;
      }
      this.zoom = this.canvas()?.zoom() ?? 1;
      this.refreshProblems();
      // Fired by the wheel, by fit-to-viewport and by our own buttons alike, and always from
      // outside Angular's awareness of what changed.
      modeler.on('canvas.viewbox.changed', ({ viewbox }: { viewbox: { scale: number } }) => {
        this.zoom = viewbox.scale;
        this.changeDetector.detectChanges();
      });
      // Every edit goes through the command stack, including the properties panel's writes and a
      // whole import, so this is the one place that sees all of them.
      modeler.on('commandStack.changed', () => this.refreshProblems());
      modeler.on('import.done', () => this.refreshProblems());
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /** The zoom as a whole percentage. Rounded, not truncated to 10% steps as the Vue label was,
   *  so fit-to-viewport reports the scale it actually applied. */
  /** Save is disabled while the diagram holds a property no engine would accept. */
  get canSave(): boolean {
    return this.problems.length === 0;
  }

  /** The tooltip on a disabled Save, and the text of the warning beside it. */
  get problemSummary(): string {
    return this.problems.map(describeProblem).join('\n');
  }

  get zoomPercent(): number {
    return Math.floor(this.zoom * 10) * 10;
  }

  zoomIn(): void {
    this.setZoom(this.zoom + ZOOM_STEP);
  }

  zoomOut(): void {
    this.setZoom(this.zoom - ZOOM_STEP);
  }

  /** Scales and centres the diagram so all of it is on screen. */
  zoomToFit(): void {
    this.canvas()?.zoom('fit-viewport');
  }

  /** Keep the current diagram in the editor service so other views can read it back, then let
   *  the host persist it wherever it came from. */
  onSave(): Promise<void> {
    // Belt and braces: the button is disabled, but a caller reaching the method directly must not
    // be able to persist a diagram the properties panel has already flagged.
    if (!this.canSave) {
      return Promise.resolve();
    }
    return this.currentXml().then(xml => {
      if (xml) {
        this.bpmnEditorService.setProcessXml(xml);
        this.host?.save(xml);
      }
    });
  }

  /** Discard the session. Without a host there is nowhere to report the discard to, so the
   *  plain browser back is all that is left. */
  onCancel(): void {
    if (this.host) {
      this.host.cancel();
    } else {
      window.history.back();
    }
  }

  /** Download the current diagram as a .bpmn file. */
  onExport(): Promise<void> {
    return this.currentXml().then(xml => {
      if (!xml) {
        return;
      }
      const { processId } = this.bpmnEditorService.getProcessDefinition();
      const url = URL.createObjectURL(new Blob([xml], { type: 'application/xml' }));
      const link = document.createElement('a');
      link.href = url;
      link.download = `${processId || 'diagram'}.bpmn`;
      link.click();
      URL.revokeObjectURL(url);
    });
  }

  /** Read a .bpmn/.xml file from disk and load it into the modeler. */
  onImport(): void {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.bpmn,.xml';
    input.onchange = () => {
      const file = input.files?.[0];
      if (!file) {
        return;
      }
      void file.text().then(xml => {
        const modeler = this.bpmnEditorService.getBpmnModeler();
        if (!modeler) {
          return;
        }

        try {
          const disallowed = this.elementAccessService.findDisallowedXmlElements(xml);
          if (disallowed.length > 0) {
            this.toastr.error(`The imported BPMN contains ${disallowed.length} element type(s) not allowed for the active portal owner.`);
            return;
          }
        } catch {
          this.toastr.error('The selected file is not valid BPMN XML.');
          return;
        }

        modeler
          .importXML(xml)
          .then(() => this.bpmnEditorService.setProcessXml(xml))
          .catch((error: unknown) => {
            console.error('Could not import BPMN 2.0 diagram', error);
            this.toastr.error('The BPMN file could not be imported.');
          });
      });
    };
    input.click();
  }

  onUndo(): void {
    const commandStack = this.commandStack();
    if (commandStack?.canUndo()) {
      commandStack.undo();
    }
  }

  onRedo(): void {
    const commandStack = this.commandStack();
    if (commandStack?.canRedo()) {
      commandStack.redo();
    }
  }

  /**
   * Throw the diagram away and start a fresh one.
   *
   * The command stack is cleared first: undoing back past an import would otherwise restore a
   * diagram the new one has no history for, and bpmn-js throws when it tries.
   */
  onRestart(): Promise<void> {
    const modeler = this.bpmnEditorService.getBpmnModeler();
    if (!modeler) {
      return Promise.resolve();
    }

    const { processId, processName } = newDiagramIdentity(this.bpmnEditorService.getEditorSettings());
    const xml = blankDiagramXml(processId, processName);

    return modeler
      .importXML(xml)
      .then(() => {
        // Clear history only after the blank document imported successfully.
        // A failed import must not destroy the user's existing undo/redo stack.
        this.commandStack()?.clear();

        // Keep the service state consistent with what is now on the canvas.
        this.bpmnEditorService.setProcessXml(xml);
      })
      .catch((error: unknown) => {
        console.error('Could not erase BPMN 2.0 diagram', error);
      });
  }

  /** Show the XML the editor would save, without downloading it. */
  onPreviewXml(): Promise<void> {
    return this.currentXml().then(xml => {
      if (xml === undefined) {
        return;
      }
      const modalRef = this.modalService.open(XmlPreviewDialogComponent, { size: 'lg', scrollable: true });
      modalRef.componentInstance.xml = xml;
    });
  }

  /**
   * Toggle BPMN token simulation.
   *
   * Angular-native equivalent of the reference toolbar's toggleProcessMock action.
   */
  toggleProcessMock(): void {
    const toggleMode = this.bpmnEditorService.getBpmnModeler()?.get('toggleMode', false) as { toggleMode?: () => void } | undefined;

    toggleMode?.toggleMode?.();
  }

  /**
   * Open the registered BPMN EventBus listener list.
   *
   * The reference implementation reads EventBus._listeners, sorts the event names and applies
   * a case-sensitive includes filter. Keep those semantics for UI parity.
   */
  openBpmnEvents(): void {
    const eventBus = this.bpmnEditorService.getBpmnModeler()?.get('eventBus', false) as
      | { _listeners?: Record<string, unknown> }
      | undefined;

    if (!eventBus) {
      return;
    }

    const modalRef = this.modalService.open(BpmnEventsDialogComponent, {
      size: 'lg',
      scrollable: true,
    });

    modalRef.componentInstance.events = Object.keys(eventBus._listeners ?? {}).sort();
  }

  /**
   * Open or close the minimap.
   *
   * `designer.scss` hides the widget's own toggle (`.djs-minimap div.toggle`), so this button is
   * the only way in. `get('minimap', false)` rather than `get('minimap')`: the module is absent
   * when the `miniMap` setting is off, and a strict lookup would throw instead of doing nothing.
   */
  onToggleMinimap(): void {
    const minimap = this.bpmnEditorService.getBpmnModeler()?.get('minimap', false);
    minimap?.toggle();
  }

  onShowShortcuts(): void {
    this.modalService.open(ShortcutKeysDialogComponent, { size: 'lg', scrollable: true });
  }

  /**
   * Manage the icons this diagram carries.
   *
   * The library is a modeler service rather than anything of the toolbar's, because it reads and
   * writes the open diagram; without a modeler there is no library and nothing to show.
   */
  onCustomIcons(): void {
    const library = this.bpmnEditorService.getBpmnModeler()?.get('customIcons', false);
    if (!library) {
      return;
    }
    const modalRef = this.modalService.open(CustomIconsDialogComponent, { size: 'lg', scrollable: true });
    modalRef.componentInstance.library = library;
  }

  /**
   * Re-reads the whole diagram's module properties.
   *
   * Called from bpmn-js events, which Angular knows nothing about, so the view has to be told.
   */
  private refreshProblems(): void {
    this.problems = moduleValidationProblems(this.bpmnEditorService.getBpmnModeler());
    this.changeDetector.detectChanges();
  }

  private setZoom(value: number): void {
    const clamped = Math.min(ZOOM_MAX, Math.max(ZOOM_MIN, Math.round(value * 100) / 100));
    // Zooming about the origin rather than the viewport centre, matching the Vue buttons.
    this.canvas()?.zoom(clamped, { x: 0, y: 0 });
  }

  private canvas(): any {
    return this.bpmnEditorService.getBpmnModeler()?.get('canvas');
  }

  private commandStack(): any {
    return this.bpmnEditorService.getBpmnModeler()?.get('commandStack');
  }

  private async currentXml(): Promise<string | undefined> {
    const modeler = this.bpmnEditorService.getBpmnModeler();
    if (!modeler) {
      return undefined;
    }
    try {
      const { xml } = await modeler.saveXML({ format: true });
      return xml;
    } catch (error: unknown) {
      console.error('Could not export BPMN 2.0 diagram', error);
      return undefined;
    }
  }
}
