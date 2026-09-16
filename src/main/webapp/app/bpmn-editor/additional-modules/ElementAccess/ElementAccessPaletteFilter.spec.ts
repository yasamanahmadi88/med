import { BpmnElementAccessConfig } from '../../services/bpmn-element-access.types';
import { CUSTOM_TASK_TYPE } from '../../custom-icons/icon-library';

import ElementAccessPaletteFilter from './ElementAccessPaletteFilter';

describe('ElementAccessPaletteFilter', () => {
  const mciConfig: BpmnElementAccessConfig = {
    allowedTypes: [
      'Merger:Merger',
      'Fragmenter:Fragmenter',
      'FileReceiver:FileReceiver',
      'FileTransmitter:FileTransmitter',
      'CdrParser:CdrParser',
      'CsvTransformer:CsvTransformer',
    ],
    allowedPaletteActions: [
      'create.merger-module',
      'create.fragmenter-module',
      'create.fileReceiver-module',
      'create.FileTransmitter-module',
      'create.cdrParser-module',
      'create.csvTransformerCorner-module',
    ],
    allowedXmlElements: [],
  };

  function createFilter(config: BpmnElementAccessConfig): ElementAccessPaletteFilter {
    let registeredPriority: number | undefined;
    let registeredProvider: unknown;

    const palette = {
      registerProvider(priority: number, provider: unknown): void {
        registeredPriority = priority;
        registeredProvider = provider;
      },
    };

    const filter = new ElementAccessPaletteFilter(palette, config);

    expect(registeredPriority).toBe(1);
    expect(registeredProvider).toBe(filter);

    return filter;
  }

  it('keeps the four generic tools and exactly the six MCI business creation entries', () => {
    const filter = createFilter(mciConfig);

    const entries: Record<string, unknown> = {
      'hand-tool': {},
      'lasso-tool': {},
      'space-tool': {},
      'global-connect-tool': {},
      'tool-separator': {},

      'create.start-event': {},
      'create.end-event': {},

      'create.merger-module': {},
      'create.fragmenter-module': {},
      'create.fileReceiver-module': {},
      'create.FileTransmitter-module': {},
      'create.cdrParser-module': {},
      'create.csvTransformerCorner-module': {},

      'create.KafkaReceiver-module': {},
      'create.KafkaTransmitter-module': {},
      'create.HttpReceiver-module': {},
      'create.HttpTransmitter-module': {},
      'create.dbReceiver-module': {},
      'create.dbTransmitter-module': {},
    };

    const result = filter.getPaletteEntries()(entries);

    expect(Object.keys(result)).toEqual([
      'hand-tool',
      'lasso-tool',
      'space-tool',
      'global-connect-tool',
      'tool-separator',
      'create.merger-module',
      'create.fragmenter-module',
      'create.fileReceiver-module',
      'create.FileTransmitter-module',
      'create.cdrParser-module',
      'create.csvTransformerCorner-module',
    ]);
  });

  it('is fail closed when no BPMN creation action is allowed', () => {
    const filter = createFilter({
      allowedTypes: [],
      allowedPaletteActions: [],
      allowedXmlElements: [],
    });

    const result = filter.getPaletteEntries()({
      'hand-tool': {},
      'lasso-tool': {},
      'space-tool': {},
      'global-connect-tool': {},
      'tool-separator': {},
      'stock-extra-tool': {},
      'create.start-event': {},
      'create.merger-module': {},
      'create.KafkaReceiver-module': {},
    });

    expect(Object.keys(result)).toEqual([
      'hand-tool',
      'lasso-tool',
      'space-tool',
      'global-connect-tool',
      'tool-separator',
    ]);
  });

  it('matches palette action ids exactly including case', () => {
    const filter = createFilter(mciConfig);

    const result = filter.getPaletteEntries()({
      'create.FileTransmitter-module': {},
      'create.fileTransmitter-module': {},
    });

    expect(Object.keys(result)).toEqual(['create.FileTransmitter-module']);
  });

  it('allows dynamic custom-icon entries only when the custom BPMN type is allowed', () => {
    const allowedFilter = createFilter({
      allowedTypes: [CUSTOM_TASK_TYPE],
      allowedPaletteActions: [],
      allowedXmlElements: [],
    });

    const allowedResult = allowedFilter.getPaletteEntries()({
      'hand-tool': {},
      'create.custom-icon-101': {},
      'create.merger-module': {},
    });

    expect(Object.keys(allowedResult)).toEqual([
      'hand-tool',
      'create.custom-icon-101',
    ]);

    const deniedFilter = createFilter({
      allowedTypes: [],
      allowedPaletteActions: [],
      allowedXmlElements: [],
    });

    const deniedResult = deniedFilter.getPaletteEntries()({
      'hand-tool': {},
      'create.custom-icon-101': {},
    });

    expect(Object.keys(deniedResult)).toEqual(['hand-tool']);
  });
});