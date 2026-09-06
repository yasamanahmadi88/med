import RuleProvider from 'diagram-js/lib/features/rules/RuleProvider';
import { Base } from 'diagram-js/lib/model';

/**
 * Keeps a diagram's start and end events from being deleted.
 *
 * Ported from the Vue editor's `CustomRules`, which is the only rule it defined. Without it a
 * user can select the start event and press Delete, leaving a process that no engine will run
 * and that the editor gives no obvious way to repair — the palette can place a new start event,
 * but nothing says one is missing.
 *
 * The rule is additive: everything diagram-js otherwise allows still applies.
 */

/** Above the default rules so this answer is the one that stands. Matches the Vue priority. */
const PRIORITY = 2000;

/** The element types a diagram cannot do without. */
const UNDELETABLE = ['bpmn:StartEvent', 'bpmn:EndEvent'];

export default class CustomRules extends RuleProvider {
  static $inject = ['eventBus'];

  // No constructor: RuleProvider's own calls `init()`, which is where the rule is registered.
  init(): void {
    // `elements.delete` is asked to approve a whole selection at once. Returning the elements
    // that may go — rather than false — lets a mixed selection delete everything else, which is
    // what a user dragging a box over half the diagram expects. The Vue rule returned a plain
    // boolean, so one protected element in the selection silently blocked the entire delete.
    this.addRule('elements.delete', PRIORITY, (context: { elements: Base[] }) =>
      context.elements.filter(element => !UNDELETABLE.includes(element.type)),
    );
  }
}
