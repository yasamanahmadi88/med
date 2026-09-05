import { CommonModule } from '@angular/common';
import { Component, DestroyRef, HostListener, Inject, OnDestroy, OnInit, Optional } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faEraser, faMagnifyingGlassMinus, faMagnifyingGlassPlus, faRotateLeft, faRotateRight } from '@fortawesome/free-solid-svg-icons';

import { BPMN_EDITOR_HOST, BpmnEditorHost } from '../../services/bpmn-editor-host';
import { BpmnEditorService } from '../../services/bpmn-editor.service';

interface BpmnCanvas {
  zoom(): number;
  zoom(scale: number | string, center?: { x: number; y: number }): number;
}

interface BpmnCommandStack {
  canRedo(): boolean;
  canUndo(): boolean;
  clear(): void;
  redo(): void;
  undo(): void;
}

interface BpmnToolbarModeler {
  createDiagram(): Promise<unknown>;
  get(service: string): unknown;
  off?(event: string, callback: (event: unknown) => void): void;
  on(event: string, callback: (event: unknown) => void): void;
  saveXML(options: { format: boolean; preamble?: boolean }): Promise<{ xml?: string }>;
}

interface CanvasViewboxEvent {
  viewbox?: {
    scale?: number;
  };
}

@Component({
  selector: 'jhi-toolbar',
  templateUrl: './toolbar.component.html',
  standalone: true,
  imports: [CommonModule, FontAwesomeModule],
})
export class ToolbarComponent implements OnInit, OnDestroy {
  readonly zoomOutIcon = faMagnifyingGlassMinus;
  readonly zoomInIcon = faMagnifyingGlassPlus;
  readonly undoIcon = faRotateLeft;
  readonly redoIcon = faRotateRight;
  readonly restartIcon = faEraser;

  currentScale = 1;
  xmlPreview: string | null = null;

  private modeler: BpmnToolbarModeler | null = null;

  private readonly onViewboxChanged = (event: unknown): void => {
    const scale = (event as CanvasViewboxEvent).viewbox?.scale;
    if (typeof scale === 'number' && Number.isFinite(scale)) {
      this.currentScale = scale;
    }
  };

  constructor(
    private bpmnEditorService: BpmnEditorService,
    private destroyRef: DestroyRef,
    @Optional() @Inject(BPMN_EDITOR_HOST) private host: BpmnEditorHost | null,
  ) {}

  ngOnInit(): void {
    this.bpmnEditorService.bpmnModeler$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(modeler => {
      this.bindModeler(modeler as BpmnToolbarModeler | null);
    });
  }

  ngOnDestroy(): void {
    this.unbindModeler();
  }

  get zoomPercentage(): string {
    return `${Math.floor(this.currentScale * 10) * 10}%`;
  }

  /** Persist the diagram through the editor host used by the Flow screen. */
  async onSave(): Promise<void> {
    const xml = await this.currentXml(false);
    if (!xml) {
      return;
    }

    this.bpmnEditorService.setProcessXml(xml);
    this.host?.save(xml);
  }

  /** Show the formatted BPMN source without navigating away from the editor. */
  async onPreviewXml(): Promise<void> {
    const xml = await this.currentXml(true);
    if (xml) {
      this.xmlPreview = xml;
    }
  }

  closePreview(): void {
    this.xmlPreview = null;
  }

  /** Discard the session using the host contract, or browser history for a standalone editor. */
  onCancel(): void {
    if (this.host) {
      this.host.cancel();
    } else {
      window.history.back();
    }
  }

  zoomOut(): void {
    this.setZoom(Math.floor(this.currentScale * 100 - 10) / 100);
  }

  fitViewport(): void {
    this.canvas()?.zoom('fit-viewport');
  }

  zoomIn(): void {
    this.setZoom(Math.floor(this.currentScale * 100 + 10) / 100);
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

  /** Match the Vue restart command: clear history and replace the canvas with a new diagram. */
  async onRestart(): Promise<void> {
    const modeler = this.activeModeler();
    if (!modeler) {
      return;
    }

    this.commandStack()?.clear();

    try {
      await modeler.createDiagram();
      const xml = await this.currentXml(false);
      if (xml) {
        this.bpmnEditorService.setProcessXml(xml);
      }
    } catch (error: unknown) {
      console.error('Could not restart BPMN 2.0 diagram', error);
    }
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.closePreview();
  }

  private bindModeler(modeler: BpmnToolbarModeler | null): void {
    this.unbindModeler();
    this.modeler = modeler;

    if (!modeler) {
      this.currentScale = 1;
      return;
    }

    const scale = this.canvas()?.zoom();
    if (typeof scale === 'number' && Number.isFinite(scale)) {
      this.currentScale = scale;
    }

    modeler.on('canvas.viewbox.changed', this.onViewboxChanged);
  }

  private unbindModeler(): void {
    this.modeler?.off?.('canvas.viewbox.changed', this.onViewboxChanged);
    this.modeler = null;
  }

  private activeModeler(): BpmnToolbarModeler | null {
    return this.modeler ?? (this.bpmnEditorService.getBpmnModeler() as BpmnToolbarModeler | null);
  }

  private canvas(): BpmnCanvas | null {
    return (this.activeModeler()?.get('canvas') as BpmnCanvas | undefined) ?? null;
  }

  private commandStack(): BpmnCommandStack | null {
    return (this.activeModeler()?.get('commandStack') as BpmnCommandStack | undefined) ?? null;
  }

  private setZoom(scale: number): void {
    const canvas = this.canvas();
    if (!canvas) {
      return;
    }

    this.currentScale = scale;
    canvas.zoom(scale, { x: 0, y: 0 });
  }

  private async currentXml(preamble: boolean): Promise<string | undefined> {
    const modeler = this.activeModeler();
    if (!modeler) {
      return undefined;
    }

    try {
      const { xml } = await modeler.saveXML({ format: true, preamble });
      return xml;
    } catch (error: unknown) {
      console.error('Could not export BPMN 2.0 diagram', error);
      return undefined;
    }
  }
}
