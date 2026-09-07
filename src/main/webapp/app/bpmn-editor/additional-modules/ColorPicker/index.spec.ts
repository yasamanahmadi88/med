import BpmnColorPickerModule, { ColorContextPadProvider, ColorPopupProvider } from './index';

describe('BPMN color picker module', () => {
  it('opens the color picker from the element context pad', () => {
    const pad = document.createElement('div');
    vi.spyOn(pad, 'getBoundingClientRect').mockReturnValue({ left: 30, top: 40, width: 20, height: 25 } as DOMRect);
    const canvasElement = document.createElement('div');
    vi.spyOn(canvasElement, 'getBoundingClientRect').mockReturnValue({ left: 10, top: 15, width: 500, height: 400 } as DOMRect);
    const contextPad = { registerProvider: vi.fn(), getPad: vi.fn(() => ({ html: pad })) };
    const popupMenu = { open: vi.fn() };
    const provider = new ColorContextPadProvider(contextPad, popupMenu, { getContainer: () => canvasElement }, label => label);

    const entries: any = provider.getContextPadEntries({ id: 'StartEvent_1' });
    entries['set-color'].action.click({ x: 100, y: 120 });

    expect(contextPad.registerProvider).toHaveBeenCalledWith(provider);
    expect(popupMenu.open).toHaveBeenCalledWith(
      [{ id: 'StartEvent_1' }],
      'color-picker',
      expect.objectContaining({ x: 20, y: 55, cursor: { x: 100, y: 120 } }),
    );
  });

  it('offers the same six colors as Vue and applies the selected color', () => {
    const popupMenu = { registerProvider: vi.fn() };
    const modeling = { setColor: vi.fn() };
    const provider = new ColorPopupProvider(undefined, popupMenu, modeling, label => label);
    const elements = [{ id: 'StartEvent_1' }];

    const entries: any[] = provider.getEntries(elements);
    entries.find(entry => entry.id === 'red-color').action();

    expect(popupMenu.registerProvider).toHaveBeenCalledWith('color-picker', provider);
    expect(entries.map(entry => entry.title)).toEqual(['Default', 'Blue', 'Orange', 'Green', 'Red', 'Purple']);
    expect(modeling.setColor).toHaveBeenCalledWith(elements, { label: 'Red', fill: '#FFCDD2', stroke: '#831311' });

    entries.find(entry => entry.id === 'default-color').action();
    expect(modeling.setColor).toHaveBeenLastCalledWith(elements, { label: 'Default', fill: undefined, stroke: undefined });
    expect(BpmnColorPickerModule.__init__).toEqual(['colorContextPadProvider', 'colorPopupProvider']);
  });
});
