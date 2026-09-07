import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { CustomIconsDialogComponent, IconLibraryHandle } from './custom-icons-dialog.component';
import { AddIconResult } from '../../custom-icons/CustomIconLibrary';
import { CustomIcon } from '../../custom-icons/icon-library';
import { MAX_LIBRARY_BYTES, toIconDataUri } from '../../custom-icons/svg-icon';

/**
 * The upload dialog. The library it edits is the modeler's, so it is stubbed here and exercised
 * for real in `custom-icons.spec.ts` and in the Playwright suite; what these specs hold is what
 * the dialog itself decides — which files it will not even preview, and how the stored SVG
 * reaches the DOM.
 */
describe('CustomIconsDialogComponent', () => {
  const CLEAN = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16"><circle cx="8" cy="8" r="7" /></svg>';

  let fixture: ComponentFixture<CustomIconsDialogComponent>;
  let component: CustomIconsDialogComponent;
  let icons: CustomIcon[];
  let library: IconLibraryHandle;
  let addResult: AddIconResult;

  /** A file the way the input hands one over: only `name` and `text()` are ever read. */
  const chooseFile = async (source: string, name = 'payment.svg'): Promise<void> => {
    const file = { name, text: () => Promise.resolve(source) };
    component.onFileSelected({ target: { files: [file], value: name } } as unknown as Event);
    await Promise.resolve();
    fixture.detectChanges();
  };

  beforeEach(async () => {
    icons = [];
    addResult = { ok: true, icon: { id: 'Icon_1', name: 'Payment', contents: toIconDataUri(CLEAN) } };
    library = {
      getIcons: () => icons,
      add: vi.fn(() => addResult),
      remove: vi.fn(),
      remainingBytes: () => MAX_LIBRARY_BYTES,
    };

    await TestBed.configureTestingModule({
      imports: [CustomIconsDialogComponent],
      providers: [{ provide: NgbActiveModal, useValue: { dismiss: vi.fn() } }],
    }).compileComponents();

    fixture = TestBed.createComponent(CustomIconsDialogComponent);
    component = fixture.componentInstance;
    component.library = library;
    fixture.detectChanges();
  });

  afterEach(() => fixture.destroy());

  const text = (): string => fixture.nativeElement.textContent;
  const query = (selector: string): HTMLElement | null => fixture.nativeElement.querySelector(selector);

  describe('choosing a file', () => {
    it('previews a clean SVG and names it after the file', async () => {
      await chooseFile(CLEAN);

      expect(component.error).toBeUndefined();
      expect(component.name).toBe('payment');
      expect(component.canAdd).toBe(true);
      expect(query('[data-cy="bpmnIconPreview"]')?.getAttribute('src')).toBe(toIconDataUri(CLEAN));
    });

    it('refuses a hostile SVG, shows why, and offers nothing to add', async () => {
      await chooseFile('<svg xmlns="http://www.w3.org/2000/svg"><script>alert(1)</script></svg>');

      expect(query('[data-cy="bpmnIconError"]')?.textContent).toContain('<script>');
      expect(query('[data-cy="bpmnIconPreview"]')).toBeNull();
      expect(component.canAdd).toBe(false);
    });

    it('refuses a file that is not an SVG at all, whatever it is called', async () => {
      await chooseFile('<html xmlns="http://www.w3.org/1999/xhtml"><body>x</body></html>', 'icon.svg');

      expect(component.error).toContain('not an SVG');
      expect(component.canAdd).toBe(false);
    });

    it('forgets the previous file when a new one is refused', async () => {
      await chooseFile(CLEAN);
      await chooseFile('<svg xmlns="http://www.w3.org/2000/svg" onload="alert(1)" />');

      // Otherwise Add would still be armed, and would store the file the user replaced.
      expect(component.canAdd).toBe(false);
      expect(component.preview).toBeUndefined();
    });
  });

  describe('adding', () => {
    it('hands the source to the library and clears the form', async () => {
      await chooseFile(CLEAN);

      component.add();

      expect(library.add).toHaveBeenCalledWith('payment', CLEAN);
      expect(component.name).toBe('');
      expect(component.preview).toBeUndefined();
      expect(component.canAdd).toBe(false);
    });

    it('shows the library refusing, and keeps the file so the name can be changed', async () => {
      addResult = { ok: false, message: 'This diagram’s icons would take 200 KB of the 192 KB a diagram may carry.' };
      await chooseFile(CLEAN);

      component.add();
      fixture.detectChanges();

      expect(query('[data-cy="bpmnIconError"]')?.textContent).toContain('192 KB');
      expect(component.canAdd).toBe(true);
    });

    it('does nothing without a file', () => {
      component.name = 'Payment';

      component.add();

      expect(library.add).not.toHaveBeenCalled();
    });
  });

  describe('what it shows', () => {
    it('lists the diagram’s icons and can remove one', () => {
      icons = [{ id: 'Icon_1', name: 'Payment', contents: toIconDataUri(CLEAN) }];
      fixture.detectChanges();

      expect(fixture.nativeElement.querySelectorAll('[data-cy="bpmnIconList"] li')).toHaveLength(1);
      expect(text()).toContain('Payment');

      query('[data-cy="bpmnIconRemove-Icon_1"]')!.click();

      expect(library.remove).toHaveBeenCalledWith('Icon_1');
    });

    it('says when there is nothing stored, and what the diagram has spent', () => {
      expect(text()).toContain('carries no custom icons yet');
      expect(query('[data-cy="bpmnIconUsage"]')?.textContent).toContain('0 KB of 192 KB');
    });

    it('renders a stored SVG as an image source and never as markup', () => {
      // The one assertion that separates `<img src="data:…">` from any form of markup injection:
      // the icon carries text and a script, and neither may appear in this document. An `[src]`
      // binding puts the whole SVG behind one attribute; `[innerHTML]` — or a
      // `bypassSecurityTrustHtml` — would put its elements in the page, and this fails.
      const marked = '<svg xmlns="http://www.w3.org/2000/svg"><text>SENTINEL</text></svg>';
      icons = [{ id: 'Icon_1', name: 'Payment', contents: toIconDataUri(marked) }];
      fixture.detectChanges();

      const image = fixture.nativeElement.querySelector('[data-cy="bpmnIconList"] img');

      expect(image.getAttribute('src')).toBe(toIconDataUri(marked));
      expect(text()).not.toContain('SENTINEL');
      expect(fixture.nativeElement.querySelectorAll('svg, text, script')).toHaveLength(0);
    });
  });

  it('dismisses the modal on close', () => {
    component.close();

    expect(TestBed.inject(NgbActiveModal).dismiss).toHaveBeenCalled();
  });

  it('shows nothing to edit when it was opened without a library', () => {
    component.library = null;
    fixture.detectChanges();

    expect(component.icons).toEqual([]);
    expect(component.canAdd).toBe(false);
  });
});
