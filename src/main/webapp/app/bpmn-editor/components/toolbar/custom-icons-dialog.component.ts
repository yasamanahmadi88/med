import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { AddIconResult } from '../../custom-icons/CustomIconLibrary';
import { CustomIcon } from '../../custom-icons/icon-library';
import { MAX_ICON_BYTES, MAX_LIBRARY_BYTES, formatKb, validateSvgIcon } from '../../custom-icons/svg-icon';

/** What this dialog needs of `customIcons`, so it can be driven without a modeler. */
export interface IconLibraryHandle {
  getIcons(): CustomIcon[];
  add(name: string, svgSource: string): AddIconResult;
  remove(id: string): void;
  remainingBytes(): number;
}

/**
 * Upload an SVG, name it, and see what this diagram already carries.
 *
 * The icons belong to the diagram, not to the browser: adding one is a command on the modeler, so
 * it is undoable, it marks the diagram dirty, and it is saved with everything else. That is what
 * makes an icon visible to the next person who opens the same flow — the Vue original kept them in
 * `localStorage`, where they were visible to one browser profile and nobody else.
 *
 * The uploaded SVG only ever reaches the DOM through `<img src="data:…">`, both here and in the
 * palette. It is never assigned as markup, and nothing is passed through `bypassSecurityTrust*`.
 */
@Component({
  selector: 'jhi-bpmn-custom-icons-dialog',
  templateUrl: './custom-icons-dialog.component.html',
  styleUrls: ['./custom-icons-dialog.component.scss'],
  standalone: true,
  imports: [CommonModule, FormsModule],
})
export class CustomIconsDialogComponent {
  @Input() library: IconLibraryHandle | null = null;

  /** The name the icon will be stored under, and the name of the shapes placed from it. */
  name = '';

  /** The validated `data:` URI of the file waiting to be added, if there is one. */
  preview: string | undefined;

  /** Why the last upload or the last file was refused. */
  error: string | undefined;

  readonly maxIconLabel = formatKb(MAX_ICON_BYTES);
  readonly maxLibraryLabel = formatKb(MAX_LIBRARY_BYTES);

  private source: string | undefined;

  /** Kept so the picker can be cleared after a successful add; otherwise re-choosing the same file fires nothing. */
  private fileInput: HTMLInputElement | undefined;

  constructor(public activeModal: NgbActiveModal) {}

  get icons(): CustomIcon[] {
    return this.library?.getIcons() ?? [];
  }

  get canAdd(): boolean {
    return Boolean(this.source && this.name.trim() && this.library);
  }

  /** What is left of the diagram's icon budget, for the footer. */
  get usage(): string {
    const remaining = this.library?.remainingBytes() ?? MAX_LIBRARY_BYTES;
    return `${formatKb(MAX_LIBRARY_BYTES - remaining)} of ${this.maxLibraryLabel} used`;
  }

  /**
   * Read the chosen file and check it before anything else happens.
   *
   * Validating here as well as in the library is not belt and braces for its own sake: it is what
   * puts the reason in front of the user while they are still looking at the file picker, and what
   * keeps a rejected file from ever becoming a preview.
   */
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];

    this.fileInput = input;

    this.source = undefined;
    this.preview = undefined;
    this.error = undefined;

    if (!file) {
      return;
    }

    // `accept` is a filter on the file dialog, not a guarantee: a renamed file arrives here just
    // the same, so the type is decided by parsing the bytes.
    void file.text().then(source => {
      const validation = validateSvgIcon(source);

      if (!validation.ok) {
        this.error = validation.message;
        return;
      }

      this.source = source;
      this.preview = validation.dataUri;
      this.name ||= file.name.replace(/\.svg$/i, '');
    });
  }

  add(): void {
    if (!this.library || !this.source) {
      return;
    }

    const result = this.library.add(this.name, this.source);

    if (!result.ok) {
      this.error = result.message;
      return;
    }

    this.reset();
  }

  remove(id: string): void {
    this.library?.remove(id);
  }

  close(): void {
    this.activeModal.dismiss('cancel');
  }

  private reset(): void {
    if (this.fileInput) {
      this.fileInput.value = '';
    }
    this.name = '';
    this.source = undefined;
    this.preview = undefined;
    this.error = undefined;
  }
}
