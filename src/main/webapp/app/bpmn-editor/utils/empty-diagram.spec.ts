import { createNewDiagram, emptyDiagramXml, newDiagramIdentity } from './empty-diagram';

/**
 * A new diagram's process id and name are what the backend keys a flow on, so getting them
 * wrong is not cosmetic — the diagram either cannot be saved under the name the user chose or,
 * when the XML is malformed, does not open at all. These specs pin both.
 */
describe('empty diagram', () => {
  describe('emptyDiagramXml', () => {
    it('carries the id on the process, the definitions and the plane', () => {
      // bpmn-js resolves the plane through `bpmnElement`; a mismatch imports as a blank canvas.
      const xml = emptyDiagramXml('Process_42', 'Order intake');

      expect(xml).toContain('id="Definitions_Process_42"');
      expect(xml).toContain('<bpmn:process id="Process_42" name="Order intake" isExecutable="true">');
      expect(xml).toContain('bpmnElement="Process_42"');
    });

    it('marks the process executable', () => {
      // The engine skips a non-executable process, so a flow drawn here would never run.
      expect(emptyDiagramXml('P', 'n')).toContain('isExecutable="true"');
    });

    it('opens with a start event that has a shape to render', () => {
      // Without this the canvas comes up blank: a process with no start event is not
      // executable and gives the user nothing to drag from. The Vue original was blank;
      // `modeler.createDiagram()`, which this replaces, was not. An element without matching
      // DI is dropped on import, so the shape has to be here too.
      const xml = emptyDiagramXml('P', 'n');

      expect(xml).toContain('<bpmn:startEvent id="StartEvent_1" />');
      expect(xml).toContain('bpmnElement="StartEvent_1"');
      expect(xml).toContain('<dc:Bounds x="173" y="102" width="36" height="36" />');
    });

    it('escapes a name holding XML syntax', () => {
      // The settings form accepts these characters; interpolated raw they close the attribute
      // and the document fails to parse. The Vue original did interpolate them raw.
      const xml = emptyDiagramXml('P', 'R&D "spike" <draft>');

      expect(xml).toContain('name="R&amp;D &quot;spike&quot; &lt;draft&gt;"');
      expect(xml).not.toContain('name="R&D');
    });

    it('declares the namespaces bpmn-js needs to import it', () => {
      const xml = emptyDiagramXml('P', 'n');

      for (const ns of ['xmlns:bpmn=', 'xmlns:bpmndi=', 'xmlns:dc=', 'xmlns:di=']) {
        expect(xml).toContain(ns);
      }
    });
  });

  describe('newDiagramIdentity', () => {
    it('uses the configured id and name', () => {
      expect(newDiagramIdentity({ processId: 'Order_1', processName: 'Orders' })).toEqual({
        processId: 'Order_1',
        processName: 'Orders',
      });
    });

    it('falls back to a timestamp when the settings are blank', () => {
      // Matches the Vue defaults, so a diagram created without settings looks unchanged.
      const identity = newDiagramIdentity({ processId: '   ', processName: '' });

      expect(identity.processId).toMatch(/^Process_\d+$/);
      expect(identity.processName).toMatch(/^processName\d+$/);
    });

    it('falls back when no settings are supplied at all', () => {
      expect(newDiagramIdentity(undefined).processId).toMatch(/^Process_\d+$/);
    });

    it('rejects an id that is not a valid NCName', () => {
      // moddle refuses a document whose id starts with a digit or holds a space, and the editor
      // then opens empty. Falling back keeps a usable diagram; the id is visible and editable.
      expect(newDiagramIdentity({ processId: '1Process', processName: 'n' }).processId).toMatch(/^Process_\d+$/);
      expect(newDiagramIdentity({ processId: 'my process', processName: 'n' }).processId).toMatch(/^Process_\d+$/);
    });

    it('keeps an id using the punctuation an NCName allows', () => {
      expect(newDiagramIdentity({ processId: 'order.flow-v2_1', processName: 'n' }).processId).toBe('order.flow-v2_1');
    });

    it('keeps a name that is only unusual, not invalid', () => {
      // Only the id is constrained; a name is free text and must survive verbatim.
      expect(newDiagramIdentity({ processId: 'P', processName: '1 سفارش' }).processName).toBe('1 سفارش');
    });
  });

  describe('createNewDiagram', () => {
    it('imports an empty diagram built from the settings', async () => {
      const modeler = { importXML: vi.fn().mockResolvedValue({}) };

      await createNewDiagram(modeler, { processId: 'Order_1', processName: 'Orders' } as any);

      expect(modeler.importXML).toHaveBeenCalledWith(expect.stringContaining('<bpmn:process id="Order_1" name="Orders"'));
    });

    it('imports the supplied xml verbatim instead of an empty one', async () => {
      const modeler = { importXML: vi.fn().mockResolvedValue({}) };

      await createNewDiagram(modeler, undefined, '<bpmn:definitions />');

      expect(modeler.importXML).toHaveBeenCalledWith('<bpmn:definitions />');
    });

    it('logs import warnings rather than failing', async () => {
      // bpmn-js warns about extension attributes it does not know, which every module property
      // in this editor is; treating those as failures would refuse to open real diagrams.
      const warn = vi.spyOn(console, 'warn').mockImplementation(() => undefined);
      const modeler = { importXML: vi.fn().mockResolvedValue({ warnings: ['unknown attribute'] }) };

      await expect(createNewDiagram(modeler, undefined)).resolves.toBeUndefined();
      expect(warn).toHaveBeenCalledWith('unknown attribute');

      warn.mockRestore();
    });

    it('rejects when the import itself fails', async () => {
      // The caller logs this; swallowing it would leave a blank canvas with no explanation.
      const modeler = { importXML: vi.fn().mockRejectedValue(new Error('malformed')) };

      await expect(createNewDiagram(modeler, undefined)).rejects.toThrow('malformed');
    });
  });
});
