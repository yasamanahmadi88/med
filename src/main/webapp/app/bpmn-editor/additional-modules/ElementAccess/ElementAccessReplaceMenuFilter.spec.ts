import { BpmnElementAccessConfig } from '../../services/bpmn-element-access.types';

import ElementAccessReplaceMenuFilter from './ElementAccessReplaceMenuFilter';

class PopupMenuStub {
  providerId?: string;
  priority?: number;
  provider?: unknown;

  registerProvider(providerId: string, provider: unknown): void;
  registerProvider(providerId: string, priority: number, provider: unknown): void;
  registerProvider(providerId: string, priorityOrProvider: number | unknown, provider?: unknown): void {
    this.providerId = providerId;

    if (typeof priorityOrProvider === 'number' && provider !== undefined) {
      this.priority = priorityOrProvider;
      this.provider = provider;
      return;
    }

    this.priority = undefined;
    this.provider = priorityOrProvider;
  }
}

describe('ElementAccessReplaceMenuFilter', () => {
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

  function createFilter(config: BpmnElementAccessConfig): {
    filter: ElementAccessReplaceMenuFilter;
    popupMenu: PopupMenuStub;
  } {
    const popupMenu = new PopupMenuStub();
    const filter = new ElementAccessReplaceMenuFilter(popupMenu, config);

    return { filter, popupMenu };
  }

  it('registers as the lowest-priority bpmn-replace middleware', () => {
    const { filter, popupMenu } = createFilter(mciConfig);

    expect(popupMenu.providerId).toBe('bpmn-replace');
    expect(popupMenu.priority).toBe(1);
    expect(popupMenu.provider).toBe(filter);
  });

  it('removes stock BPMN replace destinations that MCI does not allow', () => {
    const { filter } = createFilter(mciConfig);

    const result = filter.getPopupMenuEntries()({
      'replace-with-none-start': {},
      'replace-with-message-start': {},
      'replace-with-task': {},
      'replace-with-user-task': {},
      'replace-with-exclusive-gateway': {},
      'replace-with-expanded-pool': {},
    });

    expect(Object.keys(result)).toEqual([]);
  });

  it('keeps stock replace destinations whose target BPMN type is allowed', () => {
    const { filter } = createFilter({
      allowedTypes: ['bpmn:StartEvent'],
      allowedPaletteActions: [],
      allowedXmlElements: [],
    });

    const result = filter.getPopupMenuEntries()({
      'replace-with-none-start': {},
      'replace-with-message-start': {},
      'replace-with-timer-start': {},
      'replace-with-task': {},
      'replace-with-exclusive-gateway': {},
    });

    expect(Object.keys(result)).toEqual(['replace-with-none-start', 'replace-with-message-start', 'replace-with-timer-start']);
  });

  it('preserves the three stock sequence-flow mutations without allowing BPMN destinations', () => {
    const { filter } = createFilter({
      allowedTypes: [],
      allowedPaletteActions: [],
      allowedXmlElements: [],
    });

    const result = filter.getPopupMenuEntries()({
      'replace-with-sequence-flow': {},
      'replace-with-default-flow': {},
      'replace-with-conditional-flow': {},
      'replace-with-task': {},
    });

    expect(Object.keys(result)).toEqual(['replace-with-sequence-flow', 'replace-with-default-flow', 'replace-with-conditional-flow']);
  });

  it('fails closed for an unknown body entry', () => {
    const { filter } = createFilter(mciConfig);

    const result = filter.getPopupMenuEntries()({
      'unknown-third-party-replace-action': {},
      'replace-with-task': {},
    });

    expect(Object.keys(result)).toEqual([]);
  });

  it('keeps the legacy getEntries alias behavior identical', () => {
    const { filter } = createFilter({
      allowedTypes: ['bpmn:StartEvent'],
      allowedPaletteActions: [],
      allowedXmlElements: [],
    });

    const entries = {
      'replace-with-none-start': {},
      'replace-with-task': {},
      'replace-with-default-flow': {},
      'unknown-third-party-replace-action': {},
    };

    expect(filter.getEntries()(entries)).toEqual(filter.getPopupMenuEntries()(entries));
  });
});
