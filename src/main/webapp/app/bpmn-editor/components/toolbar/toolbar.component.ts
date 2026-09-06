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
  faRedo,
  faSave,
  faSearchMinus,
  faSearchPlus,
  faTimes,
  faUndo,
  faUpload,
} from '@fortawesome/free-solid-svg-icons';
import { Subject, takeUntil } from 'rxjs';

import { BpmnEditorService } from '../../services/bpmn-editor.service';
import { BPMN_EDITOR_HOST, BpmnEditorHost } from '../../services/bpmn-editor-host';
import { createNewDiagram } from '../../utils/empty-diagram';
import { XmlPreviewDialogComponent } from './xml-preview-dialog.component';
import { ShortcutKeysDialogComponent } from './shortcut-keys-dialog.component';

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
    minimap: faMap,
    shortcuts: faKeyboard,
  };

  /** Live canvas zoom, so the label keeps up with the wheel and with fit-to-viewport. */
  zoom = 1;

  /** `miniMap` and `otherModule` gate two buttons, exactly as they did in the Vue toolbar. */
  showMinimap = false;
  showShortcuts = true;

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly bpmnEditorService: BpmnEditorService,
    private readonly modalService: NgbModal,
    private readonly changeDetector: ChangeDetectorRef,
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
      // Fired by the wheel, by fit-to-viewport and by our own buttons alike, and always from
      // outside Angular's awareness of what changed.
      modeler.on('canvas.viewbox.changed', ({ viewbox }: { viewbox: { scale: number } }) => {
        this.zoom = viewbox.scale;
        this.changeDetector.detectChanges();
      });
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /** The zoom as a whole percentage. Rounded, not truncated to 10% steps as the Vue label was,
   *  so fit-to-viewport reports the scale it actually applied. */
  get zoomPercent(): number {
    return Math.round(this.zoom * 100);
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
        modeler.importXML(xml).catch((error: unknown) => console.error('Could not import BPMN 2.0 diagram', error));
        this.bpmnEditorService.setProcessXml(xml);
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
    this.commandStack()?.clear();
    return createNewDiagram(modeler, this.bpmnEditorService.getEditorSettings()).catch((error: unknown) => {
      console.error('Could not create BPMN 2.0 diagram', error);
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
