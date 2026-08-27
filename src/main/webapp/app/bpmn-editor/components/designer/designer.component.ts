import { Component, OnDestroy, Input, Output, EventEmitter, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject } from 'rxjs';
import BpmnModeler from 'bpmn-js/lib/Modeler';
import { BpmnPropertiesPanelModule, BpmnPropertiesProviderModule, CamundaPlatformPropertiesProviderModule } from 'bpmn-js-properties-panel';
import camundaModdleDescriptor from 'camunda-bpmn-moddle/resources/camunda.json';
import { BpmnEditorService } from '../../services/bpmn-editor.service';

@Component({
  selector: 'jhi-designer',
  templateUrl: './designer.component.html',
  styleUrls: ['./designer.component.scss'],
  standalone: true,
  imports: [CommonModule],
})
export class DesignerComponent implements AfterViewInit, OnDestroy {
  @ViewChild('canvas') canvas!: ElementRef;
  @Input() xml: string | undefined;
  @Output() xmlUpdate = new EventEmitter<string>();

  private bpmnModeler: BpmnModeler | null = null;
  private destroy$ = new Subject<void>();

  constructor(private bpmnEditorService: BpmnEditorService) {}

  ngAfterViewInit(): void {
    // PanelComponent is a sibling, so its ngAfterViewInit runs after this one in the same
    // change-detection pass. Deferring by a macrotask lets it register its element first, so
    // the properties panel has a parent to render into.
    setTimeout(() => this.initModeler(), 0);
  }

  ngOnDestroy(): void {
    if (this.bpmnModeler) {
      this.bpmnModeler.destroy();
    }
    this.bpmnEditorService.setBpmnModeler(null);
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initModeler(): void {
    if (!this.canvas) {
      setTimeout(() => this.initModeler(), 100);
      return;
    }

    const panelParent = this.bpmnEditorService.getPropertiesPanelParent();

    try {
      this.bpmnModeler = new BpmnModeler({
        container: this.canvas.nativeElement,
        keyboard: {
          // ViewerOptions uses one type parameter for both `container` and `keyboard.bindTo`,
          // and `container` must be an Element. documentElement receives the same bubbled
          // key events `window` did.
          bindTo: document.documentElement,
        },
        // The panel modules read `propertiesPanel.parent`, so they are only registered when a
        // parent exists — the editor can be configured without the custom panel.
        ...(panelParent
          ? {
              propertiesPanel: { parent: panelParent },
              additionalModules: [BpmnPropertiesPanelModule, BpmnPropertiesProviderModule, CamundaPlatformPropertiesProviderModule],
              moddleExtensions: { camunda: camundaModdleDescriptor },
            }
          : {}),
      });

      this.bpmnEditorService.setBpmnModeler(this.bpmnModeler);

      if (this.xml) {
        this.loadXml(this.xml);
      } else {
        // Without a diagram there is no canvas root and no element to select, so the properties
        // panel would render empty and the palette would refuse to place anything.
        this.bpmnModeler.createDiagram();
      }

      // Listen for xml changes
      this.bpmnModeler.on('commandStack.changed', () => {
        this.updateXml();
      });
    } catch (error) {
      console.error('Failed to initialize BPMN Modeler:', error);
    }
  }

  private loadXml(xml: string): void {
    if (!this.bpmnModeler) return;

    this.bpmnModeler.importXML(xml).catch((error: any) => {
      console.error('Could not import BPMN 2.0 diagram', error);
    });
  }

  private updateXml(): void {
    if (!this.bpmnModeler) return;

    this.bpmnModeler.saveXML({ format: true }).then((result: any) => {
      this.xmlUpdate.emit(result.xml);
    });
  }
}
