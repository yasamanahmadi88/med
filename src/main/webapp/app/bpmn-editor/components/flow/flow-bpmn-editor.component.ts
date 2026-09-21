import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, forwardRef } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IFlow } from 'app/entities/flow/flow.model';
import { FlowService } from 'app/entities/flow/service/flow.service';
import { BpmnEditorComponent } from '../bpmn-editor.component';
import { BpmnEditorService } from '../../services/bpmn-editor.service';
import { BPMN_EDITOR_HOST, BpmnEditorHost } from '../../services/bpmn-editor-host';
import { BpmnElementAccessService } from '../../services/bpmn-element-access.service';

/**
 * Flow-aware host for the BPMN editor.
 *
 * The active Portal Owner is resolved before the modeler exists.  Its Owner -> Group -> Element policy is
 * loaded first and DesignerComponent consumes that already-loaded snapshot while constructing
 * bpmn-js.  There is deliberately no unscoped editor fallback.
 */
@Component({
  selector: 'jhi-flow-bpmn-editor',
  templateUrl: './flow-bpmn-editor.component.html',
  styleUrls: ['./flow-bpmn-editor.component.scss'],
  standalone: true,
  imports: [CommonModule, BpmnEditorComponent],
  providers: [{ provide: BPMN_EDITOR_HOST, useExisting: forwardRef(() => FlowBpmnEditorComponent) }],
})
export class FlowBpmnEditorComponent implements OnInit, OnDestroy, BpmnEditorHost {
  seeded = false;
  accessError: string | null = null;
  saveError: string | null = null;

  private flow: IFlow | null = null;

  constructor(
    private route: ActivatedRoute,
    private flowService: FlowService,
    private bpmnEditorService: BpmnEditorService,
    private elementAccessService: BpmnElementAccessService,
  ) {}

  ngOnInit(): void {
    this.elementAccessService.clear();
    const flowId = this.route.snapshot.queryParams['flowId'];

    if (flowId !== undefined) {
      this.loadPersistedFlow(Number(flowId));
      return;
    }

    this.loadAccessAndSeed(this.flowService.xmlTemp);
  }

  ngOnDestroy(): void {
    this.bpmnEditorService.setProcessXml(undefined);
    this.elementAccessService.clear();
  }

  save(xml: string): void {
    this.saveError = null;
    try {
      if (this.elementAccessService.findDisallowedXmlElements(xml).length > 0) {
        this.saveError = 'Adding elements not permitted for this portal Owner is not allowed.';
        return;
      }
    } catch {
      this.saveError = 'The BPMN XML is invalid.';
      return;
    }

    if (this.flow) {
      const updated: IFlow = { ...this.flow, flow: xml };
      this.flowService.update(updated).subscribe({
        next: () => window.history.back(),
        error: () => {
          this.saveError = 'The BPMN diagram could not be saved. Check BPMN element permissions and try again.';
        },
      });
      return;
    }

    this.flowService.xmlTemp = xml;
    window.history.back();
  }

  cancel(): void {
    window.history.back();
    // Preserve the existing cancelled-draft marker used by FlowNewComponent.
    if (this.flowService.xmlTemp === '') {
      this.flowService.xmlTemp = ' ';
    }
  }

  private loadPersistedFlow(flowId: number): void {
    this.flowService.find(flowId).subscribe({
      next: res => {
        this.flow = res.body;
        this.loadAccessAndSeed(this.flow?.flow);
      },
      error: () => {
        this.accessError = 'The flow could not be loaded.';
      },
    });
  }

  private loadAccessAndSeed(xml: string | null | undefined): void {
    this.elementAccessService.loadCurrent().subscribe({
      next: () => {
        if (xml?.trim()) {
          try {
            // Only the server-loaded flow is a legacy baseline. A draft has no persisted legacy
            // instances, so imported or pasted CDR/CSV elements remain creation attempts.
            this.elementAccessService.setPersistedDiagram(this.flow?.flow);
            const disallowed = this.elementAccessService.findDisallowedXmlElements(xml);
            if (disallowed.length > 0) {
              this.accessError = `This diagram contains ${disallowed.length} BPMN element type(s) that are not allowed for the active portal Owner.`;
              return;
            }
          } catch {
            this.accessError = 'The BPMN XML is invalid.';
            return;
          }
        }
        this.seed(xml);
      },
      error: () => {
        this.accessError = 'BPMN element permissions could not be loaded for the active portal Owner.';
      },
    });
  }

  private seed(xml: string | null | undefined): void {
    this.bpmnEditorService.setProcessXml(xml?.trim() ? xml : undefined);
    this.seeded = true;
  }
}
