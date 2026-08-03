import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { IMedAuthority } from '../med-authority.model';
import { MedAuthorityService } from '../service/med-authority.service';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';

@Component({
  templateUrl: './med-authority-delete-dialog.component.html',
  standalone: false,
})
export class MedAuthorityDeleteDialogComponent {
  medAuthority?: IMedAuthority;

  constructor(
    protected medAuthorityService: MedAuthorityService,
    protected activeModal: NgbActiveModal,
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.medAuthorityService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
