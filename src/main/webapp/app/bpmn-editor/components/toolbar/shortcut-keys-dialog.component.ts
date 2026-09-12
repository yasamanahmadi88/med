import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { SHORTCUT_GROUPS, ShortcutGroup } from './shortcuts';

/**
 * The keyboard-shortcut reference, opened from the toolbar.
 *
 * bpmn-js binds a lot of keys and advertises none of them, so without this the tools behind
 * `H`, `L`, `S`, `C`, `R`, `A` and `N` are only findable by accident.
 */
@Component({
  selector: 'jhi-bpmn-shortcut-keys-dialog',
  templateUrl: './shortcut-keys-dialog.component.html',
  styleUrls: ['./shortcut-keys-dialog.component.scss'],
  standalone: true,
  imports: [CommonModule],
})
export class ShortcutKeysDialogComponent {
  readonly groups: readonly ShortcutGroup[] = SHORTCUT_GROUPS;

  constructor(public activeModal: NgbActiveModal) {}

  close(): void {
    this.activeModal.dismiss('cancel');
  }
}
