import { CheckboxEntry, NumberFieldEntry, SelectEntry, TextAreaEntry, TextFieldEntry } from '@bpmn-io/properties-panel';
import { getBusinessObject } from 'bpmn-js/lib/util/ModelUtil';
import { Base } from 'diagram-js/lib/model';

import { ModuleField, ModuleSchema } from './schema';
import { schemaForType } from './schemas';

/**
 * Adds one properties-panel group per custom integration module, built from `schema.ts`.
 *
 * The Vue editor wrote a component per field — 152 of them across 17 modules. Each did the same
 * thing: read a moddle property, render a control, write it back. This provider does that once,
 * driven by the declaration, so adding a module is a schema entry rather than a dozen files.
 *
 * Registered at a priority above the default provider so the module group appears at the top,
 * where the Vue panel put it.
 */
const PRIORITY = 500;

/** Entry component per field kind. `checkbox` is here for modules that use one. */
const ENTRY_COMPONENTS = {
  text: TextFieldEntry,
  textarea: TextAreaEntry,
  select: SelectEntry,
  number: NumberFieldEntry,
  checkbox: CheckboxEntry,
} as const;

interface Modeling {
  updateModdleProperties(element: Base, moddleElement: unknown, properties: Record<string, unknown>): void;
}

interface PropertiesPanel {
  registerProvider(priority: number, provider: unknown): void;
}

type Translate = (template: string) => string;

export default class ModulePropertiesProvider {
  static $inject = ['propertiesPanel', 'modeling', 'translate', 'injector'];

  private readonly modeling: Modeling;
  private readonly translate: Translate;
  /** The editor's configured process engine namespaces every property this panel writes. */
  private readonly prefix: string;

  constructor(propertiesPanel: PropertiesPanel, modeling: Modeling, translate: Translate, injector: any) {
    this.modeling = modeling;
    this.translate = translate;
    // `config.processEngine` is supplied when the modeler is built; camunda is the default
    // engine and the one the stock properties panel is registered for.
    this.prefix = injector.get('config.processEngine', false) ?? 'camunda';

    propertiesPanel.registerProvider(PRIORITY, this);
  }

  getGroups(element: Base): (groups: unknown[]) => unknown[] {
    return groups => {
      const schema = schemaForType(getBusinessObject(element)?.$type);
      if (!schema) {
        return groups;
      }
      return [this.moduleGroup(element, schema), ...groups];
    };
  }

  private moduleGroup(element: Base, schema: ModuleSchema): unknown {
    return {
      id: `module-${schema.type}`,
      label: this.translate(schema.label),
      entries: schema.fields.map(field => this.entry(element, field)),
    };
  }

  private entry(element: Base, field: ModuleField): unknown {
    const property = `${this.prefix}:${field.name}`;

    // `getValue`/`setValue` are called by the entry component with the element it was given, so
    // they close over the property name rather than reading it back off the DOM.
    const getValue = (): unknown => getBusinessObject(element)?.get(property);
    const setValue = (value: unknown): void => {
      this.modeling.updateModdleProperties(element, getBusinessObject(element), { [property]: value });
    };

    const entry: Record<string, unknown> = {
      id: field.name,
      element,
      label: this.translate(field.label),
      component: ENTRY_COMPONENTS[field.kind],
      getValue,
      setValue,
      debounce: (fn: unknown) => fn,
    };

    if (field.kind === 'select') {
      // The panel renders an empty first option so a property can be cleared, matching the Vue
      // form, where the select started blank until the user picked a value.
      entry['getOptions'] = () => [
        { value: '', label: '' },
        ...(field.options ?? []).map(option =>
          // A bare string is a choice whose stored value reads well enough to show as-is; the
          // pair form carries the Vue template's display text, which often differed.
          typeof option === 'string' ? { value: option, label: option } : { value: option.value, label: option.label },
        ),
      ];
    }

    return entry;
  }
}
