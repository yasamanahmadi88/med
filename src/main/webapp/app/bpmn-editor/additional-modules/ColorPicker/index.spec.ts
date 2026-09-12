import { Base } from 'diagram-js/lib/model';
import BpmnColorPickerModule, { ColorContextPadProvider, ColorPopupProvider, DEFAULT_COLORS, ColorDefinition } from './index';

const element = { id: 'StartEvent_1' } as unknown as Base;

const rect = (left: number, top: number, width: number, height: number): DOMRect =>
  ({ left, top, width, height, right: left + width, bottom: top + height, x: left, y: top }) as DOMRect;

const elementWithRect = (value: DOMRect): HTMLElement => {
  const node = document.createElement('div');
  vi.spyOn(node, 'getBoundingClientRect').mockReturnValue(value);
  return node;
};

describe('bpmn-editor colour picker', () => {
  describe('the context-pad entry', () => {
    const providerWith = () => {
      const pad = elementWithRect(rect(30, 40, 20, 25));
      const container = elementWithRect(rect(10, 15, 500, 400));
      const contextPad = { registerProvider: vi.fn(), getPad: vi.fn(() => ({ html: pad })) };
      const popupMenu = { registerProvider: vi.fn(), open: vi.fn() };
      const provider = new ColorContextPadProvider(contextPad, popupMenu, { getContainer: () => container }, label => label);

      return { provider, contextPad, popupMenu };
    };

    it('registers itself, so the stock entries stay', () => {
      // registerProvider adds to the provider list rather than replacing it. This is the whole
      // difference from the Vue ContextPad modules, which returned {} and, in the rewrite
      // variant, did it under the stock provider name.
      const { provider, contextPad } = providerWith();

      expect(contextPad.registerProvider).toHaveBeenCalledWith(provider);
      expect(Object.keys(provider.getContextPadEntries(element))).toEqual(['set-color']);
    });

    it('anchors the menu below the pad, in container coordinates', () => {
      const { provider, popupMenu } = providerWith();

      provider.getContextPadEntries(element)['set-color'].action.click({ x: 100, y: 120 });

      // pad.left - container.left = 30 - 10; pad.top - container.top + pad.height + 5 = 40 - 15 + 25 + 5
      expect(popupMenu.open).toHaveBeenCalledWith([element], 'color-picker', {
        x: 20,
        y: 55,
        cursor: { x: 100, y: 120 },
      });
    });

    it('offers the same entry for a multi-selection, and opens it on the whole list', () => {
      // diagram-js calls getMultiElementContextPadEntries instead when the pad target is an
      // array, so without this a multi-selection loses the button entirely.
      const { provider, popupMenu } = providerWith();
      const second = { id: 'Task_1' } as unknown as Base;

      provider.getMultiElementContextPadEntries([element, second])['set-color'].action.click({ x: 0, y: 0 });

      expect(popupMenu.open).toHaveBeenCalledWith([element, second], 'color-picker', expect.anything());
    });

    it('carries its own icon, because the bpmn icon font has no paint glyph', () => {
      const { provider } = providerWith();
      const entry = provider.getContextPadEntries(element)['set-color'];

      expect(entry.imageUrl).toMatch(/^data:image\/svg\+xml;utf8,/);
      expect(entry.group).toBe('edit');
    });

    it('passes the entry title through translate', () => {
      const pad = elementWithRect(rect(0, 0, 0, 0));
      const contextPad = { registerProvider: vi.fn(), getPad: vi.fn(() => ({ html: pad })) };
      const provider = new ColorContextPadProvider(
        contextPad,
        { registerProvider: vi.fn(), open: vi.fn() },
        { getContainer: () => elementWithRect(rect(0, 0, 0, 0)) },
        label => `translated:${label}`,
      );

      expect(provider.getContextPadEntries(element)['set-color'].title).toBe('translated:Set Color');
    });
  });

  describe('the swatch list', () => {
    const providerWith = (config?: { colors?: readonly ColorDefinition[] }) => {
      const popupMenu = { registerProvider: vi.fn(), open: vi.fn() };
      const modeling = { setColor: vi.fn() };
      const provider = new ColorPopupProvider(config, popupMenu, modeling, label => label);

      return { provider, popupMenu, modeling };
    };

    it('registers under the id the context-pad entry opens', () => {
      const { provider, popupMenu } = providerWith();

      expect(popupMenu.registerProvider).toHaveBeenCalledWith('color-picker', provider);
    });

    it('offers the six colours the Vue picker had', () => {
      const { provider } = providerWith();

      expect(provider.getEntries([element]).map(entry => entry.title)).toEqual(['Default', 'Blue', 'Orange', 'Green', 'Red', 'Purple']);
    });

    it('applies the selected colour to the whole selection in one command', () => {
      // One setColor call, not one per element: modeling.setColor takes a list, so the paint is a
      // single command-stack entry and a single undo.
      const { provider, modeling } = providerWith();
      const second = { id: 'Task_1' } as unknown as Base;
      const elements = [element, second];

      provider
        .getEntries(elements)
        .find(entry => entry.id === 'red-color')!
        .action();

      expect(modeling.setColor).toHaveBeenCalledTimes(1);
      expect(modeling.setColor).toHaveBeenCalledWith(elements, { label: 'Red', fill: '#FFCDD2', stroke: '#831311' });
    });

    it('sends undefined for Default, which is what clears the stored colour', () => {
      // Not white: setColor deletes bioc:fill/bioc:stroke from the DI when the value is
      // undefined, and that is what returns a shape to the stock look.
      const { provider, modeling } = providerWith();

      provider
        .getEntries([element])
        .find(entry => entry.id === 'default-color')!
        .action();

      expect(modeling.setColor).toHaveBeenCalledWith([element], { label: 'Default', fill: undefined, stroke: undefined });
    });

    it('falls back to the defaults when nothing configures colorPicker', () => {
      // didi injects undefined for a config.* key no caller sets, which is the shipped case.
      expect(providerWith(undefined).provider.getEntries([element])).toHaveLength(DEFAULT_COLORS.length);
      expect(providerWith({}).provider.getEntries([element])).toHaveLength(DEFAULT_COLORS.length);
    });

    it('honours a configured colour list', () => {
      const { provider, modeling } = providerWith({ colors: [{ label: 'Teal', fill: '#B2DFDB', stroke: '#00695C' }] });
      const entries = provider.getEntries([element]);

      expect(entries.map(entry => entry.id)).toEqual(['teal-color']);
      entries[0].action();
      expect(modeling.setColor).toHaveBeenCalledWith([element], { label: 'Teal', fill: '#B2DFDB', stroke: '#00695C' });
    });

    it('percent-encodes a configured colour instead of letting it add markup', () => {
      // The swatch is built by interpolation, so a colour carrying a quote would otherwise close
      // the rect and append elements of its own.
      const hostile = '#fff" onload="alert(1)';
      const { provider } = providerWith({ colors: [{ label: 'Hostile', fill: hostile, stroke: '#000' }] });
      const url = provider.getEntries([element])[0].imageUrl;

      expect(url).not.toContain('onload=');
      expect(url).not.toContain('"');
      expect(decodeURIComponent(url.replace('data:image/svg+xml;utf8,', ''))).toContain(hostile);
    });

    it('passes every swatch title through translate', () => {
      const provider = new ColorPopupProvider(undefined, { registerProvider: vi.fn(), open: vi.fn() }, { setColor: vi.fn() }, label =>
        label.toUpperCase(),
      );

      expect(provider.getEntries([element])[0].title).toBe('DEFAULT');
    });
  });

  it('declares both providers for didi to instantiate eagerly', () => {
    // Neither provider is injected anywhere; they register themselves in their constructors, so
    // without __init__ didi never builds them and the button never appears.
    expect(BpmnColorPickerModule.__init__).toEqual(['colorContextPadProvider', 'colorPopupProvider']);
    expect(BpmnColorPickerModule.colorContextPadProvider).toEqual(['type', ColorContextPadProvider]);
    expect(BpmnColorPickerModule.colorPopupProvider).toEqual(['type', ColorPopupProvider]);
  });
});
