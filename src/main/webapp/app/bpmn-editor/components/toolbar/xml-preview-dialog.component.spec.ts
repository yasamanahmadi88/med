import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { XmlPreviewDialogComponent } from './xml-preview-dialog.component';

describe('XmlPreviewDialogComponent', () => {
  let fixture: ComponentFixture<XmlPreviewDialogComponent>;
  let component: XmlPreviewDialogComponent;
  let activeModal: NgbActiveModal;

  const clipboard = (writeText: ReturnType<typeof vi.fn>): void => {
    Object.defineProperty(navigator, 'clipboard', { value: { writeText }, configurable: true });
  };

  const execCommand = (impl: ReturnType<typeof vi.fn>): void => {
    Object.defineProperty(document, 'execCommand', { value: impl, configurable: true, writable: true });
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [XmlPreviewDialogComponent],
      providers: [{ provide: NgbActiveModal, useValue: { dismiss: vi.fn() } }],
    }).compileComponents();

    fixture = TestBed.createComponent(XmlPreviewDialogComponent);
    component = fixture.componentInstance;
    activeModal = TestBed.inject(NgbActiveModal);
  });

  afterEach(() => {
    fixture.destroy();
    Reflect.deleteProperty(navigator, 'clipboard');
    Reflect.deleteProperty(document, 'execCommand');
  });

  it('shows the xml it was given', () => {
    component.xml = '<bpmn:definitions id="Definitions_1" />';
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[data-cy="bpmnXmlPreview"]').textContent).toContain('Definitions_1');
  });

  it('renders the markup as text rather than as elements', () => {
    // The document is untrusted input the moment a user imports a .bpmn file, so it has to
    // arrive in the DOM as text. Angular interpolation escapes it; this pins that it stays that
    // way if the template ever moves to a binding that does not.
    component.xml = '<bpmn:process /><img src="x" onerror="alert(1)">';
    fixture.detectChanges();

    const preview = fixture.nativeElement.querySelector('[data-cy="bpmnXmlPreview"]');
    expect(preview.querySelector('img')).toBeNull();
    expect(preview.textContent).toContain('<img src="x"');
  });

  it('copies the whole document', async () => {
    const writeText = vi.fn().mockResolvedValue(undefined);
    clipboard(writeText);
    component.xml = '<definitions />';

    component.copy();
    await Promise.resolve();

    expect(writeText).toHaveBeenCalledWith('<definitions />');
    expect(component.copyState).toBe('copied');
  });

  it('says so when the clipboard refuses', async () => {
    // Denied permission and an insecure origin both land here, and a Copy button that silently
    // does nothing leaves the user believing they have the XML.
    const error = vi.spyOn(console, 'error').mockImplementation(() => undefined);
    clipboard(vi.fn().mockRejectedValue(new Error('denied')));

    component.copy();
    await Promise.resolve();
    await Promise.resolve();

    expect(component.copyState).toBe('failed');
    error.mockRestore();
  });

  it('falls back to execCommand when there is no clipboard api', () => {
    // navigator.clipboard only exists in a secure context, so the app served over plain http
    // from another machine (http://<host>:8080) has none, while localhost does.
    Object.defineProperty(navigator, 'clipboard', { value: undefined, configurable: true });
    let copied: string | undefined;
    execCommand(
      vi.fn(() => {
        copied = (document.activeElement as HTMLTextAreaElement).value;
        return true;
      }),
    );
    component.xml = '<definitions />';

    component.copy();

    expect(document.execCommand).toHaveBeenCalledWith('copy');
    expect(copied).toBe('<definitions />');
    expect(component.copyState).toBe('copied');
    expect(document.querySelector('textarea')).toBeNull();
  });

  it('falls back to execCommand when the clipboard api refuses', async () => {
    const error = vi.spyOn(console, 'error').mockImplementation(() => undefined);
    clipboard(vi.fn().mockRejectedValue(new Error('denied')));
    execCommand(vi.fn().mockReturnValue(true));

    component.copy();
    await Promise.resolve();
    await Promise.resolve();

    expect(document.execCommand).toHaveBeenCalledWith('copy');
    expect(component.copyState).toBe('copied');
    error.mockRestore();
  });

  it('says so when there is no clipboard api and the fallback fails too', () => {
    Object.defineProperty(navigator, 'clipboard', { value: undefined, configurable: true });
    execCommand(vi.fn().mockReturnValue(false));

    component.copy();

    expect(component.copyState).toBe('failed');
    expect(document.querySelector('textarea')).toBeNull();
  });

  it('says so when there is no clipboard api and no execCommand either', () => {
    Object.defineProperty(navigator, 'clipboard', { value: undefined, configurable: true });

    component.copy();

    expect(component.copyState).toBe('failed');
  });

  it('copies everything on Ctrl+C when nothing is selected', () => {
    const writeText = vi.fn().mockResolvedValue(undefined);
    clipboard(writeText);
    component.xml = '<definitions />';
    vi.spyOn(window, 'getSelection').mockReturnValue({ toString: () => '' } as any);

    const event = new KeyboardEvent('keydown', { key: 'c', ctrlKey: true, cancelable: true });
    component.onKeydown(event);

    expect(writeText).toHaveBeenCalled();
    expect(event.defaultPrevented).toBe(true);
  });

  it('leaves a real selection to the browser', () => {
    // Taking the keystroke here would copy the whole document over the few lines the user
    // deliberately highlighted.
    const writeText = vi.fn();
    clipboard(writeText);
    vi.spyOn(window, 'getSelection').mockReturnValue({ toString: () => '<bpmn:process' } as any);

    const event = new KeyboardEvent('keydown', { key: 'c', ctrlKey: true, cancelable: true });
    component.onKeydown(event);

    expect(writeText).not.toHaveBeenCalled();
    expect(event.defaultPrevented).toBe(false);
  });

  it('ignores other keys', () => {
    const writeText = vi.fn();
    clipboard(writeText);

    component.onKeydown(new KeyboardEvent('keydown', { key: 'v', ctrlKey: true }));
    component.onKeydown(new KeyboardEvent('keydown', { key: 'c' }));

    expect(writeText).not.toHaveBeenCalled();
  });

  it('dismisses the modal on close', () => {
    component.close();

    expect(activeModal.dismiss).toHaveBeenCalled();
  });
});
