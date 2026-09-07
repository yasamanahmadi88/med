import BpmnModdle, { ModdleElement } from 'bpmn-moddle';

import customIcon from '../moddle-extensions/customIcons.json';
import CustomIconLibrary, { AddIconResult, UPDATE_ICON_LIBRARY } from './CustomIconLibrary';
import CustomIconPaletteProvider, { PALETTE_ENTRY_CLASS } from './CustomIconPaletteProvider';
import CustomIcons from './index';
import { CUSTOM_TASK_TYPE, readIconLibrary } from './icon-library';
import { MAX_ICON_BYTES, MAX_LIBRARY_BYTES, toIconDataUri } from './svg-icon';

/**
 * The library service and the palette entries it feeds.
 *
 * bpmn-js is not constructed here — jsdom has no SVG layout, which is why every spec in this module
 * stubs the modeler — but nothing is faked that decides anything: the moddle is the real one, the
 * command handler is instantiated from its own `$inject` list the way didi would, and the icons go
 * into and come back out of a real `bpmn:Definitions`. What a browser is needed for is that the
 * shapes are actually drawn, and that is the Playwright suite's job.
 */
describe('CustomIconLibrary', () => {
  const CLEAN = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16"><circle cx="8" cy="8" r="7" /></svg>';
  const HOSTILE = '<svg xmlns="http://www.w3.org/2000/svg" onload="alert(1)"><rect /></svg>';

  let moddle: BpmnModdle;
  let definitions: ModdleElement;
  let library: CustomIconLibrary;
  let palette: { _rebuild: ReturnType<typeof vi.fn> };
  let listeners: Record<string, () => void>;
  let shapes: { businessObject: ModdleElement }[];
  let executed: { command: string; context: any; dirty: unknown[] }[];

  const messageOf = (result: AddIconResult): string => (result.ok ? '' : result.message);

  /** Stands in for didi and diagram-js's command stack together: the handler is the real one. */
  const buildCommandStack = (services: Record<string, unknown>): any => {
    const handlers: Record<string, any> = {};

    return {
      registerHandler(command: string, HandlerClass: any) {
        handlers[command] = new HandlerClass(...HandlerClass.$inject.map((dependency: string) => services[dependency]));
      },
      execute(command: string, context: any) {
        const dirty = handlers[command].execute(context);
        executed.push({ command, context, dirty });
        listeners[`commandStack.${command}.executed`]?.();
        return dirty;
      },
      undo() {
        const last = executed.pop()!;
        const dirty = handlers[last.command].revert(last.context);
        listeners[`commandStack.${last.command}.reverted`]?.();
        return dirty;
      },
    };
  };

  beforeEach(() => {
    moddle = new BpmnModdle({ customIcon });
    definitions = moddle.create('bpmn:Definitions', { id: 'Definitions_1' });
    listeners = {};
    shapes = [];
    executed = [];

    palette = { _rebuild: vi.fn() };

    const services = {
      bpmnjs: { getDefinitions: () => definitions },
      bpmnFactory: { create: (type: string, attrs?: object) => moddle.create(type, attrs ?? {}) },
      elementRegistry: { filter: (fn: (element: unknown) => boolean) => shapes.filter(fn) },
    };
    const commandStack = buildCommandStack(services);
    const eventBus = {
      on(event: string, callback: () => void) {
        listeners[event] = callback;
      },
    };

    library = new CustomIconLibrary(eventBus as any, commandStack, services.bpmnjs, services.bpmnFactory as any, palette as any);
  });

  describe('adding', () => {
    it('stores the icon in the diagram, through the command stack', () => {
      const result = library.add('Payment', CLEAN);

      expect(result.ok).toBe(true);
      expect(result.ok && result.icon.id).toBe('Icon_1');
      // In the document, so it is saved with the diagram and travels with the flow.
      expect(readIconLibrary(definitions)).toEqual([{ id: 'Icon_1', name: 'Payment', contents: toIconDataUri(CLEAN) }]);
      // Through a command, so it is undoable and the editor knows the diagram changed.
      expect(executed.map(entry => entry.command)).toEqual([UPDATE_ICON_LIBRARY]);
    });

    it('numbers each icon and keeps the ones already stored', () => {
      library.add('First', CLEAN);
      library.add('Second', CLEAN);

      expect(readIconLibrary(definitions).map(icon => [icon.id, icon.name])).toEqual([
        ['Icon_1', 'First'],
        ['Icon_2', 'Second'],
      ]);
    });

    it('refuses a hostile SVG and writes nothing', () => {
      const result = library.add('Trojan', HOSTILE);

      expect(result.ok).toBe(false);
      expect(messageOf(result)).toContain('onload');
      expect(readIconLibrary(definitions)).toEqual([]);
      expect(executed).toEqual([]);
    });

    it('refuses an unnamed icon', () => {
      expect(library.add('   ', CLEAN).ok).toBe(false);
      expect(readIconLibrary(definitions)).toEqual([]);
    });

    it('refuses the icon that would take the diagram over the library limit, and says so', () => {
      // Filled with the largest icons that pass on their own, so it is the total that refuses
      // this one and not the per-icon check.
      const big = `<svg xmlns="http://www.w3.org/2000/svg"><desc>${'x'.repeat(MAX_ICON_BYTES - 200)}</desc></svg>`;

      // Bounded, and the bound is asserted: a library that silently stored nothing would let an
      // unbounded loop run for ever rather than fail.
      let added = 0;
      for (let attempt = 0; attempt < 20; attempt++) {
        if (!library.add(`Icon ${attempt}`, big).ok) {
          break;
        }
        added++;
      }

      expect(added).toBeGreaterThan(0);
      expect(added).toBeLessThan(20);
      const refused = library.add('One too many', big);
      expect(refused.ok).toBe(false);
      expect(messageOf(refused)).toContain('192 KB');
      expect(library.remainingBytes()).toBeLessThan(toIconDataUri(big).length);
      expect(readIconLibrary(definitions)).toHaveLength(added);
    });

    it('reports what is left of the budget', () => {
      expect(library.remainingBytes()).toBe(MAX_LIBRARY_BYTES);

      library.add('Payment', CLEAN);

      expect(library.remainingBytes()).toBe(MAX_LIBRARY_BYTES - toIconDataUri(CLEAN).length);
    });
  });

  describe('removing', () => {
    it('drops the icon and hands back the shapes that were drawn from it, so they redraw', () => {
      library.add('Payment', CLEAN);
      shapes = [{ businessObject: moddle.create(CUSTOM_TASK_TYPE, { id: 'Task_1', iconId: 'Icon_1' }) }];

      library.remove('Icon_1');

      expect(readIconLibrary(definitions)).toEqual([]);
      // The shapes a command reports are the ones diagram-js re-renders. Naming them is the whole
      // reason removing an icon does not leave a broken shape: the task stays, and redraws as the
      // placeholder rather than keeping an icon the diagram no longer carries.
      expect(executed[executed.length - 1].dirty).toEqual(shapes);
      expect(shapes[0].businessObject.get('iconId')).toBe('Icon_1');
    });

    it('leaves the other icons alone', () => {
      library.add('First', CLEAN);
      library.add('Second', CLEAN);

      library.remove('Icon_1');

      expect(readIconLibrary(definitions).map(icon => icon.id)).toEqual(['Icon_2']);
    });
  });

  describe('undo', () => {
    it('puts the library back exactly as it was', () => {
      library.add('First', CLEAN);
      library.add('Second', CLEAN);
      expect(readIconLibrary(definitions).map(icon => icon.id)).toEqual(['Icon_1', 'Icon_2']);

      undoLast();

      expect(readIconLibrary(definitions).map(icon => icon.id)).toEqual(['Icon_1']);
    });

    it('removes the library element altogether when the first icon is undone', () => {
      library.add('First', CLEAN);

      undoLast();

      expect(readIconLibrary(definitions)).toEqual([]);
      expect(definitions.get('extensionElements')).toBeUndefined();
    });
  });

  describe('the palette', () => {
    it('rebuilds when the library changes and when a diagram is imported', () => {
      expect(palette._rebuild).not.toHaveBeenCalled();

      library.add('Payment', CLEAN);
      expect(palette._rebuild).toHaveBeenCalledTimes(1);

      listeners['import.done']();
      expect(palette._rebuild).toHaveBeenCalledTimes(2);
    });

    it('re-reads the diagram after an import rather than serving the icons it had cached', () => {
      library.add('Payment', CLEAN);
      expect(library.getIcons()).toHaveLength(1);

      definitions = moddle.create('bpmn:Definitions', { id: 'Definitions_2' });

      expect(library.getIcons()).toEqual([]);
    });
  });

  /** Undo the way the toolbar's button does: through the stack, not by editing the model back. */
  function undoLast(): void {
    (library as any).commandStack.undo();
  }
});

describe('CustomIconPaletteProvider', () => {
  const CLEAN = '<svg xmlns="http://www.w3.org/2000/svg"><rect /></svg>';
  const contents = toIconDataUri(CLEAN);

  const build = (icons: { id: string; name: string; contents: string }[]) => {
    const moddle = new BpmnModdle({ customIcon });
    const create = { start: vi.fn() };
    const elementFactory = { createShape: vi.fn((attrs: any) => ({ ...attrs })) };
    const bpmnFactory = { create: (type: string, attrs?: object) => moddle.create(type, attrs ?? {}) };
    const palette = { registerProvider: vi.fn() };
    const provider = new CustomIconPaletteProvider(
      palette,
      create,
      elementFactory as any,
      bpmnFactory as any,
      { getIcons: () => icons } as unknown as CustomIconLibrary,
    );

    return { provider, palette, create, elementFactory };
  };

  it('registers itself with the palette, alongside whichever provider the settings chose', () => {
    const { palette, provider } = build([]);

    expect(palette.registerProvider).toHaveBeenCalledWith(provider);
  });

  it('offers one entry per icon, drawn from the stored data URI', () => {
    const { provider } = build([
      { id: 'Icon_1', name: 'Payment', contents },
      { id: 'Icon_2', name: 'Refund', contents },
    ]);

    const entries = provider.getPaletteEntries() as Record<string, any>;

    expect(Object.keys(entries)).toEqual(['create.custom-icon-Icon_1', 'create.custom-icon-Icon_2']);
    expect(entries['create.custom-icon-Icon_1']).toMatchObject({
      group: 'activity',
      className: PALETTE_ENTRY_CLASS,
      title: 'Create Payment',
      // diagram-js sets this as `src` on an `<img>` it creates (`Palette.js:275-280`) — the icon
      // is never handed to the palette as markup.
      imageUrl: contents,
    });
  });

  it('offers nothing when the diagram carries no icons', () => {
    expect(build([]).provider.getPaletteEntries()).toEqual({});
  });

  it('places one registered type carrying the icon id, not a type per icon', () => {
    const { provider, elementFactory, create } = build([{ id: 'Icon_1', name: 'Payment', contents }]);
    const entry = (provider.getPaletteEntries() as Record<string, any>)['create.custom-icon-Icon_1'];

    entry.action.click(new Event('click'));

    const [attrs] = elementFactory.createShape.mock.calls[0];
    expect(attrs.type).toBe(CUSTOM_TASK_TYPE);
    expect(attrs.businessObject.$type).toBe(CUSTOM_TASK_TYPE);
    expect(attrs.businessObject.get('iconId')).toBe('Icon_1');
    expect(attrs.businessObject.get('name')).toBe('Payment');
    expect(create.start).toHaveBeenCalled();
  });

  it('drags the same shape it clicks', () => {
    const { provider, elementFactory } = build([{ id: 'Icon_1', name: 'Payment', contents }]);
    const entry = (provider.getPaletteEntries() as Record<string, any>)['create.custom-icon-Icon_1'];

    entry.action.dragstart(new Event('dragstart'));

    expect(elementFactory.createShape).toHaveBeenCalledTimes(1);
  });
});

describe('the custom icons module', () => {
  it('initialises all three of its services, because nothing asks for them by name', () => {
    // didi builds a service only when something injects it. The palette provider registers itself
    // with the palette, the renderer with the event bus, and the library registers the command
    // handler — so without `__init__` the whole feature is inert and silent about it.
    expect(CustomIcons.__init__).toEqual(['customIcons', 'customIconPaletteProvider', 'customIconRenderer']);
    expect(Object.keys(CustomIcons)).toEqual(['__init__', 'customIcons', 'customIconPaletteProvider', 'customIconRenderer']);
  });
});
