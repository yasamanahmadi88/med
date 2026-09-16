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
 * A product is resolved before the modeler exists.  Its Product -> Group -> Element policy is
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

    const rawProductId = this.route.snapshot.queryParams['productId'];
    const productId = Number(rawProductId);

    if (rawProductId == null || rawProductId === '' || !Number.isFinite(productId) || productId <= 0) {
      this.accessError = 'Select a product before opening the BPMN editor.';
      return;
    }

    this.loadAccessAndSeed(productId, this.flowService.xmlTemp);
  }

  ngOnDestroy(): void {
    this.bpmnEditorService.setProcessXml(undefined);
    this.elementAccessService.clear();
  }

  save(xml: string): void {
    this.saveError = null;
    if (this.flow) {
      const updated: IFlow = { ...this.flow, flow: xml };
      this.flowService.update(updated).subscribe({
        next: () => window.history.back(),
        error: () => {
          this.saveError = 'The BPMN diagram could not be saved. Check product element permissions and try again.';
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
        const productId = this.flow?.product?.id;
        if (productId == null) {
          this.accessError = 'This flow has no product; BPMN element permissions cannot be resolved.';
          return;
        }
        this.loadAccessAndSeed(productId, this.flow?.flow);
      },
      error: () => {
        this.accessError = 'The flow could not be loaded.';
      },
    });
  }

  private loadAccessAndSeed(productId: number, xml: string | null | undefined): void {
    this.elementAccessService.loadForProduct(productId).subscribe({
      next: () => {
        if (xml?.trim()) {
          try {
            const disallowed = this.elementAccessService.findDisallowedXmlElements(xml);
            if (disallowed.length > 0) {
              this.accessError = `This diagram contains ${disallowed.length} BPMN element type(s) that are not allowed for the selected product.`;
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
        this.accessError = 'BPMN element permissions could not be loaded for the selected product.';
      },
    });
  }

  private seed(xml: string | null | undefined): void {
    this.bpmnEditorService.setProcessXml(xml?.trim() ? xml : undefined);
    this.seeded = true;
  }
}
