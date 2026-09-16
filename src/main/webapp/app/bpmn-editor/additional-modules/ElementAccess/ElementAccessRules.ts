import RuleProvider from 'diagram-js/lib/features/rules/RuleProvider';
import { BpmnElementAccessConfig, isBpmnTypeAllowed } from '../../services/bpmn-element-access.types';

const PRIORITY = 3000;

type Candidate = { type?: string } | null | undefined;

/**
 * Authoritative client-side creation guard.
 *
 * Palette filtering is UX only.  Context pad, keyboard actions and programmatic creation all run
 * through diagram-js rules, so disallowed shapes are rejected here as a second browser-side layer.
 * The Java FlowResource performs the final server-side validation before persistence.
 */
export default class ElementAccessRules extends RuleProvider {
  static $inject = ['eventBus', 'config.elementAccess'];

  constructor(eventBus: any, private readonly elementAccess?: BpmnElementAccessConfig) {
    super(eventBus);
  }

  init(): void {
    const guard = (context: Record<string, Candidate>): boolean | undefined => {
      const candidate = context['shape'] ?? context['newShape'] ?? context['newData'] ?? context['replacement'];
      const type = candidate?.type;
      if (!type) {
        return undefined;
      }
      return isBpmnTypeAllowed(this.elementAccess, type) ? undefined : false;
    };

    this.addRule('shape.create', PRIORITY, guard);
    this.addRule('shape.append', PRIORITY, guard);
    this.addRule('shape.replace', PRIORITY, guard);
  }
}
