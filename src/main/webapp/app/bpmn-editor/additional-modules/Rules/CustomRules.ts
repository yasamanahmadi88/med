import RuleProvider from 'diagram-js/lib/features/rules/RuleProvider';
import { Base } from 'diagram-js/lib/model';
import { isCreatableModuleType } from '../Palette/integration-modules';

/**
 * Keeps a diagram's start and end events from being deleted, and prevents pasting
 * non-creatable integration modules (Merger, Fragmenter, Kafka, HTTP, DB, CDR, CSV).
 *
 * Ported from the Vue editor's `CustomRules`, which originally defined only the delete rule.
 * Without it a user can select the start event and press Delete, leaving a process that no
 * engine will run and that the editor gives no obvious way to repair — the palette can place
 * a new start event, but nothing says one is missing.
 *
 * The paste rule prevents users from circumventing the palette allowlist by copy/pasting
 * non-creatable modules from legacy diagrams. While these modules remain fully supported in
 * existing flows, they cannot be created or restored via paste.
 *
 * The rules are additive: everything diagram-js otherwise allows still applies.
 */

/** Above the default rules so this answer is the one that stands. Matches the Vue priority. */
const PRIORITY = 2000;

/** The element types a diagram cannot do without. */
const UNDELETABLE = ['bpmn:StartEvent', 'bpmn:EndEvent'];

export default class CustomRules extends RuleProvider {
  static $inject = ['eventBus'];

  constructor(private eventBus: any) {
    super(eventBus);
  }

  // No constructor: RuleProvider's own calls `init()`, which is where the rule is registered.
  init(): void {
    // `elements.delete` is asked to approve a whole selection at once. Returning the elements
    // that may go — rather than false — lets a mixed selection delete everything else, which is
    // what a user dragging a box over half the diagram expects. The Vue rule returned a plain
    // boolean, so one protected element in the selection silently blocked the entire delete.
    this.addRule('elements.delete', PRIORITY, (context: { elements: Base[] }) =>
      context.elements.filter(element => !UNDELETABLE.includes(element.type)),
    );

    // Prevent pasting non-creatable module types by listening to the copyPaste.pasteElements event.
    // Elements that cannot be created via the palette also cannot be created or restored via copy/paste.
    // This enforces the creatable modules allowlist across all creation paths, not just the palette.
    this.eventBus.on('copyPaste.pasteElements', (event: any) => {
      const hasNonCreatable = event.elements.some((element: Base) => {
        // Creatable module types are allowed.
        if (isCreatableModuleType(element.type)) {
          return false;
        }
        // Standard BPMN types (start with 'bpmn:') are allowed.
        if (element.type?.startsWith('bpmn:')) {
          return false;
        }
        // Non-creatable custom module types are not allowed.
        return true;
      });

      // If any non-creatable modules are present, prevent the paste operation entirely.
      if (hasNonCreatable) {
        event.preventDefault();
      }
    });
  }
}
