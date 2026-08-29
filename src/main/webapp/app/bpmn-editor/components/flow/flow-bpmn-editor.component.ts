import { Component, forwardRef, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';

import { FlowService } from 'app/entities/flow/service/flow.service';
import { IFlow } from 'app/entities/flow/flow.model';
import { BpmnEditorComponent } from '../bpmn-editor.component';
import { BpmnEditorService } from '../../services/bpmn-editor.service';
import { BPMN_EDITOR_HOST, BpmnEditorHost } from '../../services/bpmn-editor-host';

/**
 * The Flow form's entry into the editor. It is the only piece of the feature that knows about
 * flows: it seeds the diagram from wherever the caller keeps it and is the toolbar's host, so
 * the editor components themselves stay free of FlowService.
 *
 * Two callers, matching the two states a flow can be in:
 *  - `?flowId=n` from the flow list — the diagram lives on the persisted flow.
 *  - no query param from the new-flow form — the flow does not exist yet, so its draft diagram
 *    is parked on FlowService.xmlTemp until the form is submitted.
 */
@Component({
  selector: 'jhi-flow-bpmn-editor',
  templateUrl: './flow-bpmn-editor.component.html',
  standalone: true,
  imports: [CommonModule, BpmnEditorComponent],
  providers: [{ provide: BPMN_EDITOR_HOST, useExisting: forwardRef(() => FlowBpmnEditorComponent) }],
})
export class FlowBpmnEditorComponent implements OnInit, OnDestroy, BpmnEditorHost {
  seeded = false;

  private flow: IFlow | null = null;

  constructor(
    private route: ActivatedRoute,
    private flowService: FlowService,
    private bpmnEditorService: BpmnEditorService,
  ) {}

  ngOnInit(): void {
    const flowId = this.route.snapshot.queryParams['flowId'];

    if (flowId === undefined) {
      this.seed(this.flowService.xmlTemp);
      return;
    }

    this.flowService.find(Number(flowId)).subscribe({
      next: res => {
        this.flow = res.body;
        this.seed(this.flow?.flow);
      },
      // A flow that cannot be read still opens the editor, on an empty diagram; because `flow`
      // stays null, saving then falls back to the draft rather than writing over the record.
      error: () => this.seed(undefined),
    });
  }

  ngOnDestroy(): void {
    // BpmnEditorService is application-scoped, so the diagram would otherwise still be there the
    // next time someone opens the editor on a different flow.
    this.bpmnEditorService.setProcessXml(undefined);
  }

  save(xml: string): void {
    if (this.flow) {
      this.flow.flow = xml;
      this.flowService.update(this.flow).subscribe();
    } else {
      this.flowService.xmlTemp = xml;
    }
    window.history.back();
  }

  cancel(): void {
    window.history.back();
    // FlowNewComponent sends anyone arriving with an empty xmlTemp straight back here, so a
    // cancelled empty draft has to read as something other than empty or the form traps the user
    // in the editor. The single space is that marker, and the form checks for it by value.
    if (this.flowService.xmlTemp === '') {
      this.flowService.xmlTemp = ' ';
    }
  }

  private seed(xml: string | null | undefined): void {
    // Blank covers both "nothing drafted yet" and the cancelled-draft marker; neither is a
    // diagram bpmn-js could import, so the editor starts a new one instead.
    this.bpmnEditorService.setProcessXml(xml?.trim() ? xml : undefined);
    this.seeded = true;
  }
}
