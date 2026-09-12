import { Component, OnDestroy, OnInit, ChangeDetectorRef, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';

import { BpmnEditorService } from '../../services/bpmn-editor.service';
import { AppendOption, appendOptions } from '../../context-menu/append-options';

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
  readonly options: readonly AppendOption[] = appendOptions();

  private modeler: any = null;
  private openedAt = 0;
  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly bpmnEditorService: BpmnEditorService,
    private readonly changeDetector: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.bpmnEditorService.bpmnModeler$.pipe(takeUntil(this.destroy$)).subscribe(modeler => {
      this.modeler = modeler;
      if (!modeler) {
        this.open = false;
        return;
      }
      modeler.get('eventBus').on('contextMenu.append.open', (event: { x: number; y: number }) => {
        this.show(event.x, event.y);
      });
    });
  }

  ngOnDestroy(): void {
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

  private show(x: number, y: number): void {
    this.x = x;
    this.y = y;
    this.open = true;
    this.openedAt = Date.now();
    // The event arrives from bpmn-js, outside Angular's awareness of what changed.
    this.changeDetector.detectChanges();
  }
}
