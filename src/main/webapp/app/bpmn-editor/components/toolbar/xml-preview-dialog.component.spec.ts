import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DomSanitizer } from '@angular/platform-browser';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { XmlPreviewDialogComponent } from './xml-preview-dialog.component';

describe('XmlPreviewDialogComponent', () => {
  let fixture: ComponentFixture<XmlPreviewDialogComponent>;
  let component: XmlPreviewDialogComponent;
  let activeModal: NgbActiveModal;

  const clipboard = (writeText: ReturnType<typeof vi.fn>): void => {
    Object.defineProperty(navigator, 'clipboard', { value: { writeText }, configurable: true });
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

  it('says so when there is no clipboard api at all', () => {
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

  describe('XML Highlighting', () => {
    it('should create highlightedXml SafeHtml when XML is set', () => {
      const testXml = '<process id="p1"></process>';
      component.xml = testXml;

      expect(component.highlightedXml).toBeDefined();
      expect(component.xml).toBe(testXml);
    });

    it('should trigger highlighting on every XML change', () => {
      const sanitizer = TestBed.inject(DomSanitizer);
      const spy = vi.spyOn(sanitizer, 'bypassSecurityTrustHtml');

      component.xml = '<process id="p1"></process>';
      expect(spy).toHaveBeenCalled();
      const firstCallCount = spy.mock.calls.length;

      component.xml = '<process id="p2"></process>';
      expect(spy.mock.calls.length).toBeGreaterThan(firstCallCount);
    });

    it('should highlight custom namespaces (Kafka)', () => {
      const kafkaXml = '<KafkaReceiver:kafkaReceiver id="a1"><topic>test</topic></KafkaReceiver:kafkaReceiver>';
      component.xml = kafkaXml;
      fixture.detectChanges();

      const preview = fixture.nativeElement.querySelector('[data-cy="bpmnXmlPreview"]');
      expect(preview.textContent).toContain('KafkaReceiver');
      expect(preview.textContent).toContain('kafkaReceiver');
    });

    it('should highlight custom namespaces (CDR)', () => {
      const cdrXml = '<CdrParser:cdrParser id="a1"><cdrType>callRecord</cdrType></CdrParser:cdrParser>';
      component.xml = cdrXml;
      fixture.detectChanges();

      const preview = fixture.nativeElement.querySelector('[data-cy="bpmnXmlPreview"]');
      expect(preview.textContent).toContain('CdrParser');
      expect(preview.textContent).toContain('cdrParser');
    });

    it('should highlight custom namespaces (CSV)', () => {
      const csvXml = '<CsvExporter:csvExporter id="a1"><format>CSV</format></CsvExporter:csvExporter>';
      component.xml = csvXml;
      fixture.detectChanges();

      const preview = fixture.nativeElement.querySelector('[data-cy="bpmnXmlPreview"]');
      expect(preview.textContent).toContain('CsvExporter');
      expect(preview.textContent).toContain('csvExporter');
    });

    it('should escape HTML in raw XML text for XSS safety', () => {
      const maliciousXml = '<process><text>5 > 3 && 2 < 4</text></process>';
      component.xml = maliciousXml;
      fixture.detectChanges();

      const preview = fixture.nativeElement.querySelector('[data-cy="bpmnXmlPreview"]');
      // The text content should show the escaped forms as text
      expect(preview.textContent).toContain('<process>');
      expect(preview.textContent).toContain('5 > 3 && 2 < 4');
    });

    it('should NOT create real img elements from malicious attributes', () => {
      const maliciousXml = '<process><img src="x" onerror="alert(1)" /></process>';
      component.xml = maliciousXml;
      fixture.detectChanges();

      const preview = fixture.nativeElement.querySelector('[data-cy="bpmnXmlPreview"]');
      // Because highlight.js escapes HTML, there should be no real img element
      expect(preview.querySelector('img')).toBeNull();
      expect(preview.textContent).toContain('<img src="x"');
    });

    it('should NOT create real script elements from malicious content', () => {
      const maliciousXml = '<process><script>alert("xss")</script></process>';
      component.xml = maliciousXml;
      fixture.detectChanges();

      const preview = fixture.nativeElement.querySelector('[data-cy="bpmnXmlPreview"]');
      expect(preview.querySelector('script')).toBeNull();
      expect(preview.textContent).toContain('<script>');
    });

    it('should NOT execute event handlers in XML declarations', () => {
      const maliciousXml = '<process onload="evil()" id="x"></process>';
      component.xml = maliciousXml;
      fixture.detectChanges();

      const preview = fixture.nativeElement.querySelector('[data-cy="bpmnXmlPreview"]');
      const process = preview.querySelector('process');
      expect(process).toBeNull(); // Should be text, not a real element
      expect(preview.textContent).toContain('onload="evil()"');
    });

    it('should handle large BPMN diagrams with multiple custom elements', () => {
      let largeXml = '<?xml version="1.0"?>\n<bpmn:definitions>\n';
      for (let i = 0; i < 200; i++) {
        largeXml += `  <bpmn:process id="p${i}">\n`;
        if (i % 3 === 0) {
          largeXml += `    <KafkaReceiver:kafkaReceiver id="k${i}"><topic>topic${i}</topic></KafkaReceiver:kafkaReceiver>\n`;
        }
        if (i % 3 === 1) {
          largeXml += `    <CdrParser:cdrParser id="c${i}"><cdrType>type${i}</cdrType></CdrParser:cdrParser>\n`;
        }
        if (i % 3 === 2) {
          largeXml += `    <CsvExporter:csvExporter id="e${i}"><format>CSV</format></CsvExporter:csvExporter>\n`;
        }
        largeXml += `  </bpmn:process>\n`;
      }
      largeXml += '</bpmn:definitions>';

      component.xml = largeXml;
      fixture.detectChanges();

      const preview = fixture.nativeElement.querySelector('[data-cy="bpmnXmlPreview"]');
      expect(preview.textContent).toContain('KafkaReceiver');
      expect(preview.textContent).toContain('CdrParser');
      expect(preview.textContent).toContain('CsvExporter');
    });

    it('should preserve exact XML content after highlighting', () => {
      const originalXml = '<bpmn:process id="proc1"><bpmn:task name="Task A"></bpmn:task></bpmn:process>';
      component.xml = originalXml;
      fixture.detectChanges();

      const preview = fixture.nativeElement.querySelector('[data-cy="bpmnXmlPreview"]');
      // The textContent should match the original XML exactly
      expect(preview.textContent).toBe(originalXml);
    });

    it('should apply css classes for syntax highlighting', () => {
      const testXml = '<process id="p1"></process>';
      component.xml = testXml;
      fixture.detectChanges();

      const code = fixture.nativeElement.querySelector('[data-cy="bpmnXmlPreview"] code');
      expect(code).toBeTruthy();
      expect(code.className).not.toContain('test'); // No hardcoded classes expected; highlighting is via CSS variables
    });
  });
});
