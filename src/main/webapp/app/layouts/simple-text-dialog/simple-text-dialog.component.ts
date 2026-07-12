import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'jhi-simple-text-dialog',
  templateUrl: './simple-text-dialog.component.html',
  styleUrls: ['./simple-text-dialog.component.scss'],
  standalone: false,
})
export class SimpleTextDialogComponent {
  text = '';

  constructor(public activeModal: NgbActiveModal) {}

  close(): void {
    this.activeModal.dismiss('cancel');
  }
}
