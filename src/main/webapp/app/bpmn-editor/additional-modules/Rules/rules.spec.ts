import CustomRules from './CustomRules';

/**
 * Deleting the start event leaves a process no engine will run, and the editor gives no hint
 * that one is missing — so the delete rule is the only thing standing between a stray Delete
 * key and a broken diagram. The paste rule enforces the palette's creatable modules allowlist
 * across all creation paths, preventing non-creatable modules from being restored via copy/paste.
 * These specs pin what each blocks and, just as important, what they still let through.
 */
describe('CustomRules', () => {
  const build = (): { rules: Record<string, { priority: number; fn: (context: any) => unknown }> } => {
    const rules: Record<string, { priority: number; fn: (context: any) => unknown }> = {};
    const eventBus = { on: vi.fn() };

    const provider = new CustomRules(eventBus as any);
    // RuleProvider.addRule is what init() calls; capture what it registered.
    (provider as any).addRule = (action: string, priority: number, fn: (context: any) => unknown) => {
      rules[action] = { priority, fn };
    };
    provider.init();

    return { rules };
  };

  const element = (type: string, id = type): any => ({ type, id });

  describe('delete rule', () => {
    it('registers on elements.delete above the default rules', () => {
      // A lower priority loses to diagram-js's own answer and the rule never applies.
      const { rules } = build();

      expect(rules['elements.delete']).toBeDefined();
      expect(rules['elements.delete'].priority).toBe(2000);
    });

    it('refuses to delete a start or end event', () => {
      const { rules } = build();
      const allow = rules['elements.delete'].fn;

      expect(allow({ elements: [element('bpmn:StartEvent')] })).toEqual([]);
      expect(allow({ elements: [element('bpmn:EndEvent')] })).toEqual([]);
    });

    it('lets every other element go', () => {
      const { rules } = build();
      const elements = [element('bpmn:Task'), element('bpmn:ExclusiveGateway'), element('bpmn:SequenceFlow')];

      expect(rules['elements.delete'].fn({ elements })).toEqual(elements);
    });

    it('deletes the rest of a mixed selection instead of blocking all of it', () => {
      // Dragging a box over half the diagram should not become a no-op because one start event
      // was caught in it. The Vue rule returned a bare boolean and did exactly that.
      const { rules } = build();
      const task = element('bpmn:Task');
      const gateway = element('bpmn:ExclusiveGateway');

      const allowed = rules['elements.delete'].fn({
        elements: [task, element('bpmn:StartEvent'), gateway, element('bpmn:EndEvent')],
      });

      expect(allowed).toEqual([task, gateway]);
    });

    it('handles an empty selection', () => {
      const { rules } = build();

      expect(rules['elements.delete'].fn({ elements: [] })).toEqual([]);
    });
  });

  describe('paste event handler', () => {
    it('listens to copyPaste.pasteElements event', () => {
      const eventBus = { on: vi.fn() };
      const provider = new CustomRules(eventBus as any);
      provider.init();

      expect(eventBus.on).toHaveBeenCalledWith('copyPaste.pasteElements', expect.any(Function));
    });

    it('allows pasting stock BPMN elements (tasks, gateways, events)', () => {
      const eventBus = { on: vi.fn() };
      const provider = new CustomRules(eventBus as any);
      provider.init();

      const handler = (eventBus.on as any).mock.calls.find(call => call[0] === 'copyPaste.pasteElements')[1];
      const bpmnElements = [
        element('bpmn:Task'),
        element('bpmn:ExclusiveGateway'),
        element('bpmn:ParallelGateway'),
        element('bpmn:ServiceTask'),
      ];
      const event = { elements: [...bpmnElements] };

      handler(event);

      expect(event.elements).toEqual(bpmnElements);
    });

    it('allows pasting creatable modules (FileReceiver, FileTransmitter)', () => {
      const eventBus = { on: vi.fn() };
      const provider = new CustomRules(eventBus as any);
      provider.init();

      const handler = (eventBus.on as any).mock.calls.find(call => call[0] === 'copyPaste.pasteElements')[1];
      const creatableModules = [element('FileReceiver:FileReceiver'), element('FileTransmitter:FileTransmitter')];
      const event = { elements: [...creatableModules] };

      handler(event);

      expect(event.elements).toEqual(creatableModules);
    });

    it('blocks pasting non-creatable modules (Merger, Fragmenter, Kafka, HTTP, DB, CDR, CSV)', () => {
      const eventBus = { on: vi.fn() };
      const provider = new CustomRules(eventBus as any);
      provider.init();

      const handler = (eventBus.on as any).mock.calls.find(call => call[0] === 'copyPaste.pasteElements')[1];
      const nonCreatableModules = [
        element('Merger:Merger'),
        element('Fragmenter:Fragmenter'),
        element('KafkaReceiver:KafkaReceiver'),
        element('HttpReceiver:HttpReceiver'),
        element('DbReceiver:DbReceiver'),
        element('CdrParser:CdrParser'),
        element('CsvTransformer:CsvTransformer'),
      ];
      const event = { elements: [...nonCreatableModules] };

      handler(event);

      expect(event.elements).toEqual([]);
    });

    it('filters out non-creatable modules from a mix of creatable modules and BPMN elements', () => {
      const eventBus = { on: vi.fn() };
      const provider = new CustomRules(eventBus as any);
      provider.init();

      const handler = (eventBus.on as any).mock.calls.find(call => call[0] === 'copyPaste.pasteElements')[1];
      const task = element('bpmn:Task');
      const fileReceiver = element('FileReceiver:FileReceiver');
      const merger = element('Merger:Merger');
      const gateway = element('bpmn:ExclusiveGateway');
      const event = { elements: [task, fileReceiver, merger, gateway] };

      handler(event);

      expect(event.elements).toEqual([task, fileReceiver, gateway]);
    });

    it('handles an empty selection', () => {
      const eventBus = { on: vi.fn() };
      const provider = new CustomRules(eventBus as any);
      provider.init();

      const handler = (eventBus.on as any).mock.calls.find(call => call[0] === 'copyPaste.pasteElements')[1];
      const event = { elements: [] };

      handler(event);

      expect(event.elements).toEqual([]);
    });
  });
});
