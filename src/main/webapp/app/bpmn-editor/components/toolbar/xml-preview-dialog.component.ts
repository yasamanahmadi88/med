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
    // Absent on an insecure origin (the app served over plain http from another machine), so this
    // is a real branch rather than defensive noise.
    if (!navigator.clipboard) {
      this.copyState = this.copyWithExecCommand() ? 'copied' : 'failed';
      return;
    }
    navigator.clipboard.writeText(this.xml).then(
      () => (this.copyState = 'copied'),
      (error: unknown) => {
        console.error('Could not copy the diagram XML', error);
        this.copyState = this.copyWithExecCommand() ? 'copied' : 'failed';
      },
    );
  }

  close(): void {
    this.activeModal.dismiss('cancel');
  }

  /**
   * The pre-Clipboard-API way to copy: select the text in a throwaway textarea and ask the browser
   * to copy the selection. Deprecated, but still the only option outside a secure context.
   */
  private copyWithExecCommand(): boolean {
    if (typeof document.execCommand !== 'function') {
      return false;
    }
    const textarea = document.createElement('textarea');
    textarea.value = this.xml;
    textarea.setAttribute('readonly', '');
    textarea.style.position = 'fixed';
    textarea.style.opacity = '0';
    // Inside the modal, because ngbModal traps focus and would pull it back out of a textarea on <body>.
    const host = document.querySelector('.modal.show') ?? document.body;
    host.appendChild(textarea);
    try {
      textarea.focus();
      textarea.select();
      return document.execCommand('copy');
    } catch {
      return false;
    } finally {
      textarea.remove();
    }
  }
}
