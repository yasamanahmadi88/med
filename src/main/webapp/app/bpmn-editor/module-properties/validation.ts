import { getBusinessObject } from 'bpmn-js/lib/util/ModelUtil';
import { Base } from 'diagram-js/lib/model';

import { schemaForType } from './schemas';
import { validateField } from './validators';

/**
 * Whole-diagram validation of the integration-module properties.
 *
 * The properties panel validates one element at a time — whichever is selected — and that is all
 * the Vue editor ever knew. Its stores were keyed by element, but each field component cleared
 * its own error in `onUnmounted`, so selecting a different element erased the record of the
 * invalid one and the Save button went back to enabled with the bad value still in the diagram.
 *
 * This walks the element registry instead, so what gates Save is the state of the whole diagram,
 * including elements the user has never selected and values that arrived through an import.
 */

/** One invalid property, named well enough for the user to go and find it. */
export interface ModulePropertyProblem {
  readonly elementId: string;
  /** The element's label when it has one, its id otherwise. */
  readonly elementName: string;
  readonly moduleLabel: string;
  readonly fieldLabel: string;
  readonly message: string;
}

interface ElementRegistry {
  getAll(): Base[];
}

interface ValidatableModeler {
  get(name: string, strict?: boolean): unknown;
}

/**
 * Every invalid module property in the diagram, in element order.
 *
 * Returns an empty array when there is no modeler, so a caller can gate on `length` without
 * having to know whether the editor has finished starting up.
 */
export function moduleValidationProblems(modeler: ValidatableModeler | null | undefined): ModulePropertyProblem[] {
  if (!modeler) {
    return [];
  }

  const elementRegistry = modeler.get('elementRegistry', false) as ElementRegistry | undefined;
  if (!elementRegistry) {
    return [];
  }

  // The engine prefix every module property is namespaced under, resolved the same way
  // ModulePropertiesProvider resolves it.
  const prefix = (modeler.get('config.processEngine', false) as string | undefined) ?? 'camunda';

  const problems: ModulePropertyProblem[] = [];

  for (const element of elementRegistry.getAll()) {
    const businessObject = getBusinessObject(element);
    const schema = schemaForType(businessObject?.$type);
    if (!schema) {
      continue;
    }

    for (const field of schema.fields) {
      if (!field.validate) {
        continue;
      }
      const message = validateField(field.validate, businessObject.get(`${prefix}:${field.name}`));
      if (message) {
        problems.push({
          elementId: element.id,
          elementName: businessObject.name || element.id,
          moduleLabel: schema.label,
          fieldLabel: field.label,
          message,
        });
      }
    }
  }

  return problems;
}

/** A one-line summary of a problem, for a list or a tooltip. */
export function describeProblem(problem: ModulePropertyProblem): string {
  return `${problem.elementName} — ${problem.fieldLabel}: ${problem.message}`;
}
