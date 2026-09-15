import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faEye, faEyeSlash } from '@fortawesome/free-solid-svg-icons';
import { Subject, takeUntil } from 'rxjs';
import { getBusinessObject } from 'bpmn-js/lib/util/ModelUtil';
import { Base } from 'diagram-js/lib/model';

import { ModuleField, ModuleSchema, optionLabel, optionValue } from '../../module-properties/schema';
import { schemaForType } from '../../module-properties/schemas';
import { validateField } from '../../module-properties/validators';
import { BpmnEditorService } from '../../services/bpmn-editor.service';

interface Modeling {
  updateModdleProperties(element: Base, moddleElement: unknown, properties: Record<string, unknown>): void;
}

interface ElementRegistry {
  find(predicate: (element: Base) => boolean): Base | undefined;
}

/**
 * Vue-compatible integration-module property panel.
 *
 * The generic bpmn-js properties panel is intentionally kept for non-custom panel modes. In the
 * default `penalMode: 'custom'` mode this component reproduces the source Vue editor instead:
 * one flat field list, the same field order, and the same engine-prefixed moddle properties.
 *
 * Values are always written through `modeling.updateModdleProperties`, never by mutating the
 * business object directly, so undo/redo and XML serialization keep working exactly as they do
 * for stock bpmn-js properties.
 */
@Component({
  selector: 'jhi-module-panel',
  templateUrl: './module-panel.component.html',
  styleUrls: ['./module-panel.component.scss'],
  standalone: true,
  imports: [CommonModule, FormsModule, FontAwesomeModule],
  host: {
    '[hidden]': '!panelVisible',
  },
})
export class ModulePanelComponent implements OnInit, OnDestroy {
  readonly icons = { eye: faEye, eyeSlash: faEyeSlash };
  currentElement: Base | null = null;
  currentSchema: ModuleSchema | null = null;
  visibleFields: readonly ModuleField[] = [];
  panelTitle = '';
  panelVisible = false;

  private readonly destroy$ = new Subject<void>();
  private modeler: any | null = null;
  private prefix = 'camunda';
  private readonly revealedPasswords = new Set<string>();

  private readonly importDoneHandler = (): void => this.selectElement(null);
  private readonly selectionChangedHandler = ({ newSelection }: { newSelection?: Base[] }): void => {
    this.selectElement(newSelection?.[0] ?? null);
  };
  private readonly elementChangedHandler = ({ element }: { element?: Base }): void => {
    if (element && element.id === this.currentElement?.id) {
      this.selectElement(element);
    }
  };

  constructor(
    private readonly bpmnEditorService: BpmnEditorService,
    private readonly changeDetector: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.bpmnEditorService.bpmnModeler$.pipe(takeUntil(this.destroy$)).subscribe(modeler => {
      this.detachModeler();
      this.modeler = modeler;

      if (!modeler) {
        this.clearPanel();
        return;
      }

      this.prefix = this.bpmnEditorService.getProcessEngine() || 'camunda';
      modeler.on('import.done', this.importDoneHandler);
      modeler.on('selection.changed', this.selectionChangedHandler);
      modeler.on('element.changed', this.elementChangedHandler);
      this.selectElement(null);
    });
  }

  ngOnDestroy(): void {
    this.detachModeler();
    this.destroy$.next();
    this.destroy$.complete();
  }

  fieldValue(field: ModuleField): unknown {
    const businessObject = this.currentElement ? getBusinessObject(this.currentElement) : null;
    return businessObject?.get?.(`${this.prefix}:${field.name}`) ?? '';
  }

  updateField(field: ModuleField, value: unknown): void {
    if (!this.modeler || !this.currentElement) {
      return;
    }

    const businessObject = getBusinessObject(this.currentElement);
    const modeling = this.modeler.get('modeling', false) as Modeling | undefined;
    if (!businessObject || !modeling) {
      return;
    }

    modeling.updateModdleProperties(this.currentElement, businessObject, {
      [`${this.prefix}:${field.name}`]: value,
    });
  }

  fieldError(field: ModuleField): string | undefined {
    return field.validate ? validateField(field.validate, this.fieldValue(field)) : undefined;
  }

  optionValue = optionValue;
  optionLabel = optionLabel;

  isPasswordRevealed(field: ModuleField): boolean {
    return this.revealedPasswords.has(field.name);
  }

  togglePassword(field: ModuleField): void {
    if (this.revealedPasswords.has(field.name)) {
      this.revealedPasswords.delete(field.name);
    } else {
      this.revealedPasswords.add(field.name);
    }
  }

  trackField(_index: number, field: ModuleField): string {
    return field.name;
  }

  private selectElement(element: Base | null): void {
    if (!this.modeler) {
      this.clearPanel();
      return;
    }

    let selected = element;
    if (!selected) {
      const elementRegistry = this.modeler.get('elementRegistry', false) as ElementRegistry | undefined;
      selected =
        elementRegistry?.find(candidate => candidate.type === 'bpmn:Process') ??
        elementRegistry?.find(candidate => candidate.type === 'bpmn:Collaboration') ??
        null;
    }

    this.currentElement = selected;
    this.revealedPasswords.clear();
    this.refreshSchema();
  }

  private refreshSchema(): void {
    const businessObject = this.currentElement ? getBusinessObject(this.currentElement) : null;
    const type = businessObject?.$type ?? this.currentElement?.type;

    this.currentSchema = schemaForType(type) ?? null;
    this.panelTitle = this.currentSchema?.label ?? this.shortType(type);
    this.panelVisible = Boolean(this.currentElement && type !== 'bpmn:Process');

    this.visibleFields = this.currentSchema ? this.currentSchema.fields.filter(field => field.customPanelVisible !== false) : [];

    this.changeDetector.markForCheck();
  }

  private shortType(type: string | undefined): string {
    if (!type) {
      return '';
    }
    return type.includes(':') ? (type.split(':').pop() ?? type) : type;
  }

  private detachModeler(): void {
    if (!this.modeler) {
      return;
    }

    this.modeler.off?.('import.done', this.importDoneHandler);
    this.modeler.off?.('selection.changed', this.selectionChangedHandler);
    this.modeler.off?.('element.changed', this.elementChangedHandler);
    this.modeler = null;
  }

  private clearPanel(): void {
    this.currentElement = null;
    this.currentSchema = null;
    this.visibleFields = [];
    this.panelTitle = '';
    this.panelVisible = false;
    this.revealedPasswords.clear();
    this.changeDetector.markForCheck();
  }
}
