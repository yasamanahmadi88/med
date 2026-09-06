// @vitest-environment jsdom
import ContextMenuProvider, { isAppendAction } from './ContextMenuProvider';
import { appendOptions } from './append-options';

/**
 * These exercise real DOM listeners — the canvas click that dismisses the replace menu — so
 * they need a document. `ng test` supplies one; the docblock above keeps the file runnable
 * under the bare vitest config too.
 *
 * The right-click is the only way to reach either menu, so these specs pin the routing: which
 * element goes to the stock replace popup, which raises the create menu, and what the settings
 * turn off. The menus' own rendering is covered by the Playwright suite.
 */
describe('context menu', () => {
  const build = (config?: { enabled?: boolean; custom?: boolean }) => {
    const handlers: Record<string, (event: any) => void> = {};
    const eventBus = {
      on: vi.fn((event: string, _priority: number, callback: (e: any) => void) => {
        handlers[event] = callback;
      }),
      fire: vi.fn(),
    };
    const popupMenu = { open: vi.fn(), isEmpty: vi.fn().mockReturnValue(false), isOpen: vi.fn().mockReturnValue(true), close: vi.fn() };
    const container = document.createElement('div');
    const canvas = { getContainer: () => container };

    // The provider registers its listener from the constructor, so building it is the setup.
    const provider = new ContextMenuProvider(eventBus, popupMenu, canvas, config);

    const rightClick = (element: any, position = { clientX: 100, clientY: 200 }): MouseEvent => {
      const originalEvent = { ...position, preventDefault: vi.fn(), stopPropagation: vi.fn() } as unknown as MouseEvent;
      handlers['element.contextmenu']?.({ element, originalEvent });
      return originalEvent;
    };

    return { provider, eventBus, popupMenu, canvas, container, rightClick, handlers };
  };

  const elementOf = (type: string): any => ({ type, businessObject: { $type: type, $instanceOf: (t: string) => t === type } });

  describe('isAppendAction', () => {
    it('treats the containers and the bare canvas as places to create', () => {
      // These have no type of their own to swap, so the only useful menu is "create".
      expect(isAppendAction(undefined)).toBe(true);
      for (const type of ['bpmn:Process', 'bpmn:Collaboration', 'bpmn:Participant', 'bpmn:SubProcess']) {
        expect(isAppendAction(elementOf(type))).toBe(true);
      }
    });

    it('treats an ordinary element as something to replace', () => {
      expect(isAppendAction(elementOf('bpmn:Task'))).toBe(false);
      expect(isAppendAction(elementOf('bpmn:StartEvent'))).toBe(false);
    });
  });

  describe('routing the right-click', () => {
    it('opens the stock replace menu for an element, offset from the pointer', () => {
      const { popupMenu, rightClick } = build();
      const element = elementOf('bpmn:Task');

      rightClick(element);

      expect(popupMenu.open).toHaveBeenCalledWith(element, 'bpmn-replace', { cursor: { x: 110, y: 210 } });
    });

    it('suppresses the browser menu only when it has something to show', () => {
      const { popupMenu, rightClick } = build();
      popupMenu.isEmpty.mockReturnValue(true);

      const event = rightClick(elementOf('bpmn:Task'));

      // Nothing to offer, so the browser's own menu is better than an empty panel.
      expect(popupMenu.open).not.toHaveBeenCalled();
      expect(event.preventDefault).not.toHaveBeenCalled();
    });

    it('raises the create menu at the pointer for a container', () => {
      const { eventBus, popupMenu, rightClick } = build();

      const event = rightClick(elementOf('bpmn:Process'));

      expect(eventBus.fire).toHaveBeenCalledWith('contextMenu.append.open', { x: 100, y: 200 });
      expect(popupMenu.open).not.toHaveBeenCalled();
      expect(event.preventDefault).toHaveBeenCalled();
    });

    it('registers above the default handling so it wins the event', () => {
      // diagram-js runs context-pad handlers on this event too; a lower priority loses the menu.
      const { eventBus } = build();

      expect(eventBus.on).toHaveBeenCalledWith('element.contextmenu', 2000, expect.any(Function));
    });
  });

  describe('settings', () => {
    it('leaves right-click to the browser when the context menu is off', () => {
      const { eventBus } = build({ enabled: false });

      expect(eventBus.on).not.toHaveBeenCalled();
    });

    it('offers nothing on a container when the custom menu is off', () => {
      // Matches the Vue editor with customContextmenu false: the stock menu is empty for a
      // container, and it had no other list to fall back on either.
      const { eventBus, popupMenu, rightClick } = build({ custom: false });

      rightClick(elementOf('bpmn:Process'));

      expect(eventBus.fire).not.toHaveBeenCalled();
      expect(popupMenu.open).not.toHaveBeenCalled();
    });

    it('still replaces an element when the custom menu is off', () => {
      const { popupMenu, rightClick } = build({ custom: false });

      rightClick(elementOf('bpmn:Task'));

      expect(popupMenu.open).toHaveBeenCalled();
    });
  });

  describe('dismissing the replace menu', () => {
    it('closes it on a click on bare canvas', () => {
      // The popup closes itself for most interactions but not this one, which is the obvious
      // way to dismiss it.
      const { popupMenu, container, rightClick } = build();
      rightClick(elementOf('bpmn:Task'));

      const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
      container.appendChild(svg);
      svg.dispatchEvent(new MouseEvent('click', { bubbles: true }));

      expect(popupMenu.close).toHaveBeenCalled();
    });

    it('leaves it open for a click on an element inside the canvas', () => {
      const { popupMenu, container, rightClick } = build();
      rightClick(elementOf('bpmn:Task'));

      const shape = document.createElementNS('http://www.w3.org/2000/svg', 'rect');
      container.appendChild(shape);
      shape.dispatchEvent(new MouseEvent('click', { bubbles: true }));

      expect(popupMenu.close).not.toHaveBeenCalled();
    });
  });

  describe('appendOptions', () => {
    it('offers the four groups the Vue menu offered', () => {
      const types = new Set(appendOptions().map(option => option.target.type));

      expect(types.has('bpmn:StartEvent')).toBe(true);
      expect(types.has('bpmn:Task')).toBe(true);
      expect(types.has('bpmn:ExclusiveGateway')).toBe(true);
      expect(types.has('bpmn:BoundaryEvent')).toBe(true);
    });

    it('gives every entry the label and icon the menu renders', () => {
      // A missing className renders a blank square; a non-string label renders as source code.
      for (const option of appendOptions()) {
        expect(typeof option.label).toBe('string');
        expect(option.className).toBeTruthy();
        expect(option.target.type).toMatch(/^bpmn:/);
      }
    });
  });
});
