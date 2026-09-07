import { ChangeDetectorRef, Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Base } from 'diagram-js/lib/model';

import { BpmnEditorService } from '../../services/bpmn-editor.service';
import { contextMenuOptions, ContextMenuEntry, isAppendAction } from './context-menu-options';

@Component({
  selector: 'jhi-context-menu',
  templateUrl: './context-menu.component.html',
  styleUrls: ['./context-menu.component.scss'],
  standalone: true,
  imports: [CommonModule],
})
export class ContextMenuComponent implements OnInit, OnDestroy {
  visible = false;
  title = '';
  entries: ContextMenuEntry[] = [];
  x = 0;
  y = 0;

  private modeler: any = null;
  private currentElement: Base | null = null;
  private appendMode = false;
  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly bpmnEditorService: BpmnEditorService,
    private readonly changeDetector: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.bpmnEditorService.bpmnModeler$.pipe(takeUntil(this.destroy$)).subscribe(modeler => this.bindModeler(modeler));
  }

  ngOnDestroy(): void {
    this.unbindModeler();
    this.destroy$.next();
    this.destroy$.complete();
  }

  triggerAction(entry: ContextMenuEntry, event: MouseEvent): void {
    const modeler = this.modeler ?? this.bpmnEditorService.getBpmnModeler();
    if (!modeler) return;

    try {
      if (this.appendMode) {
        const elementFactory = modeler.get('elementFactory');
        const create = modeler.get('create');
        const shape = elementFactory.createShape(entry.target);

        if (entry.target['isExpanded'] !== undefined && shape.businessObject?.di) {
          shape.businessObject.di.isExpanded = entry.target['isExpanded'];
        }

        this.close();
        setTimeout(() => create.start(event, shape), 30);
      } else if (this.currentElement) {
        modeler.get('bpmnReplace').replaceElement(this.currentElement, entry.target);
        this.close();
      }
    } catch (error) {
      console.error('Unable to run BPMN context-menu action:', error);
      this.close();
    }
  }

  close(): void {
    if (!this.visible) return;
    this.visible = false;
    this.changeDetector.detectChanges();
  }

  @HostListener('document:click')
  onDocumentClick(): void {
    this.close();
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.close();
  }

  private readonly onElementContextMenu = (event: { element?: Base; originalEvent?: MouseEvent }): void => {
    const settings = this.bpmnEditorService.getEditorSettings();
    const originalEvent = event.originalEvent;

    if (!settings.contextmenu || !settings.customContextmenu || !originalEvent) {
      this.close();
      return;
    }

    originalEvent.preventDefault();
    this.currentElement = event.element ?? null;
    this.appendMode = isAppendAction(event.element);
    this.entries = contextMenuOptions(event.element);

    if (this.entries.length === 0) {
      this.close();
      return;
    }

    this.title = this.appendMode ? 'Create Element' : 'Change Element';
    this.setPosition(originalEvent);
    this.visible = true;
    this.changeDetector.detectChanges();
  };

  private bindModeler(modeler: any): void {
    this.unbindModeler();
    this.modeler = modeler;
    this.modeler?.on?.('element.contextmenu', 2000, this.onElementContextMenu);
  }

  private unbindModeler(): void {
    this.modeler?.off?.('element.contextmenu', this.onElementContextMenu);
    this.modeler = null;
  }

  private setPosition(event: MouseEvent): void {
    const margin = 12;
    const menuWidth = Math.min(400, window.innerWidth - margin * 2);
    const menuHeight = Math.min(360, window.innerHeight - margin * 2);

    this.x = Math.max(margin, Math.min(event.clientX, window.innerWidth - menuWidth - margin));
    this.y = Math.max(margin, Math.min(event.clientY, window.innerHeight - menuHeight - margin));
  }
}
