import { AfterViewInit, Component, ElementRef, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BpmnEditorService } from '../../services/bpmn-editor.service';

@Component({
  selector: 'jhi-panel',
  templateUrl: './panel.component.html',
  styleUrls: ['./panel.component.scss'],
  standalone: true,
  imports: [CommonModule],
})
export class PanelComponent implements AfterViewInit, OnDestroy {
  @ViewChild('panelContent') panelContent!: ElementRef<HTMLElement>;

  constructor(private bpmnEditorService: BpmnEditorService) {}

  ngAfterViewInit(): void {
    // DesignerComponent waits for this before creating the modeler, so that
    // bpmn-js-properties-panel can be given a parent that is already in the DOM.
    this.bpmnEditorService.setPropertiesPanelParent(this.panelContent.nativeElement);
  }

  ngOnDestroy(): void {
    this.bpmnEditorService.setPropertiesPanelParent(null);
  }
}
