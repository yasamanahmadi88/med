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

  describe('paste rule', () => {
    it('registers on elements.paste above the default rules', () => {
      const { rules } = build();

      expect(rules['elements.paste']).toBeDefined();
      expect(rules['elements.paste'].priority).toBe(2000);
    });

    it('allows pasting stock BPMN elements (tasks, gateways, events)', () => {
      const { rules } = build();
      const allow = rules['elements.paste'].fn;
      const bpmnElements = [
        element('bpmn:Task'),
        element('bpmn:ExclusiveGateway'),
        element('bpmn:ParallelGateway'),
        element('bpmn:ServiceTask'),
      ];

      expect(allow({ elements: bpmnElements })).toEqual(bpmnElements);
    });

    it('allows pasting creatable modules (FileReceiver, FileTransmitter)', () => {
      const { rules } = build();
      const allow = rules['elements.paste'].fn;
      const creatableModules = [element('FileReceiver:FileReceiver'), element('FileTransmitter:FileTransmitter')];

      expect(allow({ elements: creatableModules })).toEqual(creatableModules);
    });

    it('blocks pasting non-creatable modules (Merger, Fragmenter, Kafka, HTTP, DB, CDR, CSV)', () => {
      const { rules } = build();
      const allow = rules['elements.paste'].fn;
      const nonCreatableModules = [
        element('Merger:Merger'),
        element('Fragmenter:Fragmenter'),
        element('KafkaReceiver:KafkaReceiver'),
        element('HttpReceiver:HttpReceiver'),
        element('DbReceiver:DbReceiver'),
        element('CdrParser:CdrParser'),
        element('CsvTransformer:CsvTransformer'),
      ];

      expect(allow({ elements: nonCreatableModules })).toEqual([]);
    });

    it('allows pasting a mix of creatable modules and BPMN elements, filtering out non-creatable modules', () => {
      const { rules } = build();
      const allow = rules['elements.paste'].fn;
      const task = element('bpmn:Task');
      const fileReceiver = element('FileReceiver:FileReceiver');
      const merger = element('Merger:Merger');
      const gateway = element('bpmn:ExclusiveGateway');

      const allowed = allow({
        elements: [task, fileReceiver, merger, gateway],
      });

      expect(allowed).toEqual([task, fileReceiver, gateway]);
    });

    it('handles an empty selection', () => {
      const { rules } = build();

      expect(rules['elements.paste'].fn({ elements: [] })).toEqual([]);
    });
  });
});
