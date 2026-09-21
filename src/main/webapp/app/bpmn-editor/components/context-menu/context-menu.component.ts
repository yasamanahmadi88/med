import { Component, OnDestroy, OnInit, ChangeDetectorRef, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';

import { BpmnEditorService } from '../../services/bpmn-editor.service';
import { AppendOption, appendOptions } from '../../context-menu/append-options';
import { BpmnElementAccessService } from '../../services/bpmn-element-access.service';

/**
 * The "create element" menu, shown when the canvas, a pool or a subprocess is right-clicked.
 *
 * `ContextMenuProvider` decides when this opens and fires `contextMenu.append.open` on the
 * modeler's event bus; replacing an existing element goes to the stock `bpmn-replace` popup
 * instead and never reaches here. Picking an entry hands the new shape to `create`, so it
 * follows the cursor until the user clicks — the same gesture as dragging from the palette.
 */
@Component({
  selector: 'jhi-context-menu',
  templateUrl: './context-menu.component.html',
  styleUrls: ['./context-menu.component.scss'],
  standalone: true,
  imports: [CommonModule],
})
export class ContextMenuComponent implements OnInit, OnDestroy {
  open = false;
  x = 0;
  y = 0;
  options: readonly AppendOption[] = [];

  private modeler: any = null;
  private eventBus: any = null;
  private openedAt = 0;
  private readonly destroy$ = new Subject<void>();

  private readonly appendOpenHandler = (event: { x: number; y: number }): void => {
    this.show(event.x, event.y);
  };

  constructor(
    private readonly bpmnEditorService: BpmnEditorService,
    private readonly changeDetector: ChangeDetectorRef,
    private readonly elementAccessService: BpmnElementAccessService,
  ) {}

  ngOnInit(): void {
    this.bpmnEditorService.bpmnModeler$.pipe(takeUntil(this.destroy$)).subscribe(modeler => {
      this.detachModeler();

      this.modeler = modeler;
      this.open = false;

      if (!modeler) {
        return;
      }

      this.eventBus = modeler.get('eventBus');
      this.eventBus.on('contextMenu.append.open', this.appendOpenHandler);
    });
  }

  ngOnDestroy(): void {
    this.detachModeler();
    this.modeler = null;
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Any click outside an entry dismisses the menu.
   *
   * The right-click that opened it can still be delivered here as the trailing `click` on some
   * platforms, so a menu opened in this same tick is left alone; otherwise it would never
   * appear at all.
   */
  @HostListener('document:click')
  onDocumentClick(): void {
    if (this.open && Date.now() - this.openedAt > 0) {
      this.open = false;
    }
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.open = false;
  }

  /** Creates the chosen element and attaches it to the cursor for the user to place. */
  select(option: AppendOption, event: MouseEvent): void {
    event.stopPropagation();
    this.open = false;

    if (!this.modeler) {
      return;
    }

    try {
      const elementFactory = this.modeler.get('elementFactory');
      const create = this.modeler.get('create');
      const shape = elementFactory.createShape(option.target);

      if (option.target.isExpanded != null) {
        // A collapsed subprocess is the same moddle type as an expanded one; only the DI says
        // which, and `createShape` does not carry it over from the replace option.
        shape.businessObject.di.isExpanded = option.target.isExpanded;
      }

      create.start(event, shape);
    } catch (error) {
      console.error('Could not create the selected element', error);
    }
  }

  /** The entry's label, translated when the modeler offers a translation for it. */
  label(option: AppendOption): string {
    const translate = this.modeler?.get('translate', false);
    return translate ? translate(option.label) : option.label;
  }

  private detachModeler(): void {
    if (!this.eventBus) {
      return;
    }

    this.eventBus.off('contextMenu.append.open', this.appendOpenHandler);
    this.eventBus = null;
  }

  private show(x: number, y: number): void {
    // Re-evaluate access on every open so an active-owner/access-policy change can never
    // leave stale append options visible.
    this.options = appendOptions().filter(option => this.elementAccessService.isTypeAllowed(option.target.type));

    if (this.options.length === 0) {
      this.open = false;

      // The event comes from bpmn-js, outside Angular change detection.
      this.changeDetector.detectChanges();
      return;
    }

    this.x = x;
    this.y = y;
    this.open = true;
    this.openedAt = Date.now();

    // The event arrives from bpmn-js, outside Angular's awareness of what changed.
    this.changeDetector.detectChanges();
  }
}
