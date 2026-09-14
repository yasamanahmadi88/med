import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'jhi-bpmn-events-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './bpmn-events-dialog.component.html',
  styleUrls: ['./bpmn-events-dialog.component.scss'],
})
export class BpmnEventsDialogComponent {
  @Input() events: readonly string[] = [];

  filter = '';

  constructor(public readonly activeModal: NgbActiveModal) {}

  get visibleEvents(): readonly string[] {
    return this.events.filter(name => name.includes(this.filter));
  }

  clearFilter(): void {
    this.filter = '';
  }

  close(): void {
    this.activeModal.dismiss('cancel');
  }
}
