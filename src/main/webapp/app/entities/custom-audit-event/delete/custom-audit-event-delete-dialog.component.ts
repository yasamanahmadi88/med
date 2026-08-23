import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { ICustomAuditEvent } from '../custom-audit-event.model';
import { CustomAuditEventService } from '../service/custom-audit-event.service';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';

@Component({
  templateUrl: './custom-audit-event-delete-dialog.component.html',
})
export class CustomAuditEventDeleteDialogComponent {
  customAuditEvent?: ICustomAuditEvent;

  constructor(protected customAuditEventService: CustomAuditEventService, protected activeModal: NgbActiveModal) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.customAuditEventService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
