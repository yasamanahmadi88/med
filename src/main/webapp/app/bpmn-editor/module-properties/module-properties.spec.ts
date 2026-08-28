import { NumberFieldEntry, SelectEntry, TextAreaEntry, TextFieldEntry } from '@bpmn-io/properties-panel';

import ModulePropertiesProvider from './ModulePropertiesProvider';
import { httpReceiverSchema, httpTransmitterSchema, schemaForType } from './schema';

/**
 * The provider is what makes a placed integration module configurable at all: without it a
 * KafkaReceiver or HttpReceiver can be drawn but has no properties. These specs pin the wiring —
 * which element types get a group, how a field becomes an entry, and that a value is written
 * under the process engine's namespace. The rendering itself is covered by the Playwright suite.
 */
const businessObjects = new Map<unknown, Record<string, unknown>>();

vi.mock('bpmn-js/lib/util/ModelUtil', () => ({
  getBusinessObject: (element: unknown) => businessObjects.get(element),
}));

describe('module properties', () => {
  const makeElement = (type: string): unknown => {
    const element = {};
    businessObjects.set(element, { $type: type, get: (key: string) => `value-of-${key}` });
    return element;
  };

  const build = (engine?: string): { provider: ModulePropertiesProvider; modeling: { updateModdleProperties: any } } => {
    const modeling = { updateModdleProperties: vi.fn() };
    const propertiesPanel = { registerProvider: vi.fn() };
    const injector = { get: vi.fn().mockReturnValue(engine) };
    const provider = new ModulePropertiesProvider(propertiesPanel, modeling, (s: string) => s, injector);
    return { provider, modeling };
  };

  describe('schemaForType', () => {
    it('finds the schema for the type the palette creates', () => {
      // enhancementPaletteProvider builds this exact moddle type; a mismatch here silently costs
      // the element its whole properties group.
      expect(schemaForType('HttpReceiver:HttpReceiver')).toBe(httpReceiverSchema);
    });

    it('returns nothing for a plain BPMN element or an unset type', () => {
      expect(schemaForType('bpmn:Task')).toBeUndefined();
      expect(schemaForType(undefined)).toBeUndefined();
    });
  });

  describe('getGroups', () => {
    it('leaves the existing groups alone for a plain BPMN element', () => {
      const { provider } = build();
      const existing = [{ id: 'general' }];

      expect(provider.getGroups(makeElement('bpmn:Task') as any)(existing)).toBe(existing);
    });

    it('puts the module group ahead of the stock groups', () => {
      // The Vue panel showed module properties first; keeping that order means a user does not
      // scroll past Camunda's groups to reach the fields they came for.
      const { provider } = build();
      const groups = provider.getGroups(makeElement('HttpReceiver:HttpReceiver') as any)([{ id: 'general' }]) as any[];

      expect(groups).toHaveLength(2);
      expect(groups[0].id).toBe('module-HttpReceiver:HttpReceiver');
      expect(groups[1].id).toBe('general');
    });

    it('builds one entry per declared field, in declaration order', () => {
      const { provider } = build();
      const [group] = provider.getGroups(makeElement('HttpReceiver:HttpReceiver') as any)([]) as any[];

      expect(group.entries.map((e: any) => e.id)).toEqual(httpReceiverSchema.fields.map(f => f.name));
    });

    it('renders each field kind with its matching entry component', () => {
      const { provider } = build();
      const [group] = provider.getGroups(makeElement('HttpReceiver:HttpReceiver') as any)([]) as any[];
      const entry = (id: string): any => group.entries.find((e: any) => e.id === id);

      expect(entry('agreementMode').component).toBe(SelectEntry);
      expect(entry('validator').component).toBe(TextAreaEntry);
      expect(entry('authUserName').component).toBe(TextFieldEntry);
    });

    it('offers a blank choice so a select can be cleared', () => {
      const { provider } = build();
      const [group] = provider.getGroups(makeElement('HttpReceiver:HttpReceiver') as any)([]) as any[];
      const options = group.entries.find((e: any) => e.id === 'agreementMode').getOptions();

      expect(options.map((o: any) => o.value)).toEqual(['', 'RUNNING', 'FETCH_ONLY', 'DRAFT']);
    });
  });

  describe('HttpTransmitter', () => {
    it('finds the schema for the type the palette creates', () => {
      expect(schemaForType('HttpTransmitter:HttpTransmitter')).toBe(httpTransmitterSchema);
    });

    it('carries every field the Vue module had', () => {
      // The Vue editor spread these across 34 components; a field dropped in the port is a
      // setting the user can no longer reach, and nothing else would fail if one went missing.
      expect(httpTransmitterSchema.fields).toHaveLength(34);
    });

    it('builds one entry per declared field, in declaration order', () => {
      const { provider } = build();
      const [group] = provider.getGroups(makeElement('HttpTransmitter:HttpTransmitter') as any)([]) as any[];

      expect(group.entries.map((e: any) => e.id)).toEqual(httpTransmitterSchema.fields.map(f => f.name));
    });

    it('keeps `ContentType` capitalised', () => {
      // Alone in this module the Vue getter reads `${prefix}:ContentType`. Normalising it to
      // `contentType` would silently address an attribute no existing diagram carries.
      const { provider, modeling } = build();
      const element = makeElement('HttpTransmitter:HttpTransmitter');
      const [group] = provider.getGroups(element as any)([]) as any[];

      group.entries.find((e: any) => e.id === 'ContentType').setValue('application/json');
      expect(modeling.updateModdleProperties).toHaveBeenCalledWith(element, expect.anything(), {
        'camunda:ContentType': 'application/json',
      });
    });

    it('offers the http methods the Vue select offered, and no others', () => {
      // The backend rejects a verb outside this list, so an extra option here would only let a
      // user author a diagram that fails at run time.
      const { provider } = build();
      const [group] = provider.getGroups(makeElement('HttpTransmitter:HttpTransmitter') as any)([]) as any[];
      const options = group.entries.find((e: any) => e.id === 'httpMethod').getOptions();

      expect(options.map((o: any) => o.value)).toEqual(['', 'GET', 'POST', 'DELETE', 'PUT']);
    });

    it('keeps the cacheable flag as the strings the Vue select stored', () => {
      // The Vue <option value="0">No</option> wrote '0', not false; a boolean here would not
      // round-trip against diagrams the Vue editor saved.
      const { provider } = build();
      const [group] = provider.getGroups(makeElement('HttpTransmitter:HttpTransmitter') as any)([]) as any[];
      const options = group.entries.find((e: any) => e.id === 'isCacheAble').getOptions();

      expect(options.map((o: any) => o.value)).toEqual(['', '0', '1']);
    });

    it('renders the number fields as number entries', () => {
      // Every delay and timeout is milliseconds; a text entry would let a unit or stray space
      // through into an attribute the engine parses as a long.
      const { provider } = build();
      const [group] = provider.getGroups(makeElement('HttpTransmitter:HttpTransmitter') as any)([]) as any[];
      const entry = (id: string): any => group.entries.find((e: any) => e.id === id);

      expect(entry('partyTimeOut').component).toBe(NumberFieldEntry);
      expect(entry('maxAttempts').component).toBe(NumberFieldEntry);
      expect(entry('overAllTimeOut').component).toBe(NumberFieldEntry);
    });
  });

  describe('reading and writing', () => {
    it('namespaces the property with the configured process engine', () => {
      // The Vue getters read `${editor().getProcessEngine}:agreementMode`, so a diagram authored
      // against activiti must not be read back under camunda.
      const { provider, modeling } = build('activiti');
      const element = makeElement('HttpReceiver:HttpReceiver');
      const [group] = provider.getGroups(element as any)([]) as any[];
      const entry = group.entries.find((e: any) => e.id === 'agreementMode');

      expect(entry.getValue()).toBe('value-of-activiti:agreementMode');

      entry.setValue('DRAFT');
      expect(modeling.updateModdleProperties).toHaveBeenCalledWith(element, expect.anything(), {
        'activiti:agreementMode': 'DRAFT',
      });
    });

    it('falls back to camunda, the engine the stock panel is registered for', () => {
      const { provider, modeling } = build(undefined);
      const element = makeElement('HttpReceiver:HttpReceiver');
      const [group] = provider.getGroups(element as any)([]) as any[];

      group.entries.find((e: any) => e.id === 'validator').setValue('check');
      expect(modeling.updateModdleProperties).toHaveBeenCalledWith(element, expect.anything(), {
        'camunda:validator': 'check',
      });
    });
  });
});
