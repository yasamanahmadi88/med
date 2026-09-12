import { Component, HostListener, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

/** How the last copy attempt went, so the button can say so instead of failing silently. */
type CopyState = 'idle' | 'copied' | 'failed';

/**
 * Shows the diagram's BPMN 2.0 XML, the way the Vue toolbar's "Preview as XML" did.
 *
 * This is the only way to see what the editor will actually save without downloading the file,
 * which is what makes it worth a dialog: the properties panel shows one element at a time and
 * says nothing about the extension attributes the integration modules write.
 */
@Component({
  selector: 'jhi-bpmn-xml-preview-dialog',
  templateUrl: './xml-preview-dialog.component.html',
  styleUrls: ['./xml-preview-dialog.component.scss'],
  standalone: true,
  imports: [CommonModule],
})
export class XmlPreviewDialogComponent {
  @Input() xml = '';

  copyState: CopyState = 'idle';

  constructor(public activeModal: NgbActiveModal) {}

  /**
   * Ctrl/⌘ + C copies the whole document — but only when the user has not selected part of it,
   * because taking the keystroke away from a real selection would copy the wrong thing.
   *
   * The Vue original intercepted the key either way and re-wrote the selection to the clipboard
   * itself, which is what the browser already does.
   */
  @HostListener('document:keydown', ['$event'])
  onKeydown(event: KeyboardEvent): void {
    if (!(event.ctrlKey || event.metaKey) || event.key.toLowerCase() !== 'c') {
      return;
    }
    if (window.getSelection()?.toString()) {
      return;
    }
    event.preventDefault();
    this.copy();
  }

  copy(): void {
    // Absent on an insecure origin, so this is a real branch rather than defensive noise.
    if (!navigator.clipboard) {
      this.copyState = 'failed';
      return;
    }
    navigator.clipboard.writeText(this.xml).then(
      () => (this.copyState = 'copied'),
      (error: unknown) => {
        console.error('Could not copy the diagram XML', error);
        this.copyState = 'failed';
      },
    );
  }

  close(): void {
    this.activeModal.dismiss('cancel');
  }
}
