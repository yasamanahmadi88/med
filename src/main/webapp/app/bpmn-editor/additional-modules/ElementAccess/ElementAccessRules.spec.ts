import RuleProvider from 'diagram-js/lib/features/rules/RuleProvider';

import { BpmnElementAccessConfig } from '../../services/bpmn-element-access.types';
import ElementAccessRules from './ElementAccessRules';

type RuleGuard = (context: Record<string, any>) => boolean | undefined;

interface RegisteredRule {
  priority: number;
  guard: RuleGuard;
}

describe('ElementAccessRules', () => {
  const mciConfig: BpmnElementAccessConfig = {
    allowedTypes: [
      'Merger:Merger',
      'Fragmenter:Fragmenter',
      'FileReceiver:FileReceiver',
      'FileTransmitter:FileTransmitter',
      'CdrParser:CdrParser',
      'CsvTransformer:CsvTransformer',
    ],
    allowedPaletteActions: [],
    allowedXmlElements: [],
  };

  let registrations: Map<string, RegisteredRule>;
  let addRuleSpy: any;

  beforeEach(() => {
    registrations = new Map<string, RegisteredRule>();

    addRuleSpy = vi.spyOn(RuleProvider.prototype as any, 'addRule').mockImplementation((...args: unknown[]) => {
      const [actions, priority, guard] = args as [string | string[], number, RuleGuard];
      const actionList = Array.isArray(actions) ? actions : [actions];

      for (const action of actionList) {
        registrations.set(action, { priority, guard });
      }
    });
  });

  afterEach(() => {
    addRuleSpy.mockRestore();
  });

  function createRules(config: BpmnElementAccessConfig = mciConfig): ElementAccessRules {
    return new ElementAccessRules({}, config);
  }

  function rule(action: string): RegisteredRule {
    const registered = registrations.get(action);

    if (!registered) {
      throw new Error(`Rule was not registered: ${action}`);
    }

    return registered;
  }

  it('registers create, append and replace guards at priority 3000', () => {
    createRules();

    expect([...registrations.keys()]).toEqual(['shape.create', 'shape.append', 'shape.replace']);

    expect(rule('shape.create').priority).toBe(3000);
    expect(rule('shape.append').priority).toBe(3000);
    expect(rule('shape.replace').priority).toBe(3000);
  });

  it('rejects a disallowed type for shape.create and shape.append', () => {
    createRules();

    expect(
      rule('shape.create').guard({
        shape: { type: 'KafkaReceiver:KafkaReceiver' },
      }),
    ).toBe(false);

    expect(
      rule('shape.append').guard({
        shape: { type: 'KafkaReceiver:KafkaReceiver' },
        source: { type: 'Merger:Merger' },
      }),
    ).toBe(false);
  });

  it('rejects a disallowed shape.replace destination from newData', () => {
    createRules();

    const result = rule('shape.replace').guard({
      oldShape: { type: 'Merger:Merger' },
      newData: { type: 'KafkaReceiver:KafkaReceiver' },
    });

    expect(result).toBe(false);
  });

  it('passes allowed MCI types through to lower-priority bpmn-js rules', () => {
    createRules();

    expect(
      rule('shape.create').guard({
        shape: { type: 'Merger:Merger' },
      }),
    ).toBeUndefined();

    expect(
      rule('shape.append').guard({
        shape: { type: 'FileReceiver:FileReceiver' },
      }),
    ).toBeUndefined();

    expect(
      rule('shape.replace').guard({
        oldShape: { type: 'KafkaReceiver:KafkaReceiver' },
        newData: { type: 'CsvTransformer:CsvTransformer' },
      }),
    ).toBeUndefined();
  });

  it('fails closed for modeling candidates when access configuration is empty', () => {
    createRules({
      allowedTypes: [],
      allowedPaletteActions: [],
      allowedXmlElements: [],
    });

    expect(
      rule('shape.create').guard({
        shape: { type: 'Merger:Merger' },
      }),
    ).toBe(false);

    expect(
      rule('shape.replace').guard({
        oldShape: { type: 'KafkaReceiver:KafkaReceiver' },
        newData: { type: 'Merger:Merger' },
      }),
    ).toBe(false);
  });

  it('does not interfere when the command context has no candidate type', () => {
    createRules();

    expect(rule('shape.create').guard({})).toBeUndefined();

    expect(
      rule('shape.replace').guard({
        oldShape: { type: 'Merger:Merger' },
      }),
    ).toBeUndefined();
  });
});
