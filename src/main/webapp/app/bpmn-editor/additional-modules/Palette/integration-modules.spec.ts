import BpmnModdle from 'bpmn-moddle';

import EnhancementPaletteProvider from './EnhancementPalette/enhancementPaletteProvider';
import RewritePaletteProvider from './RewritePalette/rewritePaletteProvider';
import { CREATABLE_MODULE_TYPES, creatableIntegrationModules, isCreatableModuleType } from './integration-modules';
import { moddleExtensionsFor } from '../index';
import { schemaForType } from '../../module-properties/schemas';
import { defaultSettings } from '../../config';

/**
 * The flows this editor builds are file-integration flows, so those are the only integration
 * modules the palette offers. The Kafka, HTTP and DB modules stay in the product — a flow that
 * already carries one has to open, draw and be editable — but they are no longer something a
 * user can add.
 *
 * Both halves matter, so both are pinned here: what the palette offers, and what still resolves
 * for a diagram that was built before the rule.
 */

/** Everything that used to have a palette entry and deliberately no longer does. */
const NON_CREATABLE_TYPES = [
  'Merger:Merger',
  'Fragmenter:Fragmenter',
  'KafkaReceiver:KafkaReceiver',
  'KafkaTransmitter:KafkaTransmitter',
  'HttpReceiver:HttpReceiver',
  'HttpTransmitter:HttpTransmitter',
  'DbReceiver:DbReceiver',
  'DbTransmitter:DbTransmitter',
  'CdrParser:CdrParser',
  'CsvTransformer:CsvTransformer',
];

const paletteStubs = (): { create: any; elementFactory: any; injected: any[] } => {
  const create = { start: vi.fn() };
  const elementFactory = { createShape: vi.fn((attrs: any) => ({ ...attrs, businessObject: {} })) };
  // The tool entries are clicked too — the "nothing places a Kafka module" check walks the whole
  // palette — so the tools need the methods their entries call.
  const spaceTool = { activateSelection: vi.fn() };
  const lassoTool = { activateSelection: vi.fn() };
  const handTool = { activateHand: vi.fn() };
  const globalConnect = { toggle: vi.fn() };
  const injected = [
    { registerProvider: vi.fn() },
    create,
    elementFactory,
    spaceTool,
    lassoTool,
    handTool,
    globalConnect,
    (text: string) => text,
  ];

  return { create, elementFactory, injected };
};

const rewriteEntries = (): Record<string, any> => {
  const { injected } = paletteStubs();
  return new (RewritePaletteProvider as any)(...injected).getPaletteEntries();
};

const enhancementEntries = (): Record<string, any> => {
  const { injected } = paletteStubs();
  return new (EnhancementPaletteProvider as any)(...injected).getPaletteEntries();
};

describe('the creatable integration modules', () => {
  it('is only the file flow modules: File Receiver and File Transmitter', () => {
    expect([...CREATABLE_MODULE_TYPES].sort()).toEqual(['FileReceiver:FileReceiver', 'FileTransmitter:FileTransmitter'].sort());
  });

  it('leaves the Kafka, HTTP, DB, CDR and CSV modules off the list', () => {
    for (const type of NON_CREATABLE_TYPES) {
      expect(isCreatableModuleType(type), type).toBe(false);
    }
  });

  it('keeps an entry declaration for every type it allows', () => {
    // A type on the allowlist with no declaration would silently offer nothing.
    expect(
      creatableIntegrationModules()
        .map(module => module.type)
        .sort(),
    ).toEqual([...CREATABLE_MODULE_TYPES].sort());
  });
});

describe('the palette providers', () => {
  it('offers an activity entry for each creatable module and nothing else', () => {
    for (const [name, entries] of [
      ['rewrite', rewriteEntries()],
      ['enhancement', enhancementEntries()],
    ] as const) {
      const activity = Object.entries(entries).filter(([, entry]) => entry.group === 'activity');

      expect(
        activity.map(([id]) => id),
        name,
      ).toEqual(['create.fileReceiver-module', 'create.FileTransmitter-module']);
    }
  });

  it('offers no way to place a Kafka, HTTP, DB, CDR or CSV module', () => {
    const { injected, elementFactory } = paletteStubs();
    const entries: Record<string, any> = new (RewritePaletteProvider as any)(...injected).getPaletteEntries();

    for (const entry of Object.values(entries)) {
      entry.action?.click?.(new Event('click'));
    }

    const placed = elementFactory.createShape.mock.calls.map(([attrs]: [any]) => attrs.type);
    for (const type of NON_CREATABLE_TYPES) {
      expect(placed, type).not.toContain(type);
    }
  });

  it('keeps the tools and the start and end events the rewrite palette always had', () => {
    // Removing module entries must not take the rest of the palette with them.
    const entries = rewriteEntries();

    for (const id of ['hand-tool', 'lasso-tool', 'space-tool', 'global-connect-tool', 'create.start-event', 'create.end-event']) {
      expect(entries[id], id).toBeDefined();
    }
  });

  it('places the type its entry declares', () => {
    const { injected, elementFactory, create } = paletteStubs();
    const entries: Record<string, any> = new (RewritePaletteProvider as any)(...injected).getPaletteEntries();

    entries['create.fileReceiver-module'].action.click(new Event('click'));

    expect(elementFactory.createShape).toHaveBeenCalledWith({ type: 'FileReceiver:FileReceiver' });
    expect(create.start).toHaveBeenCalled();
  });
});

describe('a flow that already carries a non-creatable module', () => {
  it('still resolves every dropped type, so the diagram imports', () => {
    // The moddle extensions are what importXML resolves against. Dropping one here — rather
    // than only the palette entry — is what would break every existing Kafka or DB flow.
    const moddle = new BpmnModdle(moddleExtensionsFor(defaultSettings));

    for (const type of NON_CREATABLE_TYPES) {
      expect(() => moddle.getType(type), type).not.toThrow();
    }
  });

  it('still has a property schema, so the panel can show and edit it', () => {
    for (const type of NON_CREATABLE_TYPES) {
      expect(schemaForType(type), type).toBeDefined();
    }
  });
});
