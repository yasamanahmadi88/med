import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { IFlow } from '../flow.model';
import { FlowService } from '../service/flow.service';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';

@Component({
  templateUrl: './flow-delete-dialog.component.html',
  standalone: false,
})
export class FlowDeleteDialogComponent {
  flow?: IFlow;

  constructor(
    protected flowService: FlowService,
    protected activeModal: NgbActiveModal,
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.flowService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
