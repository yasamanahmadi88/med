import CustomRules from './CustomRules';

/**
 * Deleting the start event leaves a process no engine will run, and the editor gives no hint
 * that one is missing — so this rule is the only thing standing between a stray Delete key and
 * a broken diagram. These specs pin what it blocks and, just as important, what it still lets
 * through.
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
